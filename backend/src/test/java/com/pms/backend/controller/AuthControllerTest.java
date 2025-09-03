package com.pms.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.backend.dto.auth.LoginRequest;
import com.pms.backend.entity.User;
import com.pms.backend.entity.enums.Role;
import com.pms.backend.repository.UserRepository;
import com.pms.backend.security.CustomUserDetailsService;
import com.pms.backend.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private AuthenticationManager authenticationManager;
    @MockBean private JwtTokenProvider jwtTokenProvider;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;

    @Test
    void testLogin_Success_ReturnsToken() throws Exception {
        // Given
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("password");

        Authentication auth = new UsernamePasswordAuthenticationToken("admin", "password");
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);
        when(jwtTokenProvider.generateToken(auth)).thenReturn("mock-jwt");

        User user = new User();
        user.setUserId("u1");
        user.setUsername("admin");
        user.setEmail("admin@example.com");
        user.setRole(Role.Admin);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").value("mock-jwt"))
                .andExpect(jsonPath("$.data.user.username").value("admin"));
    }

    @Test
    void testLogin_Failure_BadCredentials_Returns401() throws Exception {
        // Given
        LoginRequest req = new LoginRequest();
        req.setUsername("bad");
        req.setPassword("bad");

        when(authenticationManager.authenticate(any(Authentication.class)))
                .thenThrow(new BadCredentialsException("bad creds"));

        // When / Then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}


