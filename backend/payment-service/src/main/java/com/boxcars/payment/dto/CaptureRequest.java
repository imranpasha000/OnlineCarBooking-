package com.boxcars.payment.dto;

import com.boxcars.payment.entity.ReferenceType;
import lombok.Data;

@Data
public class CaptureRequest {

    private Long paymentId;
    private ReferenceType referenceType;
    private Long referenceId;
}
