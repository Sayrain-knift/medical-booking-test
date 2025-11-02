package com.sayrain.medicalbooking.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    // 使用新版本的密钥生成方式
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor("medical-booking-secret-key-medical-booking-secret-key".getBytes());

    // Token 过期时间（24小时）
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 24; // 24 hours

    // 生成JWT Token - 使用新API
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)  // 用户名
                .setIssuedAt(new Date())  // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))  // 设置过期时间
                .signWith(SECRET_KEY)  // 使用新API签名
                .compact();
    }

    // 从Token中提取用户名 - 使用新API
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 验证Token - 使用新API
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // 检查Token是否过期 - 使用新API
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (JwtException e) {
            return true;
        }
    }

    // 从Token中提取过期时间 - 使用新API
    private Date extractExpiration(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();
    }
}