package com.fitness.userservice.service;

import com.fitness.userservice.dto.LoginRequest;
import com.fitness.userservice.dto.LoginResponse;
import com.fitness.userservice.dto.RegisterRequest;
import com.fitness.userservice.dto.UserResponse;
import com.fitness.userservice.model.Usermodel;
import com.fitness.userservice.repository.UserRepo;
import com.fitness.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

    private final UserRepo repository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserResponse register(RegisterRequest request) {
        // idempotent: calling /register twice with the same email no longer throws
        if (repository.existsByEmail(request.getEmail())) {
            return mapToResponse(repository.findByEmail(request.getEmail()));
        }

        Usermodel user = new Usermodel();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // hashed, never plain text
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        return mapToResponse(repository.save(user));
    }

    public LoginResponse login(LoginRequest request) {
        Usermodel user = repository.findByEmail(request.getEmail());

        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new LoginResponse(token, user.getId(), user.getEmail());
    }

    public UserResponse getUserProfile(String userid) {
        Usermodel user = repository.findById(userid)
                .orElseThrow(() -> new RuntimeException("user with id not found"));
        return mapToResponse(user);
    }

    public Boolean existByUserId(String userid) {
        log.info("calling user validation for user id {}", userid);
        return repository.existsById(userid);
    }

    private UserResponse mapToResponse(Usermodel user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());
        return response;
    }
}