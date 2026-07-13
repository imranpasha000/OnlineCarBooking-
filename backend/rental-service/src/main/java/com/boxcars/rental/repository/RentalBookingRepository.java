package com.boxcars.rental.repository;

import com.boxcars.rental.entity.RentalBooking;
import com.boxcars.rental.entity.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RentalBookingRepository extends JpaRepository<RentalBooking, Long> {

    List<RentalBooking> findByCustomerIdOrOwnerIdOrderByCreatedAtDesc(Long customerId, Long ownerId);

    List<RentalBooking> findByOwnerIdAndStatusOrderByCreatedAtDesc(Long ownerId, RentalStatus status);

    @Query("""
            SELECT DISTINCT b.vehicleId FROM RentalBooking b
            WHERE b.status IN :statuses
              AND b.startDate <= :endDate
              AND b.endDate >= :startDate
            """)
    List<Long> findUnavailableVehicleIds(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<RentalStatus> statuses
    );

    @Query("""
            SELECT COUNT(b) > 0 FROM RentalBooking b
            WHERE b.vehicleId = :vehicleId
              AND b.status IN :statuses
              AND b.startDate <= :endDate
              AND b.endDate >= :startDate
            """)
    boolean existsOverlappingBooking(
            @Param("vehicleId") Long vehicleId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("statuses") List<RentalStatus> statuses
    );
}
