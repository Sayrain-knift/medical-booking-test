// File: src/main/java/com/sayrain/medicalbooking/config/SecurityConfig.java
package com.sayrain.medicalbooking.config;

import com.sayrain.medicalbooking.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // 配置 CORS
        http.cors(cors -> cors.configurationSource(corsConfigurationSource()));

        // 禁用 CSRF
        http.csrf(csrf -> csrf.disable());

        // 配置会话管理
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        // 配置授权 - 添加前端静态资源路径
        http.authorizeHttpRequests(authz -> authz
                // 放行前端静态资源路径和HTML页面
                .requestMatchers("/", "/index.html", "/register.html", "/login.html", "/doctors.html", "/appointment.html", "/ai-consultation.html", "/profile.html", "/appointments.html", "/test-pages.html").permitAll()
                .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .requestMatchers("/api/auth/**").permitAll()

                // 🔥 新增：预约接口权限配置
                .requestMatchers(HttpMethod.GET, "/api/appointments/**").hasAnyRole("ADMIN", "DOCTOR", "PATIENT") // 管理员、医生、患者都可以查看
                .requestMatchers(HttpMethod.POST, "/api/appointments/**").hasAnyRole("PATIENT", "ADMIN")          // 患者和管理员可以创建预约
                .requestMatchers(HttpMethod.PUT, "/api/appointments/**").hasAnyRole("ADMIN", "DOCTOR")            // 管理员和医生可以更新预约
                .requestMatchers(HttpMethod.DELETE, "/api/appointments/**").hasAnyRole("ADMIN", "PATIENT")        // 管理员和患者可以取消预约
                .requestMatchers(HttpMethod.PATCH, "/api/appointments/**").hasAnyRole("ADMIN", "DOCTOR")          // 管理员和医生可以更新状态

                // 🔥 新增：排班接口权限配置
                .requestMatchers(HttpMethod.GET, "/api/schedules/**").permitAll()      // 允许公开查看排班
                .requestMatchers(HttpMethod.POST, "/api/schedules/**").hasRole("ADMIN")    // 仅管理员可新增排班
                .requestMatchers(HttpMethod.PUT, "/api/schedules/**").hasRole("ADMIN")     // 仅管理员可修改排班
                .requestMatchers(HttpMethod.DELETE, "/api/schedules/**").hasRole("ADMIN")  // 仅管理员可删除排班
                .requestMatchers(HttpMethod.PATCH, "/api/schedules/**").hasRole("ADMIN")   // 仅管理员可更新状态

                // 🔥 新增：医生接口权限配置
                .requestMatchers(HttpMethod.GET, "/api/doctors/**").permitAll()        // 允许公开查看医生
                .requestMatchers(HttpMethod.POST, "/api/doctors/**").hasRole("ADMIN")      // 仅管理员可新增医生
                .requestMatchers(HttpMethod.PUT, "/api/doctors/**").hasRole("ADMIN")       // 仅管理员可修改医生
                .requestMatchers(HttpMethod.DELETE, "/api/doctors/**").hasRole("ADMIN")    // 仅管理员可删除医生

                // 科室接口
                .requestMatchers(HttpMethod.GET, "/api/departments/**").permitAll()    // 允许公开查看科室
                .requestMatchers(HttpMethod.POST, "/api/departments/**").hasRole("ADMIN")  // 仅管理员可新增
                .requestMatchers(HttpMethod.PUT, "/api/departments/**").hasRole("ADMIN")   // 仅管理员可修改
                .requestMatchers(HttpMethod.DELETE, "/api/departments/**").hasRole("ADMIN")// 仅管理员可删除

                .requestMatchers("/api/ai/chat/**").permitAll() // 放行AI聊天接口
                .requestMatchers("/error").permitAll() // 放行错误页面接口
                .requestMatchers("/actuator/**").permitAll() // 放行actuator监控端点
                .requestMatchers("/swagger-ui.html").permitAll()
                .requestMatchers("/swagger-ui/**").permitAll()
                .requestMatchers("/v3/api-docs/**").permitAll()
                .requestMatchers("/webjars/**").permitAll()
                .requestMatchers("/swagger-resources/**").permitAll()
                .requestMatchers("/configuration/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                .anyRequest().authenticated()
        );

        // 配置 headers - 修复后的正确写法
        http.headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable));

        // 添加 JWT 过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
