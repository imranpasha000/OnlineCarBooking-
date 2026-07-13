package com.boxcars.rating.repository;

import com.boxcars.rating.entity.Rating;
import com.boxcars.rating.entity.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    List<Rating> findByToUserIdOrderByCreatedAtDesc(Long toUserId);

    List<Rating> findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(ReferenceType referenceType, Long referenceId);

    @Query("select avg(r.score) from Rating r where r.toUserId = :userId")
    Double averageScoreForUser(@Param("userId") Long userId);
}
