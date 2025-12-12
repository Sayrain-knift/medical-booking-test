package com.sayrain.medicalbooking.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.sayrain.medicalbooking.dto.AuthResponse;
import com.sayrain.medicalbooking.dto.LoginRequest;
import com.sayrain.medicalbooking.dto.RegisterRequest;
import com.sayrain.medicalbooking.service.impl.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthServiceImpl authService;

    @PostMapping("/register")
    @SentinelResource(value = "register", blockHandler = "registerBlockHandler", fallback = "registerFallback")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest registerRequest) {
        try {
            // 修正：registerUser返回String消息，不是User对象
            String message = authService.registerUser(registerRequest);

            // 注册成功后自动登录
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername(registerRequest.getUsername());
            loginRequest.setPassword(registerRequest.getPassword());

            // 修正：loginUser返回AuthResponse，包含token
            AuthResponse authResponse = authService.loginUser(loginRequest);
            authResponse.setMessage("User registered successfully!");

            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (IllegalArgumentException e) {
            // 处理业务逻辑异常（如用户名已存在）
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(null, null, null, e.getMessage()));
        } catch (Exception e) {
            // 处理数据库约束异常
            String errorMessage = e.getMessage();
            if (errorMessage != null && errorMessage.contains("Unique index or primary key violation") && errorMessage.contains("USERNAME")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(new AuthResponse(null, null, null, "用户名已存在"));
            }
            // 其他异常
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new AuthResponse(null, null, null, "Registration failed. Please try again."));
        }
    }

    @PostMapping("/login")
    @SentinelResource(value = "login", blockHandler = "loginBlockHandler", fallback = "loginFallback")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        try {
            // 修正：直接返回loginUser的结果
            AuthResponse authResponse = authService.loginUser(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new AuthResponse(null, null, null, e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @SentinelResource(value = "logout", blockHandler = "logoutBlockHandler", fallback = "logoutFallback")
    public ResponseEntity<String> logout(@RequestHeader("Authorization") String authHeader) {
        try {
            // 从Authorization header中提取token
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String result = authService.logout(token);
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid or missing Authorization header");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Logout failed: " + e.getMessage());
        }
    }

    @GetMapping("/validate-token")
    @SentinelResource(value = "validateToken", blockHandler = "validateTokenBlockHandler", fallback = "validateTokenFallback")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            // 从Authorization header中提取token
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                boolean isValid = authService.isTokenValid(token);
                return ResponseEntity.ok(isValid);
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(false);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(false);
        }
    }

    @PostMapping("/refresh-cache")
    @SentinelResource(value = "refreshCache", blockHandler = "refreshCacheBlockHandler", fallback = "refreshCacheFallback")
    public ResponseEntity<String> refreshUserCache(@RequestParam String username) {
        try {
            authService.refreshUserCache(username);
            return ResponseEntity.ok("User cache refreshed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to refresh user cache: " + e.getMessage());
        }
    }

    // Sentinel流控处理方法
    public ResponseEntity<AuthResponse> registerBlockHandler(RegisterRequest registerRequest, 
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new AuthResponse(null, null, null, "注册请求过于频繁，请稍后再试"));
    }

    public ResponseEntity<AuthResponse> loginBlockHandler(LoginRequest loginRequest, 
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new AuthResponse(null, null, null, "登录请求过于频繁，请稍后再试"));
    }

    public ResponseEntity<String> logoutBlockHandler(String authHeader, 
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("请求过于频繁，请稍后再试");
    }

    public ResponseEntity<Boolean> validateTokenBlockHandler(String authHeader, 
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body(false);
    }

    public ResponseEntity<String> refreshCacheBlockHandler(String username, 
            com.alibaba.csp.sentinel.slots.block.BlockException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("请求过于频繁，请稍后再试");
    }

    // Sentinel降级处理方法
    public ResponseEntity<AuthResponse> registerFallback(RegisterRequest registerRequest, Throwable ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponse(null, null, null, "注册服务暂时不可用，请稍后再试"));
    }

    public ResponseEntity<AuthResponse> loginFallback(LoginRequest loginRequest, Throwable ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new AuthResponse(null, null, null, "登录服务暂时不可用，请稍后再试"));
    }

    public ResponseEntity<String> logoutFallback(String authHeader, Throwable ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("服务暂时不可用，请稍后再试");
    }

    public ResponseEntity<Boolean> validateTokenFallback(String authHeader, Throwable ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(false);
    }

    public ResponseEntity<String> refreshCacheFallback(String username, Throwable ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("服务暂时不可用，请稍后再试");
    }
}