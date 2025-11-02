package com.sayrain.medicalbooking.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "patients")
@Data
public class Patient {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    private String name;
    private String phone;

    @Column(name = "health_card")
    private String healthCard;
}