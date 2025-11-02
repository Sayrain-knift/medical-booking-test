package com.sayrain.medicalbooking.repository;

import com.sayrain.medicalbooking.model.Schedule;
import com.sayrain.medicalbooking.model.Appointment; // ✅ 添加必要的导入
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    // 根据医生ID查询排班
    List<Schedule> findByDoctorId(Long doctorId);

    // 查询医生某天的排班
    List<Schedule> findByDoctorIdAndDate(Long doctorId, LocalDate date);

    // 查询日期范围内的排班
    List<Schedule> findByDateBetween(LocalDate startDate, LocalDate endDate);

    // 查询医生在日期范围内的排班
    List<Schedule> findByDoctorIdAndDateBetween(Long doctorId, LocalDate startDate, LocalDate endDate);

    // 时间冲突检测：查询排班时间是否冲突
    @Query("SELECT s FROM Schedule s WHERE s.doctor.id = :doctorId AND s.date = :date " +
            "AND s.status = 1 AND (:startTime < s.endTime AND :endTime > s.startTime)")
    List<Schedule> findTimeConflicts(@Param("doctorId") Long doctorId,
                                     @Param("date") LocalDate date,
                                     @Param("startTime") LocalTime startTime,
                                     @Param("endTime") LocalTime endTime);

    // 根据状态查询排班
    List<Schedule> findByStatus(Integer status);

    // 排除自身的时间冲突检测
    @Query("SELECT s FROM Schedule s WHERE s.doctor.id = :doctorId AND s.date = :date " +
            "AND s.status = 1 AND s.id != :excludeId " +
            "AND (:startTime < s.endTime AND :endTime > s.startTime)")
    List<Schedule> findTimeConflictsExcludeSelf(@Param("doctorId") Long doctorId,
                                                @Param("date") LocalDate date,
                                                @Param("startTime") LocalTime startTime,
                                                @Param("endTime") LocalTime endTime,
                                                @Param("excludeId") Long excludeId);

    // ✅ 修正：完善的可预约排班查询（使用小写枚举值）
    @Query("SELECT s FROM Schedule s WHERE s.status = 1 " +
            "AND s.date >= :currentDate " +
            "AND (s.maxPatients IS NULL OR " +
            "(SELECT COUNT(a) FROM Appointment a WHERE a.schedule.id = s.id AND a.status <> 'CANCELLED') < s.maxPatients) " +
            "AND (:departmentId IS NULL OR s.doctor.department.id = :departmentId) " +
            "AND (:date IS NULL OR s.date = :date)")
    List<Schedule> findAvailableSchedules(@Param("departmentId") Long departmentId,
                                          @Param("date") LocalDate date,
                                          @Param("currentDate") LocalDate currentDate);
}