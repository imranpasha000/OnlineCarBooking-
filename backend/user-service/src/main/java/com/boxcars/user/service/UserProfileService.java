package com.boxcars.user.service;

import com.boxcars.user.dto.DriverOnlineRequest;
import com.boxcars.user.dto.KycUpdateRequest;
import com.boxcars.user.dto.SyncUserRequest;
import com.boxcars.user.dto.UpdateProfileRequest;
import com.boxcars.user.entity.UserProfile;
import com.boxcars.user.exception.ResourceNotFoundException;
import com.boxcars.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;

    @Transactional
    public UserProfile sync(SyncUserRequest request) {
        UserProfile profile = userProfileRepository.findById(request.userId())
                .orElseGet(() -> UserProfile.builder().id(request.userId()).build());

        profile.setUsername(request.username());
        profile.setEmail(request.email());
        profile.setPhone(request.phone());
        if (request.roles() != null) {
            profile.setRoles(new HashSet<>(request.roles()));
        }
        if (profile.getDisplayName() == null || profile.getDisplayName().isBlank()) {
            profile.setDisplayName(request.username());
        }
        return userProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public UserProfile getById(Long id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User profile not found: " + id));
    }

    @Transactional
    public UserProfile updateMe(Long userId, UpdateProfileRequest request) {
        UserProfile profile = getById(userId);
        if (request.displayName() != null) {
            profile.setDisplayName(request.displayName());
        }
        if (request.phone() != null) {
            profile.setPhone(request.phone());
        }
        if (request.sellerType() != null) {
            profile.setSellerType(request.sellerType());
        }
        return userProfileRepository.save(profile);
    }

    @Transactional
    public UserProfile updateDriverOnline(Long userId, DriverOnlineRequest request) {
        UserProfile profile = getById(userId);
        profile.setDriverOnline(Boolean.TRUE.equals(request.online()));
        if (request.lat() != null) {
            profile.setDriverLat(request.lat());
        }
        if (request.lng() != null) {
            profile.setDriverLng(request.lng());
        }
        return userProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public List<UserProfile> listOnlineDrivers() {
        return userProfileRepository.findByDriverOnlineTrue();
    }

    @Transactional
    public UserProfile updateKyc(Long id, KycUpdateRequest request) {
        UserProfile profile = getById(id);
        profile.setKycVerified(Boolean.TRUE.equals(request.kycVerified()));
        return userProfileRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public List<UserProfile> listAll() {
        return userProfileRepository.findAll();
    }
}
