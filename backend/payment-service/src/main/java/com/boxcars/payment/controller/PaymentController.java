package com.boxcars.payment.controller;

import com.boxcars.payment.dto.AuthorizeRequest;
import com.boxcars.payment.dto.CaptureRequest;
import com.boxcars.payment.dto.RefundRequest;
import com.boxcars.payment.entity.Payment;
import com.boxcars.payment.service.PaymentService;
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

@RestController
@RequestMapping("/api/payments")
@CrossOrigin
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/authorize")
    @ResponseStatus(HttpStatus.CREATED)
    public Payment authorize(@Valid @RequestBody AuthorizeRequest request) {
        return paymentService.authorize(request);
    }

    @PostMapping("/capture")
    public Payment capture(@RequestBody CaptureRequest request) {
        return paymentService.capture(request);
    }

    @PostMapping("/refund")
    public Payment refund(@Valid @RequestBody RefundRequest request) {
        return paymentService.refund(request.getPaymentId());
    }

    @GetMapping("/{id}")
    public Payment getById(@PathVariable Long id) {
        return paymentService.getById(id);
    }

    @GetMapping("/me")
    public List<Payment> me(@RequestHeader("X-User-Id") Long userId) {
        return paymentService.listForUser(userId);
    }
}
