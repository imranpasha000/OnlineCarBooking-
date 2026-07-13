package com.boxcars.vehicle.repository;

import com.boxcars.vehicle.entity.Vehicle;
import com.boxcars.vehicle.entity.VehicleStatus;
import com.boxcars.vehicle.entity.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByOwnerId(Long ownerId);

    @Query("""
            SELECT v FROM Vehicle v
            WHERE (:status IS NULL OR v.status = :status)
              AND v.type IN :types
            """)
    List<Vehicle> search(
            @Param("types") Collection<VehicleType> types,
            @Param("status") VehicleStatus status
    );

    @Query("""
            SELECT v FROM Vehicle v
            WHERE v.status = :available
              AND v.type IN :types
            """)
    List<Vehicle> findAvailable(
            @Param("types") Collection<VehicleType> types,
            @Param("available") VehicleStatus available
    );
}
