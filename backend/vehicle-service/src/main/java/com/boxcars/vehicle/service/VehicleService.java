package com.boxcars.vehicle.service;

import com.boxcars.vehicle.dto.CreateVehicleRequest;
import com.boxcars.vehicle.dto.UpdateStatusRequest;
import com.boxcars.vehicle.dto.UpdateVehicleRequest;
import com.boxcars.vehicle.entity.Vehicle;
import com.boxcars.vehicle.entity.VehicleStatus;
import com.boxcars.vehicle.entity.VehicleType;
import com.boxcars.vehicle.exception.ForbiddenException;
import com.boxcars.vehicle.exception.ResourceNotFoundException;
import com.boxcars.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    @Transactional
    public Vehicle create(Long ownerId, CreateVehicleRequest request) {
        Vehicle vehicle = Vehicle.builder()
                .ownerId(ownerId)
                .name(request.name())
                .brand(request.brand())
                .model(request.model())
                .year(request.year())
                .plateNumber(request.plateNumber())
                .type(request.type())
                .pricePerDay(request.pricePerDay())
                .pricePerKm(request.pricePerKm())
                .status(VehicleStatus.AVAILABLE)
                .imageUrl(request.imageUrl())
                .fuelType(request.fuelType())
                .transmission(request.transmission())
                .seats(request.seats())
                .description(request.description())
                .lat(request.lat())
                .lng(request.lng())
                .build();
        return vehicleRepository.save(vehicle);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> list(Long userId, Set<String> roles, Long ownerIdFilter) {
        boolean admin = roles != null && roles.contains("ROLE_ADMIN");
        if (ownerIdFilter != null) {
            if (!admin && !ownerIdFilter.equals(userId)) {
                throw new ForbiddenException("Cannot list another owner's vehicles");
            }
            return vehicleRepository.findByOwnerId(ownerIdFilter);
        }
        if (admin) {
            return vehicleRepository.findAll();
        }
        return vehicleRepository.findByOwnerId(userId);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> search(VehicleType type, VehicleStatus status) {
        return vehicleRepository.search(matchingTypes(type), status);
    }

    @Transactional(readOnly = true)
    public List<Vehicle> available(VehicleType type) {
        return vehicleRepository.findAvailable(matchingTypes(type), VehicleStatus.AVAILABLE);
    }

    private List<VehicleType> matchingTypes(VehicleType type) {
        if (type == null) {
            return List.of(VehicleType.RIDE, VehicleType.RENTAL, VehicleType.BOTH);
        }
        if (type == VehicleType.BOTH) {
            return List.of(VehicleType.BOTH);
        }
        return List.of(type, VehicleType.BOTH);
    }

    @Transactional(readOnly = true)
    public Vehicle getById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found: " + id));
    }

    @Transactional
    public Vehicle update(Long id, Long userId, Set<String> roles, UpdateVehicleRequest request) {
        Vehicle vehicle = getById(id);
        requireOwnerOrAdmin(vehicle, userId, roles);

        if (request.name() != null) {
            vehicle.setName(request.name());
        }
        if (request.brand() != null) {
            vehicle.setBrand(request.brand());
        }
        if (request.model() != null) {
            vehicle.setModel(request.model());
        }
        if (request.year() != null) {
            vehicle.setYear(request.year());
        }
        if (request.plateNumber() != null) {
            vehicle.setPlateNumber(request.plateNumber());
        }
        if (request.type() != null) {
            vehicle.setType(request.type());
        }
        if (request.pricePerDay() != null) {
            vehicle.setPricePerDay(request.pricePerDay());
        }
        if (request.pricePerKm() != null) {
            vehicle.setPricePerKm(request.pricePerKm());
        }
        if (request.imageUrl() != null) {
            vehicle.setImageUrl(request.imageUrl());
        }
        if (request.fuelType() != null) {
            vehicle.setFuelType(request.fuelType());
        }
        if (request.transmission() != null) {
            vehicle.setTransmission(request.transmission());
        }
        if (request.seats() != null) {
            vehicle.setSeats(request.seats());
        }
        if (request.description() != null) {
            vehicle.setDescription(request.description());
        }
        if (request.lat() != null) {
            vehicle.setLat(request.lat());
        }
        if (request.lng() != null) {
            vehicle.setLng(request.lng());
        }
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle updateStatus(Long id, Long userId, Set<String> roles, UpdateStatusRequest request) {
        Vehicle vehicle = getById(id);
        requireOwnerOrAdmin(vehicle, userId, roles);
        vehicle.setStatus(request.status());
        return vehicleRepository.save(vehicle);
    }

    @Transactional
    public void delete(Long id, Long userId, Set<String> roles) {
        Vehicle vehicle = getById(id);
        requireOwnerOrAdmin(vehicle, userId, roles);
        vehicleRepository.delete(vehicle);
    }

    private void requireOwnerOrAdmin(Vehicle vehicle, Long userId, Set<String> roles) {
        boolean admin = roles != null && roles.contains("ROLE_ADMIN");
        if (!admin && !vehicle.getOwnerId().equals(userId)) {
            throw new ForbiddenException("Only the owner or admin can modify this vehicle");
        }
    }
}
