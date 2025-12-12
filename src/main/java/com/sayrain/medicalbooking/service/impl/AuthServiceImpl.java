package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.dto.AuthResponse;
import com.sayrain.medicalbooking.dto.LoginRequest;
import com.sayrain.medicalbooking.dto.RegisterRequest;
import com.sayrain.medicalbooking.model.User;
import com.sayrain.medicalbooking.model.Patient;
import com.sayrain.medicalbooking.repository.UserRepository;
import com.sayrain.medicalbooking.repository.PatientRepository;
import com.sayrain.medicalbooking.util.JwtUtil;
import com.sayrain.medicalbooking.utils.RedisUtils;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.sayrain.medicalbooking.service.AuthService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService{

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisUtils redisUtils;

    private static final String USER_CACHE_PREFIX = "user:";
    private static final String TOKEN_CACHE_PREFIX = "token:";
    private static final long TOKEN_EXPIRE_TIME = 24; // 24小时

    @Override
    public String registerUser(RegisterRequest registerRequest) {
        log.info("开始注册用户: {}", registerRequest.getUsername());
        
        // 检查用户名是否已经存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }

        // 创建新用户对象
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        
        // 设置默认角色为PATIENT
        if (registerRequest.getRole() != null) {
            user.setRole(registerRequest.getRole());
        } else {
            user.setRole(User.UserRole.PATIENT);
        }

        // 保存用户到数据库
        user = userRepository.save(user);
        log.info("用户注册成功: {}", registerRequest.getUsername());

        // 如果是PATIENT角色，创建对应的Patient记录
        if (user.getRole() == User.UserRole.PATIENT) {
            try {
                Patient patient = new Patient();
                patient.setUser(user);
                patient.setName(user.getUsername()); // 使用用户名作为默认姓名
                patient.setPhone(user.getPhone());
                patient.setHealthCard("HC" + System.currentTimeMillis()); // 生成默认健康卡号
                
                patientRepository.save(patient);
                log.info("为用户 {} 创建Patient记录成功", user.getUsername());
            } catch (Exception e) {
                log.error("为用户 {} 创建Patient记录失败: {}", user.getUsername(), e.getMessage());
                // 不抛出异常，避免影响用户注册流程
            }
        }

        return "User registered successfully";
    }

    @Override
    public AuthResponse loginUser(LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        log.info("用户登录: {}", username);
        
        // 先从Redis缓存中查找用户信息
        String userCacheKey = USER_CACHE_PREFIX + username;
        Object cachedUserObj = redisUtils.get(userCacheKey);
        User cachedUser = null;
        
        // 安全地转换缓存对象
        if (cachedUserObj != null) {
            try {
                if (cachedUserObj instanceof User) {
                    cachedUser = (User) cachedUserObj;
                } else {
                    log.warn("缓存中的用户对象类型不匹配，将重新从数据库查询: {}", cachedUserObj.getClass().getName());
                }
            } catch (Exception e) {
                log.warn("转换缓存用户对象失败，将重新从数据库查询: {}", e.getMessage());
            }
        }
        
        User user;
        if (cachedUser != null) {
            user = cachedUser;
            log.info("从Redis缓存获取用户信息: {}", username);
        } else {
            // 从数据库查询用户
            user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
            
            // 将用户信息缓存到Redis，缓存30分钟
            redisUtils.set(userCacheKey, user, 30);
            log.info("从数据库获取用户信息并缓存: {}", username);
        }

        // 验证密码
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        // 生成JWT Token
        String token = jwtUtil.generateToken(user.getUsername());

        // 将token和用户信息关联缓存到Redis
        String tokenCacheKey = TOKEN_CACHE_PREFIX + token;
        redisUtils.set(tokenCacheKey, user.getUsername(), TOKEN_EXPIRE_TIME);
        
        // 缓存用户登录状态
        String loginStatusKey = "login:status:" + username;
        redisUtils.set(loginStatusKey, "active", TOKEN_EXPIRE_TIME);

        log.info("用户登录成功并缓存: {}", username);

        // 返回AuthResponse对象
        return new AuthResponse(token, user.getUsername(), user.getRole().name(), "Login successful");
    }

    @Override
    public String logout(String token) {
        try {
            // 从token中获取用户名
            String username = jwtUtil.extractUsername(token);
            log.info("用户登出: {}", username);
            
            // 删除用户缓存
            redisUtils.del(USER_CACHE_PREFIX + username);
            
            // 删除token缓存
            redisUtils.del(TOKEN_CACHE_PREFIX + token);
            
            // 删除登录状态缓存
            redisUtils.del("login:status:" + username);
            
            log.info("用户登出成功，清理缓存: {}", username);
            return "Logout successful";
        } catch (Exception e) {
            log.error("登出失败: {}", e.getMessage());
            throw new RuntimeException("Logout failed: " + e.getMessage());
        }
    }

    @Override
    public boolean isTokenValid(String token) {
        try {
            String tokenCacheKey = TOKEN_CACHE_PREFIX + token;
            Object usernameObj = redisUtils.get(tokenCacheKey);
            String username = usernameObj != null ? usernameObj.toString() : null;
            
            if (username != null) {
                // 检查登录状态
                String loginStatusKey = "login:status:" + username;
                Object statusObj = redisUtils.get(loginStatusKey);
                String status = statusObj != null ? statusObj.toString() : null;
                return "active".equals(status);
            }
            
            return false;
        } catch (Exception e) {
            log.error("检查token有效性失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void refreshUserCache(String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            String userCacheKey = USER_CACHE_PREFIX + username;
            redisUtils.set(userCacheKey, user, 30);
            
            log.info("刷新用户缓存成功: {}", username);
        } catch (Exception e) {
            log.error("刷新用户缓存失败: {}", e.getMessage());
        }
    }
}
