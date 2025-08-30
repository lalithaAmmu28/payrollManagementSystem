package com.pms.backend.controller;

import com.pms.backend.dto.ApiResponse;
import com.pms.backend.dto.employee.EmployeeResponse;
import com.pms.backend.dto.user.PasswordChangeRequest;
import com.pms.backend.dto.user.UserProfileUpdateRequest;
import com.pms.backend.entity.User;
import com.pms.backend.exception.ResourceNotFoundException;
import com.pms.backend.repository.UserRepository;
import com.pms.backend.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "User Profile & Self-Service", description = "Self-service endpoints for authenticated users")
@SecurityRequirement(name = "Bearer Authentication")
public class UserController {

    private final EmployeeService employeeService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(EmployeeService employeeService, UserRepository userRepository) {
        this.employeeService = employeeService;
        this.userRepository = userRepository;
    }

    @GetMapping("/users/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user profile", 
               description = "Get the profile information of the currently authenticated user")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getCurrentUserProfile() {
        
        String currentUserId = getCurrentUserId();
        EmployeeResponse employee = employeeService.getEmployeeByUserId(currentUserId);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "User profile retrieved successfully", employee));
    }

    @PatchMapping("/employees/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update own profile", 
               description = "Update the profile information of the currently authenticated employee")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateOwnProfile(
            @Valid @RequestBody UserProfileUpdateRequest request) {
        
        String currentUserId = getCurrentUserId();
        EmployeeResponse employee = employeeService.updateEmployeeProfile(currentUserId, request);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated successfully", employee));
    }

    @PatchMapping("/users/me/password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Change password", 
               description = "Change the password of the currently authenticated user")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody PasswordChangeRequest request) {
        
        String currentUserId = getCurrentUserId();
        employeeService.changePassword(currentUserId, request);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully", null));
    }
    
    /**
     * Helper method to get the current user's ID from the security context
     */
    private String getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || authentication.getName() == null) {
            throw new RuntimeException("No authenticated user found");
        }
        
        // The authentication name contains the username, we need to find the user ID
        // We'll use the EmployeeService to get the user by username
        String username = authentication.getName();
        
        // For this implementation, we need to get user ID from the username
        // We can create a service method for this or use UserRepository directly
        // For now, let's assume we have a way to get userId from username
        
        return getUserIdByUsername(username);
    }
    
    /**
     * Helper method to get user ID by username
     */
    private String getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        return user.getUserId();
    }
}