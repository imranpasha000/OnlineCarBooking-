package com.boxcars.vehicle.config;

import com.boxcars.vehicle.entity.Vehicle;
import com.boxcars.vehicle.entity.VehicleStatus;
import com.boxcars.vehicle.entity.VehicleType;
import com.boxcars.vehicle.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final VehicleRepository vehicleRepository;

    @Override
    public void run(String... args) {
        if (vehicleRepository.count() > 0) {
            return;
        }

        List<Vehicle> samples = List.of(
                Vehicle.builder()
                        .ownerId(1L)
                        .name("City Cruiser")
                        .brand("Toyota")
                        .model("Corolla")
                        .year(2022)
                        .plateNumber("RENT-001")
                        .type(VehicleType.RENTAL)
                        .pricePerDay(new BigDecimal("45.00"))
                        .pricePerKm(new BigDecimal("0.80"))
                        .status(VehicleStatus.AVAILABLE)
                        .imageUrl("https://images.boxcars.local/corolla.jpg")
                        .fuelType("Petrol")
                        .transmission("Automatic")
                        .seats(5)
                        .description("Reliable compact sedan for city rentals.")
                        .lat(12.9716)
                        .lng(77.5946)
                        .build(),
                Vehicle.builder()
                        .ownerId(1L)
                        .name("Family SUV")
                        .brand("Hyundai")
                        .model("Creta")
                        .year(2023)
                        .plateNumber("RENT-002")
                        .type(VehicleType.RENTAL)
                        .pricePerDay(new BigDecimal("65.00"))
                        .pricePerKm(new BigDecimal("1.10"))
                        .status(VehicleStatus.AVAILABLE)
                        .imageUrl("https://images.boxcars.local/creta.jpg")
                        .fuelType("Diesel")
                        .transmission("Automatic")
                        .seats(5)
                        .description("Spacious SUV ideal for weekend getaways.")
                        .lat(12.9352)
                        .lng(77.6245)
                        .build(),
                Vehicle.builder()
                        .ownerId(1L)
                        .name("Luxury Sedan")
                        .brand("Honda")
                        .model("Civic")
                        .year(2024)
                        .plateNumber("RENT-003")
                        .type(VehicleType.RENTAL)
                        .pricePerDay(new BigDecimal("75.00"))
                        .pricePerKm(new BigDecimal("1.25"))
                        .status(VehicleStatus.AVAILABLE)
                        .imageUrl("https://images.boxcars.local/civic.jpg")
                        .fuelType("Petrol")
                        .transmission("Manual")
                        .seats(5)
                        .description("Premium feel for business and leisure rentals.")
                        .lat(13.0827)
                        .lng(80.2707)
                        .build(),
                Vehicle.builder()
                        .ownerId(1L)
                        .name("Quick Ride Hatch")
                        .brand("Maruti")
                        .model("Swift")
                        .year(2021)
                        .plateNumber("RIDE-001")
                        .type(VehicleType.RIDE)
                        .pricePerDay(null)
                        .pricePerKm(new BigDecimal("1.50"))
                        .status(VehicleStatus.AVAILABLE)
                        .imageUrl("https://images.boxcars.local/swift.jpg")
                        .fuelType("Petrol")
                        .transmission("Manual")
                        .seats(4)
                        .description("Efficient hatchback for short urban rides.")
                        .lat(12.9719)
                        .lng(77.6412)
                        .build(),
                Vehicle.builder()
                        .ownerId(1L)
                        .name("Comfort Ride Sedan")
                        .brand("Honda")
                        .model("City")
                        .year(2022)
                        .plateNumber("RIDE-002")
                        .type(VehicleType.RIDE)
                        .pricePerDay(null)
                        .pricePerKm(new BigDecimal("1.80"))
                        .status(VehicleStatus.AVAILABLE)
                        .imageUrl("https://images.boxcars.local/city.jpg")
                        .fuelType("Petrol")
                        .transmission("Automatic")
                        .seats(4)
                        .description("Comfortable sedan for airport and city trips.")
                        .lat(12.9141)
                        .lng(77.6411)
                        .build()
        );

        vehicleRepository.saveAll(samples);
        log.info("Seeded {} sample vehicles for ownerId=1", samples.size());
    }
}
