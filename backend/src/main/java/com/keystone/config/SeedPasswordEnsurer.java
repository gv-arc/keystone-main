package com.keystone.config;

import com.keystone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SeedPasswordEnsurer implements ApplicationRunner {

    private static final String SEED_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        userRepository.findAll().forEach(user -> {
            if (!passwordEncoder.matches(SEED_PASSWORD, user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.encode(SEED_PASSWORD));
                userRepository.save(user);
                log.info("Reset password for seed user: {}", user.getEmail());
            }
        });
    }
}
