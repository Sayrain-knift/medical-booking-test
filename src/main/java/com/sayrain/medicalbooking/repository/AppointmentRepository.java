package com.sayrain.medicalbooking.repository;

import com.sayrain.medicalbooking.model.Appointment;
import com.sayrain.medicalbooking.model.Appointment.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>, JpaSpecificationExecutor<Appointment> {

    // 根据患者ID查询预约
    List<Appointment> findByPatientId(Long patientId);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND a.status != 'CANCELLED'")
    List<Appointment> findActiveAppointmentsByPatientId(@Param("patientId") Long patientId);

    // 根据排班ID查询预约
    List<Appointment> findByScheduleId(Long scheduleId);

    // 根据状态查询预约
    List<Appointment> findByStatus(AppointmentStatus status);

    // 检查患者是否已预约该排班
    Optional<Appointment> findByScheduleIdAndPatientId(Long scheduleId, Long patientId);

    // 修复：使用正确的枚举值 CANCELLED
    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.schedule.id = :scheduleId AND a.status <> 'CANCELLED'")
    Integer countValidAppointmentsByScheduleId(@Param("scheduleId") Long scheduleId);
    // 查询患者特定状态的预约
    List<Appointment> findByPatientIdAndStatus(Long patientId, AppointmentStatus status);

    // 查询排班特定状态的预约
    List<Appointment> findByScheduleIdAndStatus(Long scheduleId, AppointmentStatus status);

    // 统计所有状态的预约
    Integer countByScheduleId(Long scheduleId);

    // 添加分页查询
    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId ORDER BY a.id DESC")
    List<Appointment> findPatientAppointmentsWithPaging(@Param("patientId") Long patientId);

    // 添加悲观锁查询用于并发控制
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Appointment a WHERE a.schedule.id = :scheduleId")
    List<Appointment> findByScheduleIdWithLock(@Param("scheduleId") Long scheduleId);

    // 新增：患者预约分页查询
    Page<Appointment> findByPatientId(Long patientId, Pageable pageable);

    // 新增：医生相关预约分页查询（需要根据你的业务逻辑调整）
    @Query("SELECT a FROM Appointment a WHERE a.schedule.doctor.id = :doctorId")
    Page<Appointment> findByDoctorId(@Param("doctorId") Long doctorId, Pageable pageable);

    // 新增：按状态分页查询
    Page<Appointment> findByStatus(AppointmentStatus status, Pageable pageable);
}