// File: src/test/java/com/sayrain/medicalbooking/integration/AppointmentFlowIntegrationTest.java
package com.sayrain.medicalbooking.integration;

import static org.junit.jupiter.api.Assertions.*;
import com.sayrain.medicalbooking.dto.*;
import com.sayrain.medicalbooking.model.*;
import com.sayrain.medicalbooking.repository.*;
import com.sayrain.medicalbooking.util.RedisService;
import com.sayrain.medicalbooking.util.ResponseResult;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {
                "spring.cloud.nacos.discovery.enabled=false",
                "spring.cloud.nacos.discovery.register-enabled=false",
                "spring.cloud.nacos.config.enabled=false",
                "spring.cloud.compatibility-verifier.enabled=false"
        })
@ActiveProfiles("test")
// 移除 @Transactional 注解，改为手动管理数据
class AppointmentFlowIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private RedisService redisService;

    @TestConfiguration
    static class TestConfig {
        @Bean
        @Primary
        public RedisService redisService() {
            return mock(RedisService.class);
        }
    }

    private Department savedDepartment;
    private Doctor savedDoctor;
    private Schedule savedSchedule;
    private Long departmentId;
    private Long doctorId;
    private Long scheduleId;

    private String testUsername = "patient1_" + System.currentTimeMillis();

    @BeforeEach
    void setUp() {
        // 清理数据 - 使用 deleteAllInBatch 避免事务问题
        appointmentRepository.deleteAllInBatch();
        scheduleRepository.deleteAllInBatch();
        doctorRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        patientRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // 1) 创建科室
        Department dept = new Department();
        dept.setName("内科_" + System.currentTimeMillis()); // 确保唯一性
        dept.setDescription("内科描述");
        dept.setStatus(1);
        dept.setSortOrder(1);
        savedDepartment = departmentRepository.save(dept);
        departmentId = savedDepartment.getId();
        assertNotNull(departmentId);

        // 2) 创建医生并关联科室
        Doctor doc = new Doctor();
        doc.setName("测试医生_" + System.currentTimeMillis());
        doc.setTitle("主任医师");
        doc.setDepartment(savedDepartment);
        doc.setStatus(1);
        doc.setDescription("擅长内科");
        savedDoctor = doctorRepository.save(doc);
        doctorId = savedDoctor.getId();
        assertNotNull(doctorId);

        // 3) 创建排班
        Schedule schedule = new Schedule();
        schedule.setDoctor(savedDoctor);
        schedule.setDate(LocalDate.now().plusDays(2));
        schedule.setStartTime(LocalTime.of(9, 0));
        schedule.setEndTime(LocalTime.of(11, 0));
        schedule.setMaxPatients(10);
        schedule.setStatus(1);
        savedSchedule = scheduleRepository.save(schedule);
        scheduleId = savedSchedule.getId();
        assertNotNull(scheduleId);

        // 配置 Mock RedisService
        DepartmentDTO deptDto = new DepartmentDTO();
        deptDto.setId(departmentId);
        deptDto.setName(savedDepartment.getName());
        deptDto.setDescription("内科描述");
        deptDto.setStatus(1);
        deptDto.setSortOrder(1);

        List<DepartmentDTO> cachedDepartments = Arrays.asList(deptDto);

        when(redisService.getCachedDepartments()).thenReturn(cachedDepartments);
        when(redisService.cacheDepartment(any(Department.class))).thenReturn(true);
        when(redisService.clearDepartmentCache()).thenReturn(true);
        when(redisService.cacheDepartment(anyList())).thenReturn(true);
        
        // 配置医生缓存相关的mock
        when(redisService.getCachedDoctorsByDepartment(departmentId)).thenReturn(null); // 模拟缓存中没有数据
        when(redisService.cacheDoctorsByDepartment(any(Long.class), any(List.class))).thenReturn(true);
        when(redisService.clearDoctorCache(any(Long.class))).thenReturn(true);
    }

    @AfterEach
    void tearDown() {
        // 测试结束后清理数据
        appointmentRepository.deleteAllInBatch();
        scheduleRepository.deleteAllInBatch();
        doctorRepository.deleteAllInBatch();
        departmentRepository.deleteAllInBatch();
        patientRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    void testCompleteAppointmentFlow() {
        // -----------------------
        // 1) 注册患者用户
        // -----------------------
        RegisterRequest registerReq = new RegisterRequest();
        registerReq.setUsername(testUsername);
        registerReq.setPassword("pass1234");
        registerReq.setEmail(testUsername + "@example.com");
        registerReq.setPhone("10086");
        registerReq.setRole(User.UserRole.PATIENT);

        ResponseEntity<AuthResponse> registerResp = restTemplate.postForEntity(
                "/api/auth/register",
                registerReq,
                AuthResponse.class
        );

        assertTrue(registerResp.getStatusCode().is2xxSuccessful(),
                "注册接口应返回2xx，实际返回: " + registerResp.getStatusCode());
        assertNotNull(registerResp.getBody(), "注册响应体不应为null");
        assertEquals("User registered successfully!", registerResp.getBody().getMessage());

        // 从数据库读取用户 ID
        User savedUser = userRepository.findByUsername(testUsername).orElseThrow(() ->
                new IllegalStateException("注册后未找到用户"));

        // 创建对应的Patient记录
        Patient patient = new Patient();
        patient.setUser(savedUser);  // 关联User实体
        patient.setName(testUsername);
        patient.setPhone(registerReq.getPhone());
        patient.setHealthCard("HC" + savedUser.getId());
        patient = patientRepository.save(patient);
        assertNotNull(patient.getId(), "患者ID不应为null");

        // -----------------------
        // 2) 登录并获取 JWT Token
        // -----------------------
        LoginRequest loginReq = new LoginRequest();
        loginReq.setUsername(testUsername);
        loginReq.setPassword("pass1234");

        ResponseEntity<AuthResponse> loginResp = restTemplate.postForEntity(
                "/api/auth/login",
                loginReq,
                AuthResponse.class
        );

        assertEquals(HttpStatus.OK, loginResp.getStatusCode(),
                "登录应返回200，实际返回: " + loginResp.getStatusCode());
        assertNotNull(loginResp.getBody(), "登录响应体不应为null");
        String token = loginResp.getBody().getToken();
        assertNotNull(token, "登录应返回 JWT token");

        // 生成带 Bearer 的 headers 供后续请求使用
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // -----------------------
        // 3) 查询所有科室 - 直接使用已知ID
        // -----------------------
        Long deptId = departmentId;

        // -----------------------
        // 4) 根据科室ID查询医生
        // -----------------------
        ResponseEntity<ResponseResult<List<Doctor>>> doctorsResp = restTemplate.exchange(
                "/api/doctors/department/" + deptId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ResponseResult<List<Doctor>>>() {
                }
        );

        // 打印响应信息用于调试
        System.out.println("=== 医生查询响应调试信息 ===");
        System.out.println("响应状态码: " + doctorsResp.getStatusCode());
        System.out.println("响应头: " + doctorsResp.getHeaders());
        if (doctorsResp.getBody() != null) {
            System.out.println("响应体: " + doctorsResp.getBody());
            System.out.println("响应数据: " + doctorsResp.getBody().getData());
        } else {
            System.out.println("响应体为null");
        }
        System.out.println("科室ID: " + deptId);
        System.out.println("========================");

        // 如果医生列表为空，可能是数据还未同步，我们重试几次
        List<Doctor> doctorList = null;
        for (int i = 0; i < 3; i++) {
            System.out.println("=== 重试第 " + (i + 1) + " 次查询医生 ===");
            doctorsResp = restTemplate.exchange(
                    "/api/doctors/department/" + deptId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<ResponseResult<List<Doctor>>>() {
                    }
            );

            System.out.println("状态码: " + doctorsResp.getStatusCode());
            if (doctorsResp.getBody() != null) {
                System.out.println("响应体: " + doctorsResp.getBody());
                System.out.println("响应code: " + doctorsResp.getBody().getCode());
                System.out.println("响应message: " + doctorsResp.getBody().getMessage());
                System.out.println("响应数据: " + doctorsResp.getBody().getData());
            } else {
                System.out.println("响应体为null");
            }

            if (doctorsResp.getStatusCode() == HttpStatus.OK &&
                    doctorsResp.getBody() != null &&
                    doctorsResp.getBody().getData() != null &&
                    !doctorsResp.getBody().getData().isEmpty()) {
                doctorList = doctorsResp.getBody().getData();
                System.out.println("找到医生列表，大小: " + doctorList.size());
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        assertNotNull(doctorList, "医生列表不应为null");
        assertFalse(doctorList.isEmpty(), "医生列表不应为空");

        Long actualDoctorId = doctorList.get(0).getId();
        assertEquals(savedDoctor.getName(), doctorList.get(0).getName(), "医生名称应该匹配");

        // -----------------------
        // 5) 根据医生ID查询排班
        // -----------------------
        ResponseEntity<ResponseResult<List<ScheduleDTO>>> schedulesResp = restTemplate.exchange(
                "/api/schedules/doctor/" + actualDoctorId,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ResponseResult<List<ScheduleDTO>>>() {
                }
        );

        List<ScheduleDTO> scheduleList = null;
        for (int i = 0; i < 3; i++) {
            schedulesResp = restTemplate.exchange(
                    "/api/schedules/doctor/" + actualDoctorId,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    new ParameterizedTypeReference<ResponseResult<List<ScheduleDTO>>>() {
                    }
            );

            if (schedulesResp.getStatusCode() == HttpStatus.OK &&
                    schedulesResp.getBody() != null &&
                    schedulesResp.getBody().getData() != null &&
                    !schedulesResp.getBody().getData().isEmpty()) {
                scheduleList = schedulesResp.getBody().getData();
                break;
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        assertNotNull(scheduleList, "排班列表不应为null");
        assertFalse(scheduleList.isEmpty(), "排班列表不应为空");

        Long actualScheduleId = scheduleList.get(0).getId();
        assertEquals(savedSchedule.getDate(), scheduleList.get(0).getDate(), "排班日期应该匹配");

        // -----------------------
        // 6) 创建预约 - 修复：只发送一次请求
        // -----------------------
        AppointmentDTO createReq = new AppointmentDTO();
        createReq.setPatientId(patient.getId());
        createReq.setScheduleId(actualScheduleId);

        HttpEntity<AppointmentDTO> createEntity = new HttpEntity<>(createReq, headers);

        // 添加详细的调试信息
        System.out.println("=== 创建预约请求 ===");
        System.out.println("患者ID: " + patient.getId());
        System.out.println("排班ID: " + actualScheduleId);
        System.out.println("请求头: " + headers);

        // 只发送一次请求，使用 ParameterizedTypeReference 接收
        ResponseEntity<ResponseResult<Appointment>> createResp = restTemplate.exchange(
                "/api/appointments",
                HttpMethod.POST,
                createEntity,
                new ParameterizedTypeReference<ResponseResult<Appointment>>() {
                }
        );

        // 打印响应状态和内容用于调试
        System.out.println("=== 创建预约响应 ===");
        System.out.println("状态码: " + createResp.getStatusCode());
        System.out.println("响应体: " + createResp.getBody());

        // 如果状态码不是2xx，直接失败
        if (!createResp.getStatusCode().is2xxSuccessful()) {
            fail("创建预约失败，状态码: " + createResp.getStatusCode() + ", 响应: " + createResp.getBody());
        }

        assertTrue(createResp.getStatusCode().is2xxSuccessful(),
                "创建预约应返回2xx，实际返回: " + createResp.getStatusCode());
        assertNotNull(createResp.getBody(), "创建预约响应体不应为null");

        // 这里添加更详细的检查
        ResponseResult<Appointment> responseBody = createResp.getBody();
        System.out.println("响应码: " + responseBody.getCode());
        System.out.println("响应消息: " + responseBody.getMessage());
        System.out.println("响应数据: " + responseBody.getData());

        // 检查业务响应码是否为200
        assertEquals(200, responseBody.getCode(), "业务响应码应为200");

        Appointment created = responseBody.getData();
        assertNotNull(created, "创建的预约不应为null");
        assertNotNull(created.getId(), "创建的预约应具有ID");
        Long appointmentId = created.getId();

        // -----------------------
        // 7) 查询我的预约列表
        // -----------------------
        ResponseEntity<ResponseResult<List<Appointment>>> myApptsResp = restTemplate.exchange(
                "/api/appointments/my-appointments",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ResponseResult<List<Appointment>>>() {}
        );
        assertEquals(HttpStatus.OK, myApptsResp.getStatusCode(),
                "查询我的预约应返回200，实际返回: " + myApptsResp.getStatusCode());
        assertNotNull(myApptsResp.getBody(), "我的预约响应体不应为null");
        assertNotNull(myApptsResp.getBody().getData(), "我的预约数据不应为null");

        List<Appointment> myList = myApptsResp.getBody().getData();
        assertFalse(myList.isEmpty(), "我的预约列表不应为空");
        boolean found = myList.stream().anyMatch(a -> appointmentId.equals(a.getId()));
        assertTrue(found, "我的预约列表应包含刚创建的预约");

        // -----------------------
        // 8) 取消预约（删除）
        // -----------------------
        ResponseEntity<ResponseResult<Void>> delResp = restTemplate.exchange(
                "/api/appointments/" + appointmentId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ResponseResult<Void>>() {}
        );
        assertTrue(delResp.getStatusCode().is2xxSuccessful(),
                "删除预约应返回2xx，实际返回: " + delResp.getStatusCode());

        // 再次确认 my-appointments 中不包含该预约
        ResponseEntity<ResponseResult<List<Appointment>>> myApptsAfterResp = restTemplate.exchange(
                "/api/appointments/my-appointments",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                new ParameterizedTypeReference<ResponseResult<List<Appointment>>>() {}
        );
        assertEquals(HttpStatus.OK, myApptsAfterResp.getStatusCode(),
                "再次查询我的预约应返回200，实际返回: " + myApptsAfterResp.getStatusCode());
        assertNotNull(myApptsAfterResp.getBody(), "再次查询响应体不应为null");
        assertNotNull(myApptsAfterResp.getBody().getData(), "再次查询预约数据不应为null");

        List<Appointment> myAfterList = myApptsAfterResp.getBody().getData();
        boolean stillExists = myAfterList.stream().anyMatch(a -> appointmentId.equals(a.getId()));
        assertFalse(stillExists, "取消后我的预约列表不应包含该预约");
    }
}
