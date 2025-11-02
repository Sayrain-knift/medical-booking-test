package com.sayrain.medicalbooking.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;

@Getter
public class CustomUserDetails extends User {
    private final Long userId;
    private final String email;
    private final String phone;
    private final com.sayrain.medicalbooking.model.User.UserRole role;

    public CustomUserDetails(String username, String password,
                             Collection<? extends GrantedAuthority> authorities,
                             Long userId, String email, String phone,
                             com.sayrain.medicalbooking.model.User.UserRole role) {
        super(username, password, authorities);
        this.userId = userId;
        this.email = email;
        this.phone = phone;
        this.role = role;
    }
}