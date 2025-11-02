package com.sayrain.medicalbooking.dto;

import com.sayrain.medicalbooking.model.User;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String email;
    private String phone;
    private User.UserRole role; // 改为枚举类型
}