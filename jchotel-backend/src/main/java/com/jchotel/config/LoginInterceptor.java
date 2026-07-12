package com.jchotel.config;

import com.jchotel.annotation.RequireRole;
import com.jchotel.utils.JwtUtil;
import com.jchotel.utils.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    private static final long REFRESH_THRESHOLD_MS = 30 * 60 * 1000;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtil.validateToken(token)) {
                Claims claims = jwtUtil.parseToken(token);
                Long userId = Long.valueOf(claims.getSubject());
                String username = (String) claims.get("username");
                String role = (String) claims.get("role");

                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("role", role);

                Date expiration = claims.getExpiration();
                if (expiration != null && expiration.getTime() - System.currentTimeMillis() < REFRESH_THRESHOLD_MS) {
                    String newToken = jwtUtil.generateToken(userId, username, role != null ? role : "receptionist");
                    response.setHeader("X-New-Token", newToken);
                }

                if (handler instanceof HandlerMethod) {
                    HandlerMethod hm = (HandlerMethod) handler;
                    Method method = hm.getMethod();
                    RequireRole methodAnnotation = method.getAnnotation(RequireRole.class);
                    RequireRole classAnnotation = hm.getBeanType().getAnnotation(RequireRole.class);

                    RequireRole effective = methodAnnotation != null ? methodAnnotation : classAnnotation;
                    if (effective != null) {
                        List<String> requiredRoles = Arrays.asList(effective.value());
                        if (role == null || !requiredRoles.contains(role)) {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            ObjectMapper mapper = new ObjectMapper();
                            mapper.writeValue(response.getOutputStream(), Result.error(403, "权限不足，需要角色：" + String.join("/", requiredRoles)));
                            return false;
                        }
                    }
                }

                return true;
            }
        }

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), Result.error(401, "未登录或登录已过期"));
        return false;
    }
}
