package com.boxcars.rental.service;

import com.boxcars.common.event.DomainEvent;
import com.boxcars.rental.dto.BookRentalRequest;
import com.boxcars.rental.dto.RentalSearchResponse;
import com.boxcars.rental.entity.RentalBooking;
import com.boxcars.rental.entity.RentalStatus;
import com.boxcars.rental.exception.BadRequestException;
import com.boxcars.rental.exception.ForbiddenException;
import com.boxcars.rental.exception.ResourceNotFoundException;
import com.boxcars.rental.repository.RentalBookingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RentalService {

    private static final List<RentalStatus> BLOCKING_STATUSES = List.of(
            RentalStatus.REQUESTED,
            RentalStatus.CONFIRMED,
            RentalStatus.PICKED_UP
    );

    private final RentalBookingRepository rentalBookingRepository;
    private final RentalEventPublisher eventPublisher;

    @Transactional(readOnly = true)
    public RentalSearchResponse search(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new BadRequestException("startDate and endDate are required");
        }
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("endDate must be on or after startDate");
        }
        List<Long> unavailable = rentalBookingRepository.findUnavailableVehicleIds(
                startDate, endDate, BLOCKING_STATUSES
        );
        return new RentalSearchResponse(
                "Use vehicle-service GET /api/vehicles/search?type=RENTAL, then exclude unavailableVehicleIds for this date range",
                unavailable
        );
    }

    @Transactional
    public RentalBooking book(Long customerId, BookRentalRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("endDate must be on or after startDate");
        }
        if (rentalBookingRepository.existsOverlappingBooking(
                request.vehicleId(), request.startDate(), request.endDate(), BLOCKING_STATUSES
        )) {
            throw new BadRequestException("Vehicle already has a booking overlapping these dates");
        }

        long days = ChronoUnit.DAYS.between(request.startDate(), request.endDate());
        if (days < 1) {
            days = 1;
        }
        BigDecimal totalAmount = request.dailyRate()
                .multiply(BigDecimal.valueOf(days))
                .setScale(2, RoundingMode.HALF_UP);

        RentalBooking booking = RentalBooking.builder()
                .customerId(customerId)
                .ownerId(request.ownerId())
                .vehicleId(request.vehicleId())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .pickupAddress(request.pickupAddress())
                .returnAddress(request.returnAddress())
                .status(RentalStatus.REQUESTED)
                .dailyRate(request.dailyRate())
                .totalAmount(totalAmount)
                .build();

        booking = rentalBookingRepository.save(booking);

        Map<String, Object> payload = new HashMap<>();
        payload.put("rentalId", booking.getId());
        payload.put("customerId", booking.getCustomerId());
        payload.put("ownerId", booking.getOwnerId());
        payload.put("vehicleId", booking.getVehicleId());
        payload.put("startDate", booking.getStartDate().toString());
        payload.put("endDate", booking.getEndDate().toString());
        payload.put("dailyRate", booking.getDailyRate());
        payload.put("totalAmount", booking.getTotalAmount());
        eventPublisher.publish(DomainEvent.of(DomainEvent.RENTAL_REQUESTED, String.valueOf(booking.getId()), payload));

        return booking;
    }

    @Transactional(readOnly = true)
    public List<RentalBooking> myBookings(Long userId) {
        return rentalBookingRepository.findByCustomerIdOrOwnerIdOrderByCreatedAtDesc(userId, userId);
    }

    @Transactional(readOnly = true)
    public List<RentalBooking> ownerPending(Long ownerId) {
        return rentalBookingRepository.findByOwnerIdAndStatusOrderByCreatedAtDesc(ownerId, RentalStatus.REQUESTED);
    }

    @Transactional(readOnly = true)
    public RentalBooking getById(Long id) {
        return rentalBookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rental booking not found: " + id));
    }

    @Transactional
    public RentalBooking confirm(Long id, Long ownerId) {
        RentalBooking booking = getById(id);
        requireOwner(booking, ownerId);
        if (booking.getStatus() != RentalStatus.REQUESTED) {
            throw new BadRequestException("Only REQUESTED bookings can be confirmed");
        }
        booking.setStatus(RentalStatus.CONFIRMED);
        booking = rentalBookingRepository.save(booking);

        Map<String, Object> payload = Map.of(
                "rentalId", booking.getId(),
                "customerId", booking.getCustomerId(),
                "ownerId", booking.getOwnerId(),
                "vehicleId", booking.getVehicleId()
        );
        eventPublisher.publish(DomainEvent.of(DomainEvent.RENTAL_CONFIRMED, String.valueOf(booking.getId()), payload));

        return booking;
    }

    @Transactional
    public RentalBooking pickup(Long id, Long customerId) {
        RentalBooking booking = getById(id);
        requireCustomer(booking, customerId);
        if (booking.getStatus() != RentalStatus.CONFIRMED) {
            throw new BadRequestException("Only CONFIRMED bookings can be picked up");
        }
        booking.setStatus(RentalStatus.PICKED_UP);
        booking = rentalBookingRepository.save(booking);

        Map<String, Object> payload = Map.of(
                "rentalId", booking.getId(),
                "customerId", booking.getCustomerId(),
                "ownerId", booking.getOwnerId(),
                "vehicleId", booking.getVehicleId()
        );
        eventPublisher.publish(DomainEvent.of(DomainEvent.RENTAL_PICKED_UP, String.valueOf(booking.getId()), payload));

        return booking;
    }

    @Transactional
    public RentalBooking returnVehicle(Long id, Long customerId) {
        RentalBooking booking = getById(id);
        requireCustomer(booking, customerId);
        if (booking.getStatus() != RentalStatus.PICKED_UP) {
            throw new BadRequestException("Only PICKED_UP bookings can be returned");
        }
        booking.setStatus(RentalStatus.RETURNED);
        booking = rentalBookingRepository.save(booking);

        Map<String, Object> payload = new HashMap<>();
        payload.put("rentalId", booking.getId());
        payload.put("customerId", booking.getCustomerId());
        payload.put("ownerId", booking.getOwnerId());
        payload.put("vehicleId", booking.getVehicleId());
        payload.put("amount", booking.getTotalAmount());
        eventPublisher.publish(DomainEvent.of(DomainEvent.RENTAL_RETURNED, String.valueOf(booking.getId()), payload));

        return booking;
    }

    @Transactional
    public RentalBooking cancel(Long id, Long userId) {
        RentalBooking booking = getById(id);
        boolean isCustomer = booking.getCustomerId().equals(userId);
        boolean isOwner = booking.getOwnerId().equals(userId);
        if (!isCustomer && !isOwner) {
            throw new ForbiddenException("Only the customer or owner can cancel this booking");
        }
        if (booking.getStatus() == RentalStatus.RETURNED || booking.getStatus() == RentalStatus.CANCELLED) {
            throw new BadRequestException("Booking cannot be cancelled in status " + booking.getStatus());
        }
        if (booking.getStatus() == RentalStatus.PICKED_UP) {
            throw new BadRequestException("Cannot cancel a PICKED_UP booking; use return instead");
        }
        booking.setStatus(RentalStatus.CANCELLED);
        return rentalBookingRepository.save(booking);
    }

    private void requireOwner(RentalBooking booking, Long ownerId) {
        if (!booking.getOwnerId().equals(ownerId)) {
            throw new ForbiddenException("Only the vehicle owner can perform this action");
        }
    }

    private void requireCustomer(RentalBooking booking, Long customerId) {
        if (!booking.getCustomerId().equals(customerId)) {
            throw new ForbiddenException("Only the customer can perform this action");
        }
    }
}
