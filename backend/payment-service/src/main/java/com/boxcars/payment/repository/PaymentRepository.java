package com.boxcars.payment.repository;

import com.boxcars.payment.entity.Payment;
import com.boxcars.payment.entity.ReferenceType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByReferenceTypeAndReferenceId(ReferenceType referenceType, Long referenceId);

    List<Payment> findByPayerIdOrPayeeIdOrderByCreatedAtDesc(Long payerId, Long payeeId);
}
