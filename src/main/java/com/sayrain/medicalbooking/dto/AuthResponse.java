package com.sayrain.medicalbooking.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor  // 添加这个注解生成全参构造函数
public class AuthResponse {
    private String token;
    private String username;
    private String role;
    private String message;
}