package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.dto.ScheduleDTO;
import com.sayrain.medicalbooking.exception.BusinessException;
import com.sayrain.medicalbooking.model.Department;
import com.sayrain.medicalbooking.model.Doctor;
import com.sayrain.medicalbooking.model.Schedule;
import com.sayrain.medicalbooking.repository.ScheduleRepository;
import com.sayrain.medicalbooking.service.DoctorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceTest {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    private ScheduleDTO scheduleDTO;
    private Doctor doctor;
    private Department department;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        // 创建科室对象
        department = new Department();
        department.setId(2L);
        department.setName("测试科室");

        // 创建医生对象 - 基于您实际的Doctor类结构
        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("张医生");
        doctor.setTitle("主任医师");
        doctor.setDepartment(department);  // ✅ 正确设置关联科室
        doctor.setStatus(1);
        doctor.setDescription("测试医生描述");
        doctor.setSpecialty("测试专科");

        // 创建排班DTO
        scheduleDTO = new ScheduleDTO();
        scheduleDTO.setDoctorId(1L);
        scheduleDTO.setDate(LocalDate.of(2025, 10, 25));
        scheduleDTO.setStartTime(LocalTime.of(9, 0));
        scheduleDTO.setEndTime(LocalTime.of(11, 0));
        scheduleDTO.setMaxPatients(10);
        scheduleDTO.setStatus(1);

        // 创建排班实体
        schedule = new Schedule();
        schedule.setId(1L);
        schedule.setDoctor(doctor);
        schedule.setDate(scheduleDTO.getDate());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setMaxPatients(scheduleDTO.getMaxPatients());
        schedule.setStatus(scheduleDTO.getStatus());
    }

    /** 测试创建排班成功 */
    @Test
    void testCreateSchedule_Success() {
        when(doctorService.isDoctorActive(1L)).thenReturn(true);
        when(doctorService.getDoctorById(1L)).thenReturn(doctor);
        when(scheduleRepository.findTimeConflicts(anyLong(), any(), any(), any())).thenReturn(List.of());
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);

        Schedule result = scheduleService.createSchedule(scheduleDTO);

        assertNotNull(result);
        assertEquals(scheduleDTO.getDate(), result.getDate());
        assertEquals(doctor.getName(), result.getDoctor().getName());
        verify(scheduleRepository).save(any(Schedule.class));
    }

    /** 测试时间冲突异常 */
    @Test
    void testCreateSchedule_TimeConflict() {
        when(doctorService.isDoctorActive(1L)).thenReturn(true);
        when(scheduleRepository.findTimeConflicts(anyLong(), any(), any(), any()))
                .thenReturn(List.of(new Schedule()));

        BusinessException ex = assertThrows(BusinessException.class, () -> scheduleService.createSchedule(scheduleDTO));
        assertEquals("该医生在此时间段已有排班", ex.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    /** 测试医生未启用异常 */
    @Test
    void testCreateSchedule_DoctorNotActive() {
        when(doctorService.isDoctorActive(1L)).thenReturn(false);

        BusinessException ex = assertThrows(BusinessException.class, () -> scheduleService.createSchedule(scheduleDTO));
        assertEquals("医生不存在或不处于启用状态", ex.getMessage());
        verify(scheduleRepository, never()).save(any());
    }

    /** 测试根据ID获取排班成功 */
    @Test
    void testGetScheduleById_Success() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        Schedule result = scheduleService.getScheduleById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("张医生", result.getDoctor().getName());
        verify(scheduleRepository).findById(1L);
    }

    /** 测试根据ID获取排班不存在 */
    @Test
    void testGetScheduleById_NotFound() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class, () -> scheduleService.getScheduleById(1L));
        assertEquals("排班不存在", ex.getMessage());
    }

    /** 测试更新排班成功 */
    @Test
    void testUpdateSchedule_Success() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));
        when(doctorService.isDoctorActive(1L)).thenReturn(true);
        when(doctorService.getDoctorById(1L)).thenReturn(doctor);
        when(scheduleRepository.findTimeConflictsExcludeSelf(anyLong(), any(), any(), any(), anyLong()))
                .thenReturn(List.of());
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);

        Schedule result = scheduleService.updateSchedule(1L, scheduleDTO);

        assertNotNull(result);
        assertEquals(scheduleDTO.getDate(), result.getDate());
        verify(scheduleRepository).save(any(Schedule.class));
    }

    /** 测试删除排班成功 */
    @Test
    void testDeleteSchedule_Success() {
        when(scheduleRepository.findById(1L)).thenReturn(Optional.of(schedule));

        scheduleService.deleteSchedule(1L);

        verify(scheduleRepository).delete(schedule);
    }

    /** 测试根据医生获取排班 */
    @Test
    void testGetSchedulesByDoctor() {
        when(scheduleRepository.findByDoctorId(1L)).thenReturn(List.of(schedule));

        List<Schedule> result = scheduleService.getSchedulesByDoctor(1L);

        assertEquals(1, result.size());
        assertEquals("张医生", result.get(0).getDoctor().getName());
        verify(scheduleRepository).findByDoctorId(1L);
    }
}