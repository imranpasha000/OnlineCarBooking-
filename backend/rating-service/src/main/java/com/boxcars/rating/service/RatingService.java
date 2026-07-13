package com.boxcars.rating.service;

import com.boxcars.rating.dto.CreateRatingRequest;
import com.boxcars.rating.entity.Rating;
import com.boxcars.rating.entity.ReferenceType;
import com.boxcars.rating.exception.BadRequestException;
import com.boxcars.rating.repository.RatingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;

    @Transactional
    public Rating create(Long fromUserId, CreateRatingRequest request) {
        if (fromUserId.equals(request.getToUserId())) {
            throw new BadRequestException("Cannot rate yourself");
        }

        Rating rating = Rating.builder()
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .fromUserId(fromUserId)
                .toUserId(request.getToUserId())
                .score(request.getScore())
                .comment(request.getComment())
                .build();

        return ratingRepository.save(rating);
    }

    @Transactional(readOnly = true)
    public List<Rating> listForUser(Long userId) {
        return ratingRepository.findByToUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Rating> listForReference(ReferenceType type, Long id) {
        return ratingRepository.findByReferenceTypeAndReferenceIdOrderByCreatedAtDesc(type, id);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> averageForUser(Long userId) {
        Double average = ratingRepository.averageScoreForUser(userId);
        List<Rating> ratings = ratingRepository.findByToUserIdOrderByCreatedAtDesc(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("average", average != null ? average : 0.0);
        result.put("count", ratings.size());
        return result;
    }
}
