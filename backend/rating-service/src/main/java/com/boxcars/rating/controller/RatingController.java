package com.boxcars.rating.controller;

import com.boxcars.rating.dto.CreateRatingRequest;
import com.boxcars.rating.entity.Rating;
import com.boxcars.rating.entity.ReferenceType;
import com.boxcars.rating.service.RatingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
@CrossOrigin
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Rating create(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CreateRatingRequest request
    ) {
        return ratingService.create(userId, request);
    }

    @GetMapping("/user/{userId}")
    public List<Rating> listForUser(@PathVariable Long userId) {
        return ratingService.listForUser(userId);
    }

    @GetMapping("/reference/{type}/{id}")
    public List<Rating> listForReference(@PathVariable ReferenceType type, @PathVariable Long id) {
        return ratingService.listForReference(type, id);
    }

    @GetMapping("/user/{userId}/average")
    public Map<String, Object> average(@PathVariable Long userId) {
        return ratingService.averageForUser(userId);
    }
}
