package com.sayrain.medicalbooking.security;

import com.sayrain.medicalbooking.service.impl.AuthServiceImpl;
import com.sayrain.medicalbooking.service.impl.CustomUserDetailsServiceImpl;
import com.sayrain.medicalbooking.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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

        if (token != null && authService.isTokenValid(token) && jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                try {
                    var userDetails = customUserDetailsServiceImpl.loadUserByUsername(username);
                    UsernamePasswordAuthenticationToken authenticationToken =
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                } catch (Exception ex) {
                    log.warn("Failed to authenticate user {}", username, ex);
                }
            }
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
            requestPath.startsWith("/api/ai/chat")) {
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
