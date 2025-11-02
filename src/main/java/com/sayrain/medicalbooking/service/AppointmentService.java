package com.sayrain.medicalbooking.service;

import com.sayrain.medicalbooking.dto.AppointmentDTO;
import com.sayrain.medicalbooking.model.Appointment;
import com.sayrain.medicalbooking.model.Appointment.AppointmentStatus;

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
}
