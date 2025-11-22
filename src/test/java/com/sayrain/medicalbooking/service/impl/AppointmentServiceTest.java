package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.dto.AppointmentDTO;
import com.sayrain.medicalbooking.exception.BusinessException;
import com.sayrain.medicalbooking.model.Appointment;
import com.sayrain.medicalbooking.model.Appointment.AppointmentStatus;
import com.sayrain.medicalbooking.model.Patient;
import com.sayrain.medicalbooking.model.Schedule;
import com.sayrain.medicalbooking.repository.AppointmentRepository;
import com.sayrain.medicalbooking.repository.PatientRepository;
import com.sayrain.medicalbooking.repository.ScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private PatientRepository patientRepository;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private Schedule validSchedule;
    private Patient validPatient;
    private Appointment existingAppointment;

    @BeforeEach
    void setUp() {
        validSchedule = new Schedule();
        validSchedule.setId(1L);
        validSchedule.setDate(LocalDate.now().plusDays(1));
        validSchedule.setStatus(1);
        validSchedule.setMaxPatients(3);

        validPatient = new Patient();
        validPatient.setId(1L);
        validPatient.setName("张三");

        existingAppointment = new Appointment();
        existingAppointment.setId(100L);
        existingAppointment.setSchedule(validSchedule);
        existingAppointment.setPatient(validPatient);
        existingAppointment.setStatus(AppointmentStatus.PENDING);
        existingAppointment.setQueueNumber(1);
    }

    // 创建预约成功
    @Test
    void testCreateAppointment_Success() {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setScheduleId(1L);
        dto.setPatientId(1L);

        when(appointmentRepository.findByScheduleIdWithLock(1L)).thenReturn(List.of());
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(validSchedule));
        when(appointmentRepository.countValidAppointmentsByScheduleId(1L)).thenReturn(1);
        when(appointmentRepository.findByScheduleIdAndPatientId(1L, 1L)).thenReturn(Optional.empty());
        when(patientRepository.findById(1L)).thenReturn(Optional.of(validPatient));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(inv -> {
            Appointment saved = inv.getArgument(0);
            saved.setId(200L);
            return saved;
        });

        Appointment result = appointmentService.createAppointment(dto);

        assertNotNull(result);
        assertEquals(200L, result.getId());
        assertEquals(AppointmentStatus.PENDING, result.getStatus());
        assertEquals(2, result.getQueueNumber());
        verify(appointmentRepository).save(any(Appointment.class));
    }

    // 排班已满
    @Test
    void testCreateAppointment_ScheduleFull() {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setScheduleId(1L);
        dto.setPatientId(1L);

        when(appointmentRepository.findByScheduleIdWithLock(1L)).thenReturn(List.of());
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(validSchedule));
        when(appointmentRepository.countValidAppointmentsByScheduleId(1L)).thenReturn(3);

        BusinessException ex = assertThrows(BusinessException.class,
                () -> appointmentService.createAppointment(dto));
        assertEquals("该排班预约已满", ex.getMessage());
    }

    // 重复预约
    @Test
    void testCreateAppointment_DuplicateAppointment() {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setScheduleId(1L);
        dto.setPatientId(1L);

        when(appointmentRepository.findByScheduleIdWithLock(1L)).thenReturn(List.of());
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(validSchedule));
        when(appointmentRepository.countValidAppointmentsByScheduleId(1L)).thenReturn(1);
        when(appointmentRepository.findByScheduleIdAndPatientId(1L, 1L)).thenReturn(Optional.of(existingAppointment));

        BusinessException ex = assertThrows(BusinessException.class,
                () -> appointmentService.createAppointment(dto));
        assertEquals("您已预约该排班，不能重复预约", ex.getMessage());
    }

    // 排班不存在
    @Test
    void testCreateAppointment_ScheduleNotFound() {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setScheduleId(99L);
        dto.setPatientId(1L);

        when(scheduleRepository.findById(99L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> appointmentService.createAppointment(dto));
        assertEquals("排班不存在", ex.getMessage());
    }

    // 获取预约成功
    @Test
    void testGetAppointmentById_Success() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(existingAppointment));

        Appointment result = appointmentService.getAppointmentById(100L);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(appointmentRepository).findById(100L);
    }

    // 预约不存在
    @Test
    void testGetAppointmentById_NotFound() {
        when(appointmentRepository.findById(404L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> appointmentService.getAppointmentById(404L));
        assertEquals("预约不存在", ex.getMessage());
    }

    // 取消预约成功
    @Test
    void testCancelAppointment_Success() {
        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(existingAppointment));

        appointmentService.cancelAppointment(100L, 1L);

        assertEquals(AppointmentStatus.CANCELLED, existingAppointment.getStatus());
        verify(appointmentRepository).save(existingAppointment);
    }

    // 更新预约状态成功
    @Test
    void testUpdateAppointmentStatus_Success() {
        Appointment appt = new Appointment();
        appt.setId(1L);
        appt.setStatus(AppointmentStatus.PENDING);

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appt));

        appointmentService.updateAppointmentStatus(1L, AppointmentStatus.CONFIRMED);

        assertEquals(AppointmentStatus.CONFIRMED, appt.getStatus());
        verify(appointmentRepository).save(appt);
    }

    // 根据患者ID查询预约
    @Test
    void testGetAppointmentsByPatient() {
        when(appointmentRepository.findActiveAppointmentsByPatientId(1L)).thenReturn(List.of(existingAppointment));

        List<Appointment> list = appointmentService.getPatientAppointments(1L);

        assertEquals(1, list.size());
        assertEquals(existingAppointment, list.get(0));
    }

    // 根据排班ID查询预约
    @Test
    void testGetAppointmentsBySchedule() {
        when(appointmentRepository.findByScheduleId(1L)).thenReturn(List.of(existingAppointment));

        List<Appointment> list = appointmentService.getScheduleAppointments(1L);

        assertEquals(1, list.size());
        assertEquals(existingAppointment, list.get(0));
    }
}
