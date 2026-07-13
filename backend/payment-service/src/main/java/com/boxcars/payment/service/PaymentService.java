package com.boxcars.payment.service;

import com.boxcars.common.event.DomainEvent;
import com.boxcars.payment.config.RabbitConfig;
import com.boxcars.payment.dto.AuthorizeRequest;
import com.boxcars.payment.dto.CaptureRequest;
import com.boxcars.payment.entity.Payment;
import com.boxcars.payment.entity.PaymentStatus;
import com.boxcars.payment.entity.ReferenceType;
import com.boxcars.payment.exception.BadRequestException;
import com.boxcars.payment.exception.ResourceNotFoundException;
import com.boxcars.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final StripePaymentClient stripePaymentClient;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public Payment authorize(AuthorizeRequest request) {
        StripePaymentClient.AuthResult auth = stripePaymentClient.authorize(
                request.getPayerId(), request.getAmount(), "INR");

        Payment payment = Payment.builder()
                .referenceType(request.getReferenceType())
                .referenceId(request.getReferenceId())
                .payerId(request.getPayerId())
                .payeeId(request.getPayeeId())
                .amount(request.getAmount())
                .currency("INR")
                .status(PaymentStatus.AUTHORIZED)
                .provider(auth.provider())
                .providerRef(auth.providerRef())
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment capture(CaptureRequest request) {
        Payment payment = resolvePayment(request);

        if (payment.getStatus() == PaymentStatus.CAPTURED) {
            return payment;
        }
        if (payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new BadRequestException("Payment must be AUTHORIZED to capture, current=" + payment.getStatus());
        }

        stripePaymentClient.capture(payment.getProviderRef());
        payment.setStatus(PaymentStatus.CAPTURED);
        Payment saved = paymentRepository.save(payment);
        publishPaymentCaptured(saved);
        return saved;
    }

    @Transactional
    public Payment refund(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            return payment;
        }
        if (payment.getStatus() != PaymentStatus.CAPTURED && payment.getStatus() != PaymentStatus.AUTHORIZED) {
            throw new BadRequestException("Cannot refund payment in status " + payment.getStatus());
        }

        stripePaymentClient.refund(payment.getProviderRef());
        payment.setStatus(PaymentStatus.REFUNDED);
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Payment getById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<Payment> listForUser(Long userId) {
        return paymentRepository.findByPayerIdOrPayeeIdOrderByCreatedAtDesc(userId, userId);
    }

    @Transactional
    public Payment autoAuthorizeAndCapture(
            ReferenceType referenceType,
            Long referenceId,
            Long payerId,
            Long payeeId,
            BigDecimal amount
    ) {
        Payment existing = paymentRepository.findByReferenceTypeAndReferenceId(referenceType, referenceId)
                .orElse(null);
        if (existing != null) {
            if (existing.getStatus() == PaymentStatus.CAPTURED) {
                return existing;
            }
            if (existing.getStatus() == PaymentStatus.AUTHORIZED) {
                CaptureRequest captureRequest = new CaptureRequest();
                captureRequest.setPaymentId(existing.getId());
                return capture(captureRequest);
            }
        }

        AuthorizeRequest authorizeRequest = new AuthorizeRequest();
        authorizeRequest.setReferenceType(referenceType);
        authorizeRequest.setReferenceId(referenceId);
        authorizeRequest.setPayerId(payerId);
        authorizeRequest.setPayeeId(payeeId);
        authorizeRequest.setAmount(amount);

        Payment authorized = authorize(authorizeRequest);
        CaptureRequest captureRequest = new CaptureRequest();
        captureRequest.setPaymentId(authorized.getId());
        return capture(captureRequest);
    }

    private Payment resolvePayment(CaptureRequest request) {
        if (request.getPaymentId() != null) {
            return paymentRepository.findById(request.getPaymentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Payment not found: " + request.getPaymentId()));
        }
        if (request.getReferenceType() != null && request.getReferenceId() != null) {
            return paymentRepository.findByReferenceTypeAndReferenceId(
                            request.getReferenceType(), request.getReferenceId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Payment not found for " + request.getReferenceType() + "/" + request.getReferenceId()));
        }
        throw new BadRequestException("Provide paymentId or referenceType+referenceId");
    }

    private void publishPaymentCaptured(Payment payment) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", payment.getId());
        payload.put("referenceType", payment.getReferenceType().name());
        payload.put("referenceId", payment.getReferenceId());
        payload.put("payerId", payment.getPayerId());
        payload.put("payeeId", payment.getPayeeId());
        payload.put("amount", payment.getAmount());
        payload.put("currency", payment.getCurrency());

        DomainEvent event = DomainEvent.of(
                DomainEvent.PAYMENT_CAPTURED,
                String.valueOf(payment.getId()),
                payload
        );
        rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE, DomainEvent.PAYMENT_CAPTURED, event);
        log.info("Published PAYMENT_CAPTURED for paymentId={}", payment.getId());
    }
}
