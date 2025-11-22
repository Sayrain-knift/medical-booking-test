package com.sayrain.medicalbooking.security;

import com.sayrain.medicalbooking.service.impl.AuthServiceImpl;
import com.sayrain.medicalbooking.service.impl.CustomUserDetailsServiceImpl;
import com.sayrain.medicalbooking.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsServiceImpl customUserDetailsServiceImpl;

    @Autowired
    private AuthServiceImpl authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        
        // 跳过静态资源和公开路径的JWT验证
        if (shouldSkipAuthentication(requestPath)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        // 添加详细调试日志
        System.out.println("🔐 JWT Filter - 请求 URI: " + request.getRequestURI());
        System.out.println("🔐 JWT Filter - Authorization Header: " + request.getHeader("Authorization"));
        System.out.println("🔐 JWT Filter - 提取的 Token: " + (token != null ? token.substring(0, Math.min(20, token.length())) + "..." : "null"));

        if (token != null) {
            System.out.println("🔐 JWT Filter - 开始验证 Token...");

            // 首先从Redis缓存检查token是否有效
            if (authService.isTokenValid(token)) {
                // Redis验证通过，再进行JWT验证
                if (jwtUtil.validateToken(token)) {
                    String username = jwtUtil.extractUsername(token);
                    System.out.println("🔐 JWT Filter - Token 验证成功，用户名: " + username);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        try {
                            // 从CustomUserDetailsService加载用户详情
                            var userDetails = customUserDetailsServiceImpl.loadUserByUsername(username);
                            System.out.println("🔐 JWT Filter - 用户详情加载成功: " + userDetails.getUsername());
                            System.out.println("🔐 JWT Filter - 用户权限: " + userDetails.getAuthorities());

                            // 修正：使用userDetails作为principal
                            UsernamePasswordAuthenticationToken authenticationToken =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                            System.out.println("🔐 JWT Filter - SecurityContext 设置成功");

                        } catch (Exception e) {
                            System.out.println("❌ JWT Filter - 用户详情加载失败: " + e.getMessage());
                            e.printStackTrace();
                        }
                    } else {
                        System.out.println("⚠️ JWT Filter - 用户名为空或已有认证信息");
                    }
                } else {
                    System.out.println("❌ JWT Filter - JWT Token 验证失败");
                }
            } else {
                System.out.println("❌ JWT Filter - Redis Token 验证失败（可能已登出或过期）");
            }
        } else {
            System.out.println("⚠️ JWT Filter - 未找到 Token");
        }

        // 检查最终的认证状态
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            System.out.println("✅ JWT Filter - 最终认证状态: 已认证 - " +
                    SecurityContextHolder.getContext().getAuthentication().getName() + " - " +
                    SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        } else {
            System.out.println("❌ JWT Filter - 最终认证状态: 未认证");
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 判断是否应该跳过JWT验证
     */
    private boolean shouldSkipAuthentication(String requestPath) {
        // 跳过静态资源
        if (requestPath.startsWith("/static/") || 
            requestPath.startsWith("/css/") || 
            requestPath.startsWith("/js/") || 
            requestPath.startsWith("/images/") ||
            requestPath.equals("/favicon.ico")) {
            return true;
        }
        
        // 跳过HTML页面
        if (requestPath.endsWith(".html") || 
            requestPath.equals("/") ||
            requestPath.startsWith("/@vite/")) {
            return true;
        }
        
        // 跳过公开API
        if (requestPath.startsWith("/api/auth/") || 
            requestPath.startsWith("/api/departments") ||
            requestPath.startsWith("/api/doctors") ||
            requestPath.startsWith("/api/schedules") ||
            requestPath.equals("/api/chat/ask")) {
            return true;
        }
        
        // 跳过Swagger和其他公开路径
        if (requestPath.startsWith("/swagger-ui/") || 
            requestPath.startsWith("/v3/api-docs/") ||
            requestPath.startsWith("/webjars/") ||
            requestPath.startsWith("/swagger-resources/") ||
            requestPath.startsWith("/configuration/") ||
            requestPath.startsWith("/h2-console/") ||
            requestPath.equals("/error")) {
            return true;
        }
        
        return false;
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}