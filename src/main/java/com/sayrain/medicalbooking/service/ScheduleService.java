package com.sayrain.medicalbooking.service;

import com.sayrain.medicalbooking.dto.ScheduleDTO;
import com.sayrain.medicalbooking.model.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {

    // 创建排班
    Schedule createSchedule(ScheduleDTO scheduleDTO);

    // 更新排班
    Schedule updateSchedule(Long id, ScheduleDTO scheduleDTO);

    // 删除排班
    void deleteSchedule(Long id);

    // 根据ID获取排班
    Schedule getScheduleById(Long id);

    // 获取所有排班
    List<Schedule> getAllSchedules();

    // 根据医生ID获取排班
    List<Schedule> getSchedulesByDoctor(Long doctorId);

    // 根据日期范围获取排班
    List<Schedule> getSchedulesByDateRange(LocalDate startDate, LocalDate endDate);

    // 根据医生ID和日期范围获取排班
    List<Schedule> getDoctorSchedulesByDateRange(Long doctorId, LocalDate startDate, LocalDate endDate);

    // 获取可预约的排班
    List<Schedule> getAvailableSchedules(Long departmentId, LocalDate date);

    // 修改排班状态
    void changeScheduleStatus(Long id, Integer status);


}
