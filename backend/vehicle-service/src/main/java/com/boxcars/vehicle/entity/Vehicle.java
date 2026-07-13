package com.boxcars.vehicle.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String brand;

    @Column(length = 50)
    private String model;

    private Integer year;

    @Column(length = 20)
    private String plateNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VehicleType type;

    @Column(precision = 12, scale = 2)
    private BigDecimal pricePerDay;

    @Column(precision = 12, scale = 2)
    private BigDecimal pricePerKm;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 30)
    private String fuelType;

    @Column(length = 30)
    private String transmission;

    private Integer seats;

    @Column(length = 1000)
    private String description;

    private Double lat;

    private Double lng;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
        if (status == null) {
            status = VehicleStatus.AVAILABLE;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }
}
