package com.boxcars.rating.dto;

import com.boxcars.rating.entity.ReferenceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateRatingRequest {

    @NotNull(message = "referenceType is required")
    private ReferenceType referenceType;

    @NotNull(message = "referenceId is required")
    private Long referenceId;

    @NotNull(message = "toUserId is required")
    private Long toUserId;

    @NotNull(message = "score is required")
    @Min(value = 1, message = "score must be at least 1")
    @Max(value = 5, message = "score must be at most 5")
    private Integer score;

    private String comment;
}
