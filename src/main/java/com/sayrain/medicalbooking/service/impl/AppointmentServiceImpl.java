package com.sayrain.medicalbooking.service.impl;

import com.sayrain.medicalbooking.dto.AppointmentDTO;
import com.sayrain.medicalbooking.exception.BusinessException;
import com.sayrain.medicalbooking.model.Appointment;
import com.sayrain.medicalbooking.model.Appointment.AppointmentStatus;
import com.sayrain.medicalbooking.model.Schedule;
import com.sayrain.medicalbooking.model.Patient;
import com.sayrain.medicalbooking.repository.AppointmentRepository;
import com.sayrain.medicalbooking.repository.ScheduleRepository;
import com.sayrain.medicalbooking.repository.PatientRepository;
import com.sayrain.medicalbooking.service.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ScheduleRepository scheduleRepository;
    private final PatientRepository patientRepository;



    // 定义允许的状态转换
    private final Map<AppointmentStatus, Set<AppointmentStatus>> allowedTransitions = Map.of(
            AppointmentStatus.PENDING, Set.of(AppointmentStatus.CONFIRMED, AppointmentStatus.CANCELLED),
            AppointmentStatus.CONFIRMED, Set.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED),
            AppointmentStatus.COMPLETED, Set.of(),
            AppointmentStatus.CANCELLED, Set.of()
    );

    @Override
    @Transactional
    public Appointment createAppointment(AppointmentDTO appointmentDTO) {
        Long scheduleId = appointmentDTO.getScheduleId();
        Long patientId = appointmentDTO.getPatientId();

        // 使用悲观锁锁定相关排班的预约记录，防止并发问题
        List<Appointment> lockedAppointments = appointmentRepository.findByScheduleIdWithLock(scheduleId);
        log.info("锁定排班预约记录: {}", lockedAppointments.size());

        // 检查排班是否存在且为启用状态
        // 检查排班是否存在且为启用状态
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> {
                    log.error("排班不存在: {}", scheduleId);
                    return new BusinessException("排班不存在");
                });
        log.info("找到排班: {}", schedule);

        if (schedule.getStatus() == null || schedule.getStatus() != 1) {
            log.error("排班状态不可用: status={}", schedule.getStatus());
            throw new BusinessException("排班当前不可预约（未启用或已停用）");
        }

        // 检查排班日期不能是过去日期
        if (schedule.getDate().isBefore(LocalDate.now())) {
            log.error("排班日期已过: date={}", schedule.getDate());
            throw new BusinessException("排班日期已过，无法预约");
        }

        // 检查排班是否已满额（当前预约数 < 最大患者数）
        Integer currentCount = appointmentRepository.countValidAppointmentsByScheduleId(scheduleId);
        log.info("当前预约数: {}, 最大患者数: {}", currentCount, schedule.getMaxPatients());

        if (schedule.getMaxPatients() != null && currentCount >= schedule.getMaxPatients()) {
            throw new BusinessException("该排班预约已满");
        }

        // 检查患者是否已预约该排班（同一患者不能重复预约）
        if (appointmentRepository.findByScheduleIdAndPatientId(scheduleId, patientId).isPresent()) {
            throw new BusinessException("您已预约该排班，不能重复预约");
        }

        // 检查患者是否存在
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> {
                    log.error("患者不存在: {}", patientId);
                    return new BusinessException("患者不存在");
                });
        log.info("找到患者: {}", patient);
        // 自动生成排队号（当前有效预约数 + 1）
        int queueNumber = currentCount + 1;

        // 创建预约实体
        Appointment appointment = new Appointment();
        appointment.setSchedule(schedule);
        appointment.setPatient(patient);
        appointment.setQueueNumber(queueNumber);
        appointment.setStatus(AppointmentStatus.PENDING);

        Appointment saved = appointmentRepository.save(appointment);
        log.info("创建预约成功：预约ID={}, 患者={}, 排班日期={}, 排队号={}",
                saved.getId(), patient.getName(), schedule.getDate(), queueNumber);
        return saved;
    }

    @Override
    @Transactional
    public void cancelAppointment(Long id, Long patientId) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预约不存在"));

        // 验证患者身份（只能取消自己的预约）
        if (!appointment.getPatient().getId().equals(patientId)) {
            throw new BusinessException("您无权取消此预约");
        }

        // 检查预约状态（只能取消 PENDING 或 CONFIRMED 状态）
        AppointmentStatus status = appointment.getStatus();
        if (!(status == AppointmentStatus.PENDING || status == AppointmentStatus.CONFIRMED)) {
            throw new BusinessException("此预约状态不能取消");
        }

        // 更新状态为 CANCELLED
        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
        log.info("取消预约成功：预约ID={}, 患者ID={}", id, patientId);
    }

    @Override
    @Transactional
    public void updateAppointmentStatus(Long id, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预约不存在"));

        AppointmentStatus oldStatus = appointment.getStatus();

        // 验证状态流转合法性
        if (oldStatus == AppointmentStatus.CANCELLED) {
            throw new BusinessException("已取消预约不能再改变状态");
        }

        if (oldStatus == AppointmentStatus.COMPLETED && newStatus != AppointmentStatus.COMPLETED) {
            throw new BusinessException("已完成预约不能变更为其他状态");
        }

        // 检查状态转换是否允许
        if (!allowedTransitions.get(oldStatus).contains(newStatus)) {
            throw new BusinessException(String.format("不允许从状态 %s 变更为 %s", oldStatus, newStatus));
        }

        appointment.setStatus(newStatus);
        appointmentRepository.save(appointment);
        log.info("更新预约状态成功：预约ID={}, 从{}变更为{}", id, oldStatus, newStatus);
    }

    @Override
    public List<Appointment> getPatientAppointments(Long patientId) {
        return appointmentRepository.findActiveAppointmentsByPatientId(patientId);
    }

    @Override
    public List<Appointment> getScheduleAppointments(Long scheduleId) {
        return appointmentRepository.findByScheduleId(scheduleId);
    }

    @Override
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status);
    }

    @Override
    public boolean isScheduleAvailable(Long scheduleId) {
        try {
            // 检查排班是否存在且启用
            Schedule schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new BusinessException("排班不存在"));

            if (schedule.getStatus() == null || schedule.getStatus() != 1) {
                return false;
            }

            if (schedule.getDate().isBefore(LocalDate.now())) {
                return false;
            }

            Integer currentCount = appointmentRepository.countValidAppointmentsByScheduleId(scheduleId);
            return schedule.getMaxPatients() == null || currentCount < schedule.getMaxPatients();
        } catch (Exception e) {
            log.error("检查排班可用性失败: scheduleId={}", scheduleId, e);
            return false;
        }
    }

    @Override
    public Integer getCurrentQueueNumber(Long scheduleId) {
        Integer currentCount = appointmentRepository.countValidAppointmentsByScheduleId(scheduleId);
        return currentCount + 1;
    }

    @Override
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预约不存在"));
    }
}