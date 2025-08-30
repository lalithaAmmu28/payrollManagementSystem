package com.pms.backend.controller;

import com.pms.backend.dto.ApiResponse;
import com.pms.backend.dto.auth.JwtAuthResponse;
import com.pms.backend.dto.auth.LoginRequest;
import com.pms.backend.entity.User;
import com.pms.backend.repository.UserRepository;
import com.pms.backend.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Handles login API [/api/v1/auth]
 */
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @Autowired
    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            System.out.println("Login attempt for username: " + loginRequest.getUsername());
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(authentication);

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            JwtAuthResponse.UserInfo userInfo = new JwtAuthResponse.UserInfo(
                    user.getUserId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole()
            );

            JwtAuthResponse jwtResponse = new JwtAuthResponse(token, userInfo);

            return ResponseEntity.ok(new ApiResponse<>(
                    true,
                    "Login successful",
                    jwtResponse
            ));

        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(new ApiResponse<>(
                    false,
                    "Invalid username or password",
                    null
            ));
        }
    }
}
