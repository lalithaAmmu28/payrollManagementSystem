package com.pms.backend.dto.auth;

import com.pms.backend.entity.enums.Role;

public class JwtAuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private UserInfo user;

    // Constructors
    public JwtAuthResponse() {}
    
    public JwtAuthResponse(String accessToken, String tokenType, UserInfo user) {
        this.accessToken = accessToken;
        this.tokenType = tokenType;
        this.user = user;
    }

    public JwtAuthResponse(String accessToken, UserInfo user) {
        this.accessToken = accessToken;
        this.user = user;
    }
    
    // Getters and Setters
    public String getAccessToken() {
        return accessToken;
    }
    
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
    
    public UserInfo getUser() {
        return user;
    }
    
    public void setUser(UserInfo user) {
        this.user = user;
    }

    public static class UserInfo {
        private String userId;
        private String username;
        private String email;
        private Role role;
        
        // Constructors
        public UserInfo() {}
        
        public UserInfo(String userId, String username, String email, Role role) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.role = role;
        }
        
        // Getters and Setters
        public String getUserId() {
            return userId;
        }
        
        public void setUserId(String userId) {
            this.userId = userId;
        }
        
        public String getUsername() {
            return username;
        }
        
        public void setUsername(String username) {
            this.username = username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public void setEmail(String email) {
            this.email = email;
        }
        
        public Role getRole() {
            return role;
        }
        
        public void setRole(Role role) {
            this.role = role;
        }
    }
}