package com.example.buddyfinder_backend.config;

import com.example.buddyfinder_backend.entity.User;
import com.example.buddyfinder_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
@Slf4j
public class SuperAdminInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        long superAdmins = userRepository.countByIsSuperAdminTrue();
        if (superAdmins > 0) {
            return;
        }

        log.warn("No SUPER_ADMIN accounts found. Creating default super admin...");

        String email = "super_admin@buddyfinder.com";
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> User.builder()
                        .name("Super Admin")
                        .email(email)
                        .password(passwordEncoder.encode("123456"))
                        .age(30)
                        .gender("Female")
                        .interests("Leadership, Fitness, Strategy")
                        .location("Toronto, ON")
                        .availability("Flexible")
                        .tier(User.TierType.ELITE)
                        .isVerified(true)
                        .isActive(true)
                        .build());

        user.setIsAdmin(true);
        user.setIsSuperAdmin(true);

        userRepository.save(user);
        log.info("Default SUPER_ADMIN account is ready (email: {}, password: 123456)", email);
    }
}
