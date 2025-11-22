package com.sayrain.medicalbooking.service;

import com.sayrain.medicalbooking.dto.AppointmentDTO;
import com.sayrain.medicalbooking.model.Appointment;
import com.sayrain.medicalbooking.model.Appointment.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AppointmentService {

    Appointment createAppointment(AppointmentDTO appointmentDTO);


    void cancelAppointment(Long id, Long patientId);


    void updateAppointmentStatus(Long id, AppointmentStatus status);


    List<Appointment> getPatientAppointments(Long patientId);


    List<Appointment> getScheduleAppointments(Long scheduleId);


    List<Appointment> getAppointmentsByStatus(AppointmentStatus status);


    boolean isScheduleAvailable(Long scheduleId);


    Integer getCurrentQueueNumber(Long scheduleId);

    Appointment getAppointmentById(Long id);

    // 新增：分页查询所有预约（管理员用）
    Page<Appointment> getAllAppointments(Pageable pageable, Long patientId, AppointmentStatus status);

    // 新增：分页查询患者预约
    Page<Appointment> getPatientAppointments(Long patientId, Pageable pageable);

    // 新增：分页查询医生相关预约
    Page<Appointment> getDoctorAppointments(Long doctorId, Pageable pageable);
}
