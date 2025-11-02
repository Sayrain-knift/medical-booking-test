package com.sayrain.medicalbooking.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private String email;
    private String phone;

    @Enumerated(EnumType.STRING)
    private UserRole role;  // PATIENT, DOCTOR, ADMIN

    public enum UserRole {
        PATIENT, DOCTOR, ADMIN
    }
}