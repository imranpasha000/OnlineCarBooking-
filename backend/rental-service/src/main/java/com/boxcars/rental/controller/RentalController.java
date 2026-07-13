package com.boxcars.rental.controller;

import com.boxcars.rental.dto.BookRentalRequest;
import com.boxcars.rental.dto.RentalSearchResponse;
import com.boxcars.rental.entity.RentalBooking;
import com.boxcars.rental.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rentals")
@CrossOrigin
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @GetMapping("/search")
    public RentalSearchResponse search(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return rentalService.search(startDate, endDate);
    }

    @PostMapping("/book")
    @ResponseStatus(HttpStatus.CREATED)
    public RentalBooking book(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BookRentalRequest request
    ) {
        return rentalService.book(userId, request);
    }

    @GetMapping("/me")
    public List<RentalBooking> myBookings(@RequestHeader("X-User-Id") Long userId) {
        return rentalService.myBookings(userId);
    }

    @GetMapping("/owner/pending")
    public List<RentalBooking> ownerPending(@RequestHeader("X-User-Id") Long userId) {
        return rentalService.ownerPending(userId);
    }

    @GetMapping("/{id}")
    public RentalBooking getById(@PathVariable Long id) {
        return rentalService.getById(id);
    }

    @PostMapping("/{id}/confirm")
    public RentalBooking confirm(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return rentalService.confirm(id, userId);
    }

    @PostMapping("/{id}/pickup")
    public RentalBooking pickup(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return rentalService.pickup(id, userId);
    }

    @PostMapping("/{id}/return")
    public RentalBooking returnVehicle(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return rentalService.returnVehicle(id, userId);
    }

    @PostMapping("/{id}/cancel")
    public RentalBooking cancel(
            @PathVariable Long id,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return rentalService.cancel(id, userId);
    }
}
