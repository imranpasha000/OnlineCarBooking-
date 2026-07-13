package com.boxcars.user.dto;

import com.boxcars.user.entity.SellerType;

public record UpdateProfileRequest(
        String displayName,
        String phone,
        SellerType sellerType
) {}
