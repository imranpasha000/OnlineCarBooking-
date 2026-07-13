package com.boxcars.payment.service;

import com.boxcars.payment.entity.PaymentProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Phase 3 stub: falls back to mock when stripe.api-key is sk_test_mock.
 */
@Component
@Slf4j
public class StripePaymentClient {

    private final String apiKey;

    public StripePaymentClient(@Value("${stripe.api-key:sk_test_mock}") String apiKey) {
        this.apiKey = apiKey;
    }

    public boolean isMockMode() {
        return "sk_test_mock".equals(apiKey) || apiKey == null || apiKey.isBlank();
    }

    public AuthResult authorize(Long payerId, BigDecimal amount, String currency) {
        if (isMockMode()) {
            String ref = "mock_auth_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            log.info("Mock authorize payerId={} amount={} {}", payerId, amount, currency);
            return new AuthResult(PaymentProvider.MOCK, ref);
        }
        // Phase 3: real Stripe Integration — stub falls back to mock-shaped ref
        log.warn("Stripe authorize stub (key present) — using mock provider flow for amount={}", amount);
        String ref = "stripe_stub_" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        return new AuthResult(PaymentProvider.STRIPE, ref);
    }

    public String capture(String providerRef) {
        if (isMockMode()) {
            log.info("Mock capture providerRef={}", providerRef);
            return providerRef;
        }
        log.warn("Stripe capture stub for providerRef={}", providerRef);
        return providerRef;
    }

    public String refund(String providerRef) {
        if (isMockMode()) {
            log.info("Mock refund providerRef={}", providerRef);
            return providerRef;
        }
        log.warn("Stripe refund stub for providerRef={}", providerRef);
        return providerRef;
    }

    public record AuthResult(PaymentProvider provider, String providerRef) {
    }
}
