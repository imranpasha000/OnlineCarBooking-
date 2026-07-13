package com.boxcars.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RefundRequest {

    @NotNull(message = "paymentId is required")
    private Long paymentId;
}
