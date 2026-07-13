package com.boxcars.trip.repository;

import com.boxcars.trip.entity.Trip;
import com.boxcars.trip.entity.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TripRepository extends JpaRepository<Trip, Long> {

    List<Trip> findByRiderIdOrDriverIdOrderByCreatedAtDesc(Long riderId, Long driverId);

    List<Trip> findByStatusOrderByCreatedAtDesc(TripStatus status);

    Optional<Trip> findFirstByRiderIdAndStatusInOrderByCreatedAtDesc(Long riderId, List<TripStatus> statuses);

    Optional<Trip> findFirstByDriverIdAndStatusInOrderByCreatedAtDesc(Long driverId, List<TripStatus> statuses);
}
