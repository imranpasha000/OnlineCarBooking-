package com.boxcars.payment.dto;

import com.boxcars.payment.entity.ReferenceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AuthorizeRequest {

    @NotNull(message = "referenceType is required")
    private ReferenceType referenceType;

    @NotNull(message = "referenceId is required")
    private Long referenceId;

    @NotNull(message = "payerId is required")
    private Long payerId;

    @NotNull(message = "payeeId is required")
    private Long payeeId;

    @NotNull(message = "amount is required")
    @DecimalMin(value = "0.01", message = "amount must be positive")
    private BigDecimal amount;
}
