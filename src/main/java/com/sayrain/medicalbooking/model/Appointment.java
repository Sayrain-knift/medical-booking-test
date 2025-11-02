package com.sayrain.medicalbooking.model;

import jakarta.persistence.*;
import lombok.Data;


@Entity
@Table(name = "appointments")
@Data
public class Appointment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id")
    private Patient patient;

    @ManyToOne
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @Column(name = "queue_number")
    private Integer queueNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private AppointmentStatus status;

    // 修复枚举定义 - 使用大写并添加完整状态
    public enum AppointmentStatus {
        PENDING,      // 待确认
        CONFIRMED,    // 已确认
        COMPLETED,    // 已完成
        CANCELLED     // 已取消
    }
}