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
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;

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
    // @CacheEvict(value = "appointments", 
    //             key = {"'patient:' + #appointmentDTO.patientId", "'schedule:' + #appointmentDTO.scheduleId"})
    public Appointment createAppointment(AppointmentDTO appointmentDTO) {
        Long scheduleId = appointmentDTO.getScheduleId();
        Long patientId = appointmentDTO.getPatientId();

        // 使用悲观锁锁定相关排班的预约记录，防止并发问题
        List<Appointment> lockedAppointments = appointmentRepository.findByScheduleIdWithLock(scheduleId);
        log.info("锁定排班预约记录: {}", lockedAppointments.size());

        // 检查排班是否存在且为启用状态
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> {
                    log.error("排班不存在: {}", scheduleId);
                    return new BusinessException("排班不存在");
                });
        log.info("获取排班: {}", schedule);

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
        int currentCount = appointmentRepository.countValidAppointmentsByScheduleId(scheduleId);
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
    // @CacheEvict(value = {"appointments", "schedules"}, 
    //             key = {"'patient:' + #patientId", "'schedule:' + #appointment.schedule.id"})
    public void cancelAppointment(Long id, Long patientId) {
        log.info("患者取消预约：预约ID={}, 患者ID={}", id, patientId);
        
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
    // @CacheEvict(value = "appointments", allEntries = true)
    public void updateAppointmentStatus(Long id, AppointmentStatus status) {
        log.info("更新预约状态：预约ID={}, 新状态={}", id, status);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预约不存在"));

        AppointmentStatus oldStatus = appointment.getStatus();

        // 验证状态流转合法性
        if (oldStatus == AppointmentStatus.CANCELLED) {
            throw new BusinessException("已取消预约不能再改变状态");
        }

        if (oldStatus == AppointmentStatus.COMPLETED && status != AppointmentStatus.COMPLETED) {
            throw new BusinessException("已完成预约不能变更为其他状态");
        }

        // 检查状态转换是否允许
        if (!allowedTransitions.get(oldStatus).contains(status)) {
            throw new BusinessException(String.format("不允许从状态 %s 变更为 %s", oldStatus, status));
        }

        appointment.setStatus(status);
        appointmentRepository.save(appointment);
        
        log.info("更新预约状态成功：预约ID={}, 从{}变更为{}", id, oldStatus, status);
    }

    @Override
    // @Cacheable(value = "appointments", key = "'patient:' + #patientId", unless = "#result == null or #result.size() == 0")
    public List<Appointment> getPatientAppointments(Long patientId) {
        log.info("从数据库获取患者预约列表，患者ID: {}", patientId);
        return appointmentRepository.findActiveAppointmentsByPatientId(patientId);
    }

    @Override
    // @Cacheable(value = "appointments", key = "'schedule:' + #scheduleId", unless = "#result == null or #result.size() == 0")
    public List<Appointment> getScheduleAppointments(Long scheduleId) {
        log.info("从数据库获取排班预约列表，排班ID: {}", scheduleId);
        return appointmentRepository.findByScheduleId(scheduleId);
    }

    @Override
    @Cacheable(value = "appointments", key = "'status:' + #status.name()", unless = "#result == null or #result.size() == 0")
    public List<Appointment> getAppointmentsByStatus(AppointmentStatus status) {
        log.info("从数据库获取状态预约列表，状态: {}", status);
        return appointmentRepository.findByStatus(status);
    }

    @Override
    public boolean isScheduleAvailable(Long scheduleId) {
        log.info("检查排班可用性，排班ID: {}", scheduleId);
        
        try {
            // 检查排班是否存在且启用
            Schedule schedule = scheduleRepository.findById(scheduleId)
                    .orElseThrow(() -> new BusinessException("排班不存在"));

            boolean isAvailable = true;
            if (schedule.getStatus() == null || schedule.getStatus() != 1) {
                isAvailable = false;
            } else if (schedule.getDate().isBefore(LocalDate.now())) {
                isAvailable = false;
            } else {
                // 直接查询数据库检查预约数量
                int currentCount = appointmentRepository.countValidAppointmentsByScheduleId(scheduleId);
                isAvailable = schedule.getMaxPatients() == null || currentCount < schedule.getMaxPatients();
            }
            
            log.info("排班可用性检查完成，排班ID: {}, 可用性: {}", scheduleId, isAvailable);
            return isAvailable;
        } catch (Exception e) {
            log.error("检查排班可用性失败: scheduleId={}", scheduleId, e);
            return false;
        }
    }

    @Override
    public Integer getCurrentQueueNumber(Long scheduleId) {
        log.info("计算当前排队号，排班ID: {}", scheduleId);
        
        // 直接查询数据库计算排队号
        int currentCount = appointmentRepository.countValidAppointmentsByScheduleId(scheduleId);
        Integer queueNumber = currentCount + 1;
        
        log.info("当前排队号计算完成，排班ID: {}, 排队号: {}", scheduleId, queueNumber);
        return queueNumber;
    }

    @Override
    // @Cacheable(value = "appointments", key = "#id", unless = "#result == null")
    public Appointment getAppointmentById(Long id) {
        log.info("从数据库获取预约信息，预约ID: {}", id);
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new BusinessException("预约不存在"));
    }

    @Override
    public Page<Appointment> getAllAppointments(Pageable pageable, Long patientId, AppointmentStatus status) {
        log.info("管理员分页查询预约列表: page={}, size={}, patientId={}, status={}",
                pageable.getPageNumber(), pageable.getPageSize(), patientId, status);

        // 使用Specification构建动态查询
        Specification<Appointment> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (patientId != null) {
                predicates.add(criteriaBuilder.equal(root.get("patient").get("id"), patientId));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return appointmentRepository.findAll(spec, pageable);
    }

    // 分页查询患者预约
    @Override
    public Page<Appointment> getPatientAppointments(Long patientId, Pageable pageable) {
        log.info("分页查询患者预约: patientId={}, page={}, size={}",
                patientId, pageable.getPageNumber(), pageable.getPageSize());
        return appointmentRepository.findByPatientId(patientId, pageable);
    }

    // 分页查询医生相关预约
    @Override
    // @Cacheable(value = "appointments", key = "'doctor:' + #doctorId")
    public Page<Appointment> getDoctorAppointments(Long doctorId, Pageable pageable) {
        log.info("分页查询医生预约: doctorId={}, page={}, size={}",
                doctorId, pageable.getPageNumber(), pageable.getPageSize());

        // 这里需要根据你的业务逻辑实现
        // 假设医生可以看到自己排班的所有预约
        return appointmentRepository.findByDoctorId(doctorId, pageable);
    }
}