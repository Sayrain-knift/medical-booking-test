package com.sayrain.medicalbooking.service;

import com.sayrain.medicalbooking.dto.AuthResponse;
import com.sayrain.medicalbooking.dto.LoginRequest;
import com.sayrain.medicalbooking.dto.RegisterRequest;

public interface AuthService {
    String registerUser(RegisterRequest registerRequest);
    AuthResponse loginUser(LoginRequest loginRequest);
    String logout(String token);
    boolean isTokenValid(String token);
    void refreshUserCache(String username);
}