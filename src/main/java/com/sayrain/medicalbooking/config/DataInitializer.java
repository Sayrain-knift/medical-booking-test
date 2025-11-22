package com.sayrain.medicalbooking.config;

import com.sayrain.medicalbooking.model.Department;
import com.sayrain.medicalbooking.model.Doctor;
import com.sayrain.medicalbooking.model.Schedule;
import com.sayrain.medicalbooking.model.User;
import com.sayrain.medicalbooking.model.Patient;
import com.sayrain.medicalbooking.repository.DepartmentRepository;
import com.sayrain.medicalbooking.repository.DoctorRepository;
import com.sayrain.medicalbooking.repository.ScheduleRepository;
import com.sayrain.medicalbooking.repository.UserRepository;
import com.sayrain.medicalbooking.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@Configuration
@RequiredArgsConstructor
@Slf4j
@Profile("minimal")
public class DataInitializer {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner initData() {
        return args -> {
            log.info("开始初始化基础数据...");
            
            // 初始化用户数据
            if (userRepository.count() == 0) {
                initializeUsers();
            }
            
            // 初始化科室数据
            if (departmentRepository.count() == 0) {
                initializeDepartments();
            }
            
            // 初始化医生数据
            if (doctorRepository.count() == 0) {
                initializeDoctors();
            }
            
            // 初始化排班数据
            if (scheduleRepository.count() == 0) {
                initializeSchedules();
            }
            
            log.info("基础数据初始化完成！");
        };
    }

    private void initializeDepartments() {
        List<Department> departments = Arrays.asList(
            createDepartment("内科", "心血管、消化、呼吸等内科疾病", "bi-heart-pulse", 1),
            createDepartment("外科", "普外、骨科、神经外科等", "bi-activity", 2),
            createDepartment("妇产科", "妇科疾病、产科护理", "bi-person-heart", 3),
            createDepartment("儿科", "儿童常见疾病、预防保健", "bi-emoji-smile", 4),
            createDepartment("眼科", "眼部疾病、视力矫正", "bi-eye", 5),
            createDepartment("耳鼻喉科", "耳鼻喉疾病治疗", "bi-ear", 6),
            createDepartment("口腔科", "口腔疾病、牙齿矫正", "bi-mouth", 7),
            createDepartment("皮肤科", "皮肤疾病、美容护肤", "bi-patch-check", 8),
            createDepartment("中医科", "中医诊疗、针灸推拿", "bi-yin-yang", 9),
            createDepartment("心理科", "心理咨询、精神健康", "bi-emoji-smile", 10)
        );

        departmentRepository.saveAll(departments);
        log.info("初始化了 {} 个科室", departments.size());
    }

    private Department createDepartment(String name, String description, String icon, int sortOrder) {
        Department department = new Department();
        department.setName(name);
        department.setDescription(description);
        department.setIcon(icon);
        department.setSortOrder(sortOrder);
        department.setStatus(1);
        department.setParentId(0L);
        return department;
    }

    private void initializeDoctors() {
        List<Department> departments = departmentRepository.findAll();
        if (departments.isEmpty()) {
            log.warn("没有找到科室数据，跳过医生初始化");
            return;
        }

        String[] titles = {"主任医师", "副主任医师", "主治医师", "住院医师"};
        String[][] specialties = {
            {"心血管疾病", "高血压", "冠心病", "心律失常"},
            {"普外手术", "创伤治疗", "微创手术", "急诊外科"},
            {"妇科炎症", "产科护理", "不孕不育", "内分泌失调"},
            {"儿童感冒", "疫苗接种", "儿童营养", "发育评估"},
            {"近视矫正", "白内障", "青光眼", "眼底疾病"},
            {"鼻炎", "中耳炎", "扁桃体炎", "喉部疾病"},
            {"牙齿矫正", "口腔修复", "牙周治疗", "种植牙"},
            {"湿疹治疗", "痤疮治疗", "皮肤美容", "性病防治"},
            {"针灸推拿", "中药调理", "中医内科", "中医外科"},
            {"抑郁症", "焦虑症", "心理疏导", "睡眠障碍"}
        };

        List<Doctor> doctors = new ArrayList<>();
        int doctorId = 1;

        for (int i = 0; i < departments.size(); i++) {
            Department dept = departments.get(i);
            
            // 每个科室创建3-5个医生
            int doctorCount = 3 + (int) (Math.random() * 3);
            for (int j = 0; j < doctorCount; j++) {
                Doctor doctor = new Doctor();
                doctor.setName("医生" + doctorId);
                doctor.setTitle(titles[Math.min(j, titles.length - 1)]);
                doctor.setDepartment(dept);
                doctor.setSpecialty(String.join("、", specialties[i]));
                doctor.setDescription("具有丰富的临床经验，专注于" + dept.getName() + "相关疾病的诊疗。");
                doctor.setImageUrl("https://via.placeholder.com/300x250/0056b3/ffffff?text=医生" + doctorId);
                doctor.setStatus(1);
                
                doctors.add(doctor);
                doctorId++;
            }
        }

        doctorRepository.saveAll(doctors);
        log.info("初始化了 {} 个医生", doctors.size());
    }

    private void initializeSchedules() {
        List<Doctor> doctors = doctorRepository.findAll();
        if (doctors.isEmpty()) {
            log.warn("没有找到医生数据，跳过排班初始化");
            return;
        }

        List<Schedule> schedules = new ArrayList<>();
        LocalDate today = LocalDate.now();
        Random random = new Random();

        // 为每个医生创建未来7天的排班
        for (Doctor doctor : doctors) {
            for (int i = 0; i < 7; i++) {
                LocalDate scheduleDate = today.plusDays(i);
                
                // 每天创建1-2个时间段
                int slotCount = 1 + random.nextInt(2);
                for (int j = 0; j < slotCount; j++) {
                    Schedule schedule = new Schedule();
                    schedule.setDoctor(doctor);
                    schedule.setDate(scheduleDate);
                    
                    // 随机生成时间段
                    if (j == 0) {
                        // 上午时段
                        schedule.setStartTime(LocalTime.of(8 + random.nextInt(2), 0)); // 8:00-9:00
                        schedule.setEndTime(LocalTime.of(11 + random.nextInt(2), 0));   // 11:00-12:00
                    } else {
                        // 下午时段
                        schedule.setStartTime(LocalTime.of(14 + random.nextInt(2), 0)); // 14:00-15:00
                        schedule.setEndTime(LocalTime.of(16 + random.nextInt(2), 0));   // 16:00-17:00
                    }
                    
                    // 设置最大预约人数 - 增加容量确保有可预约的时段
                    schedule.setMaxPatients(10 + random.nextInt(11)); // 10-20人
                    schedule.setStatus(1); // 启用状态
                    
                    schedules.add(schedule);
                }
            }
        }

        scheduleRepository.saveAll(schedules);
        log.info("初始化了 {} 个排班", schedules.size());
    }

    private void initializeUsers() {
        // 创建测试患者用户
        User testPatient = new User();
        testPatient.setUsername("testuser");
        testPatient.setPassword(passwordEncoder.encode("123456"));
        testPatient.setEmail("test@example.com");
        testPatient.setPhone("13800138000");
        testPatient.setRole(User.UserRole.PATIENT);

        // 创建管理员用户
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("123456"));
        admin.setEmail("admin@example.com");
        admin.setPhone("13900139000");
        admin.setRole(User.UserRole.ADMIN);

        // 保存用户
        List<User> savedUsers = userRepository.saveAll(Arrays.asList(testPatient, admin));
        
        // 为患者用户创建对应的Patient记录
         for (User user : savedUsers) {
             if (user.getRole() == User.UserRole.PATIENT) {
                 Patient patient = new Patient();
                 patient.setUser(user);
                 patient.setName("测试患者");
                 patient.setPhone("13800138000");
                 patient.setHealthCard("HC123456789");
                 patientRepository.save(patient);
             }
         }
        
        log.info("初始化了 2 个用户和对应的Patient记录");
    }
}