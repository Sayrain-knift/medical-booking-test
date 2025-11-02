package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.dto.ScheduleDTO;
import com.sayrain.medicalbooking.exception.BusinessException;
import com.sayrain.medicalbooking.model.Doctor;
import com.sayrain.medicalbooking.model.Schedule;
import com.sayrain.medicalbooking.repository.ScheduleRepository;
import com.sayrain.medicalbooking.service.DoctorService;
import com.sayrain.medicalbooking.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final DoctorService doctorService;

    @Override
    @Transactional
    public Schedule createSchedule(ScheduleDTO scheduleDTO) {
        // 验证时间有效性
        if (!scheduleDTO.isTimeValid()) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }

        // 检查医生状态
        if (!doctorService.isDoctorActive(scheduleDTO.getDoctorId())) {
            throw new BusinessException("医生不存在或不处于启用状态");
        }

        // 获取医生对象
        Doctor doctor = doctorService.getDoctorById(scheduleDTO.getDoctorId());

        // 使用Repository中的正确方法
        if (!scheduleRepository.findTimeConflicts(
                scheduleDTO.getDoctorId(),
                scheduleDTO.getDate(),
                scheduleDTO.getStartTime(),
                scheduleDTO.getEndTime()).isEmpty()) {
            throw new BusinessException("该医生在此时间段已有排班");
        }

        Schedule schedule = new Schedule();
        schedule.setDoctor(doctor);  // 设置Doctor对象
        schedule.setDate(scheduleDTO.getDate());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setMaxPatients(scheduleDTO.getMaxPatients());
        schedule.setStatus(scheduleDTO.getStatus());

        Schedule savedSchedule = scheduleRepository.save(schedule);
        log.info("创建排班成功：医生={}, 日期={}, 时间段={}-{}",
                doctor.getName(), scheduleDTO.getDate(),
                scheduleDTO.getStartTime(), scheduleDTO.getEndTime());
        return savedSchedule;
    }

    @Override
    @Transactional
    public Schedule updateSchedule(Long id, ScheduleDTO scheduleDTO) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在"));

        // 验证时间有效性
        if (!scheduleDTO.isTimeValid()) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }

        // 检查医生状态
        if (!doctorService.isDoctorActive(scheduleDTO.getDoctorId())) {
            throw new BusinessException("医生不存在或不处于启用状态");
        }

        // 获取医生对象
        Doctor doctor = doctorService.getDoctorById(scheduleDTO.getDoctorId());

        // 使用排除自身的时间冲突检测
        if (!scheduleRepository.findTimeConflictsExcludeSelf(
                scheduleDTO.getDoctorId(),
                scheduleDTO.getDate(),
                scheduleDTO.getStartTime(),
                scheduleDTO.getEndTime(),
                id).isEmpty()) {
            throw new BusinessException("该医生在此时间段已有排班");
        }

        // 检查关联预约：如果已有预约，不能修改关键信息
        // TODO: 如果排班已关联预约，检查是否可以修改

        // 设置Doctor对象而不是doctorId
        schedule.setDoctor(doctor);
        schedule.setDate(scheduleDTO.getDate());
        schedule.setStartTime(scheduleDTO.getStartTime());
        schedule.setEndTime(scheduleDTO.getEndTime());
        schedule.setMaxPatients(scheduleDTO.getMaxPatients());
        schedule.setStatus(scheduleDTO.getStatus());
        // 时间戳由@PreUpdate自动处理

        Schedule updatedSchedule = scheduleRepository.save(schedule);
        log.info("更新排班成功：排班ID={}, 医生={}, 日期={}",
                id, doctor.getName(), scheduleDTO.getDate());
        return updatedSchedule;
    }

    @Override
    @Transactional
    public void deleteSchedule(Long id) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在"));

        // 检查是否有关联预约
        // TODO: 如果排班已关联预约，不能删除

        scheduleRepository.delete(schedule);
        log.info("删除排班成功：排班ID={}, 医生={}", id, schedule.getDoctor().getName());
    }

    @Override
    public Schedule getScheduleById(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在"));
    }

    @Override
    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    @Override
    public List<Schedule> getSchedulesByDoctor(Long doctorId) {
        return scheduleRepository.findByDoctorId(doctorId);
    }

    @Override
    public List<Schedule> getSchedulesByDateRange(LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findByDateBetween(startDate, endDate);
    }

    @Override
    public List<Schedule> getDoctorSchedulesByDateRange(Long doctorId, LocalDate startDate, LocalDate endDate) {
        return scheduleRepository.findByDoctorIdAndDateBetween(doctorId, startDate, endDate);
    }

    //可预约排班查询
    @Override
    public List<Schedule> getAvailableSchedules(Long departmentId, LocalDate date) {
        LocalDate today = LocalDate.now();
        List<Schedule> schedules = scheduleRepository.findAvailableSchedules(departmentId, date, today);
        log.info("查询可预约排班：科室ID={}, 日期={}, 返回条数={}", departmentId, date, schedules.size());
        return schedules;
    }

    @Override
    @Transactional
    public void changeScheduleStatus(Long id, Integer status) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new BusinessException("排班不存在"));

        schedule.setStatus(status);
        // 时间戳由@PreUpdate自动处理
        scheduleRepository.save(schedule);
        log.info("修改排班状态成功：排班ID={}, 医生={}, 新状态={}",
                id, schedule.getDoctor().getName(), status);
    }



}