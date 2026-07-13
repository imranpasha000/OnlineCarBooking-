package com.boxcars.auth.config;

import com.boxcars.auth.entity.Role;
import com.boxcars.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private static final List<String> DEFAULT_ROLES = List.of(
            "ROLE_RIDER",
            "ROLE_DRIVER",
            "ROLE_OWNER",
            "ROLE_ADMIN"
    );

    private final RoleRepository roleRepository;

    @Override
    public void run(ApplicationArguments args) {
        for (String roleName : DEFAULT_ROLES) {
            roleRepository.findByName(roleName).orElseGet(() -> roleRepository.save(new Role(roleName)));
        }
    }
}
