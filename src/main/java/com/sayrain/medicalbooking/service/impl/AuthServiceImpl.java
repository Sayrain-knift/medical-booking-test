package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.dto.AuthResponse;
import com.sayrain.medicalbooking.dto.LoginRequest;
import com.sayrain.medicalbooking.dto.RegisterRequest;
import com.sayrain.medicalbooking.model.User;
import com.sayrain.medicalbooking.repository.UserRepository;
import com.sayrain.medicalbooking.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.sayrain.medicalbooking.service.AuthService;
@Service
public class AuthServiceImpl implements AuthService{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;  // 改为PasswordEncoder接口


    @Autowired
    private JwtUtil jwtUtil;

    // 注册用户
    public String registerUser(RegisterRequest registerRequest) {
        // 检查用户名是否已经存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // 创建新用户对象
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));  // 加密密码
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setRole(registerRequest.getRole());

        // 保存用户到数据库
        userRepository.save(user);

        return "User registered successfully";
    }

    // 用户登录
    public AuthResponse loginUser(LoginRequest loginRequest) {
        // 根据用户名查询用户
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getUsername());

        // 返回AuthResponse对象
        return new AuthResponse(token, user.getUsername(), user.getRole().name(), "Login successful");
    }
}
