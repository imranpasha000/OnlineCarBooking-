package com.boxcars.trip.service;

import com.boxcars.common.event.DomainEvent;
import com.boxcars.trip.dto.CreateTripRequest;
import com.boxcars.trip.dto.LocationUpdateRequest;
import com.boxcars.trip.entity.Trip;
import com.boxcars.trip.entity.TripStatus;
import com.boxcars.trip.exception.BadRequestException;
import com.boxcars.trip.exception.ForbiddenException;
import com.boxcars.trip.exception.ResourceNotFoundException;
import com.boxcars.trip.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TripService {

    private static final List<TripStatus> ACTIVE_STATUSES = List.of(
            TripStatus.REQUESTED,
            TripStatus.OFFERED,
            TripStatus.ASSIGNED,
            TripStatus.STARTED
    );

    private final TripRepository tripRepository;
    private final TripEventPublisher eventPublisher;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentHashMap<Long, Map<String, Double>> driverLocations = new ConcurrentHashMap<>();

    @Transactional
    public Trip create(Long riderId, CreateTripRequest request) {
        double distanceKm = haversineKm(
                request.pickupLat(), request.pickupLng(),
                request.dropoffLat(), request.dropoffLng()
        );
        BigDecimal fare = BigDecimal.valueOf(50 + distanceKm * 12).setScale(2, RoundingMode.HALF_UP);

        Trip trip = Trip.builder()
                .riderId(riderId)
                .vehicleId(request.vehicleId())
                .pickupLat(request.pickupLat())
                .pickupLng(request.pickupLng())
                .pickupAddress(request.pickupAddress())
                .dropoffLat(request.dropoffLat())
                .dropoffLng(request.dropoffLng())
                .dropoffAddress(request.dropoffAddress())
                .status(TripStatus.REQUESTED)
                .distanceKm(roundKm(distanceKm))
                .fare(fare)
                .build();

        trip = tripRepository.save(trip);

        Map<String, Object> payload = new HashMap<>();
        payload.put("tripId", trip.getId());
        payload.put("riderId", trip.getRiderId());
        payload.put("vehicleId", trip.getVehicleId());
        payload.put("pickupLat", trip.getPickupLat());
        payload.put("pickupLng", trip.getPickupLng());
        payload.put("pickupAddress", trip.getPickupAddress());
        payload.put("dropoffLat", trip.getDropoffLat());
        payload.put("dropoffLng", trip.getDropoffLng());
        payload.put("dropoffAddress", trip.getDropoffAddress());
        payload.put("fare", trip.getFare());
        payload.put("distanceKm", trip.getDistanceKm());
        eventPublisher.publish(DomainEvent.of(DomainEvent.TRIP_REQUESTED, String.valueOf(trip.getId()), payload));

        return trip;
    }

    @Transactional(readOnly = true)
    public List<Trip> myTrips(Long userId) {
        return tripRepository.findByRiderIdOrDriverIdOrderByCreatedAtDesc(userId, userId);
    }

    @Transactional(readOnly = true)
    public Trip getById(Long id) {
        return tripRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found: " + id));
    }

    @Transactional
    public Trip accept(Long tripId, Long driverId) {
        Trip trip = getById(tripId);
        if (trip.getStatus() != TripStatus.REQUESTED && trip.getStatus() != TripStatus.OFFERED) {
            throw new BadRequestException("Trip cannot be accepted in status " + trip.getStatus());
        }
        trip.setDriverId(driverId);
        trip.setStatus(TripStatus.ASSIGNED);
        trip = tripRepository.save(trip);

        Map<String, Object> payload = new HashMap<>();
        payload.put("tripId", trip.getId());
        payload.put("riderId", trip.getRiderId());
        payload.put("driverId", trip.getDriverId());
        payload.put("vehicleId", trip.getVehicleId());
        eventPublisher.publish(DomainEvent.of(DomainEvent.TRIP_ASSIGNED, String.valueOf(trip.getId()), payload));

        return trip;
    }

    @Transactional
    public Trip start(Long tripId, Long driverId) {
        Trip trip = getById(tripId);
        requireDriver(trip, driverId);
        if (trip.getStatus() != TripStatus.ASSIGNED) {
            throw new BadRequestException("Trip must be ASSIGNED to start");
        }
        trip.setStatus(TripStatus.STARTED);
        trip.setStartedAt(Instant.now());
        trip = tripRepository.save(trip);

        Map<String, Object> payload = Map.of(
                "tripId", trip.getId(),
                "riderId", trip.getRiderId(),
                "driverId", trip.getDriverId()
        );
        eventPublisher.publish(DomainEvent.of(DomainEvent.TRIP_STARTED, String.valueOf(trip.getId()), payload));

        return trip;
    }

    @Transactional
    public Trip complete(Long tripId, Long driverId) {
        Trip trip = getById(tripId);
        requireDriver(trip, driverId);
        if (trip.getStatus() != TripStatus.STARTED) {
            throw new BadRequestException("Trip must be STARTED to complete");
        }
        if (trip.getFare() == null && trip.getDistanceKm() != null) {
            trip.setFare(BigDecimal.valueOf(50 + trip.getDistanceKm() * 12).setScale(2, RoundingMode.HALF_UP));
        }
        trip.setStatus(TripStatus.COMPLETED);
        trip.setCompletedAt(Instant.now());
        trip = tripRepository.save(trip);

        Map<String, Object> payload = new HashMap<>();
        payload.put("tripId", trip.getId());
        payload.put("riderId", trip.getRiderId());
        payload.put("driverId", trip.getDriverId());
        payload.put("amount", trip.getFare());
        eventPublisher.publish(DomainEvent.of(DomainEvent.TRIP_COMPLETED, String.valueOf(trip.getId()), payload));

        driverLocations.remove(tripId);
        return trip;
    }

    @Transactional
    public Trip cancel(Long tripId, Long userId) {
        Trip trip = getById(tripId);
        boolean isRider = trip.getRiderId().equals(userId);
        boolean isDriver = trip.getDriverId() != null && trip.getDriverId().equals(userId);
        if (!isRider && !isDriver) {
            throw new ForbiddenException("Only the rider or assigned driver can cancel this trip");
        }
        if (trip.getStatus() == TripStatus.COMPLETED || trip.getStatus() == TripStatus.CANCELLED) {
            throw new BadRequestException("Trip cannot be cancelled in status " + trip.getStatus());
        }
        trip.setStatus(TripStatus.CANCELLED);
        trip = tripRepository.save(trip);

        Map<String, Object> payload = new HashMap<>();
        payload.put("tripId", trip.getId());
        payload.put("riderId", trip.getRiderId());
        payload.put("driverId", trip.getDriverId());
        payload.put("cancelledBy", userId);
        eventPublisher.publish(DomainEvent.of(DomainEvent.TRIP_CANCELLED, String.valueOf(trip.getId()), payload));

        driverLocations.remove(tripId);
        return trip;
    }

    public Map<String, Double> updateLocation(Long tripId, Long driverId, LocationUpdateRequest request) {
        Trip trip = getById(tripId);
        requireDriver(trip, driverId);
        if (trip.getStatus() != TripStatus.ASSIGNED && trip.getStatus() != TripStatus.STARTED) {
            throw new BadRequestException("Location updates allowed only for ASSIGNED or STARTED trips");
        }

        Map<String, Double> location = Map.of("lat", request.lat(), "lng", request.lng());
        driverLocations.put(tripId, location);
        messagingTemplate.convertAndSend("/topic/trips/" + tripId + "/location", location);
        return location;
    }

    @Transactional(readOnly = true)
    public List<Trip> pending() {
        return tripRepository.findByStatusOrderByCreatedAtDesc(TripStatus.REQUESTED);
    }

    @Transactional(readOnly = true)
    public Optional<Trip> active(Long userId) {
        return tripRepository.findFirstByRiderIdAndStatusInOrderByCreatedAtDesc(userId, ACTIVE_STATUSES)
                .or(() -> tripRepository.findFirstByDriverIdAndStatusInOrderByCreatedAtDesc(userId, ACTIVE_STATUSES));
    }

    private void requireDriver(Trip trip, Long driverId) {
        if (trip.getDriverId() == null || !trip.getDriverId().equals(driverId)) {
            throw new ForbiddenException("Only the assigned driver can perform this action");
        }
    }

    private static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double earthRadiusKm = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusKm * c;
    }

    private static double roundKm(double km) {
        return BigDecimal.valueOf(km).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }
}
