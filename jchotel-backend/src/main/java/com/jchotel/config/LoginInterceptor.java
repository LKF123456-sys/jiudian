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

/**
 * 登录认证与权限拦截器
 * <p>
 * 实现Spring MVC的HandlerInterceptor接口，在请求到达Controller之前进行统一拦截处理，
 * 主要功能包括：
 * <ul>
 *   <li>JWT令牌有效性验证</li>
 *   <li>解析用户信息并传递到后续请求处理</li>
 *   <li>令牌接近过期时自动刷新</li>
 *   <li>基于@RequireRole注解的接口权限控制</li>
 * </ul>
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    /**
     * Token自动刷新阈值（毫秒）
     * 当Token剩余有效期小于此值时，自动生成新Token返回给前端
     * 默认设置为30分钟
     */
    private static final long REFRESH_THRESHOLD_MS = 30 * 60 * 1000;

    /**
     * JWT工具类实例
     * 用于JWT令牌的生成、验证、解析等操作
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 请求预处理方法
     * <p>
     * 在Controller方法执行前进行拦截，完成登录验证、用户信息解析、权限校验、Token自动刷新等核心逻辑。
     * </p>
     *
     * @param request  HTTP请求对象，用于获取请求头、传递用户属性
     * @param response HTTP响应对象，用于返回错误信息和新Token
     * @param handler  当前请求对应的处理器对象（通常是HandlerMethod）
     * @return boolean true表示放行继续执行后续逻辑，false表示中断请求
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理CORS跨域预检请求，OPTIONS请求直接放行不做校验
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头的Authorization字段获取Token
        String token = request.getHeader("Authorization");
        // 判断Token是否存在且符合Bearer格式
        if (token != null && token.startsWith("Bearer ")) {
            // 截取"Bearer "前缀后的实际Token字符串
            token = token.substring(7);
            // 验证Token的有效性（签名正确且未过期）
            if (jwtUtil.validateToken(token)) {
                // 解析Token获取载荷信息
                Claims claims = jwtUtil.parseToken(token);
                // 从subject中获取用户ID
                Long userId = Long.valueOf(claims.getSubject());
                // 从自定义声明中获取用户名
                String username = (String) claims.get("username");
                // 从自定义声明中获取用户角色
                String role = (String) claims.get("role");

                // 将用户信息存入request属性，供后续Controller和Service使用
                request.setAttribute("userId", userId);
                request.setAttribute("username", username);
                request.setAttribute("role", role);

                // 获取Token的过期时间
                Date expiration = claims.getExpiration();
                // 判断是否需要刷新Token：剩余有效期小于阈值时自动刷新
                if (expiration != null && expiration.getTime() - System.currentTimeMillis() < REFRESH_THRESHOLD_MS) {
                    // 生成新的Token，默认角色为前台接待员
                    String newToken = jwtUtil.generateToken(userId, username, role != null ? role : "receptionist");
                    // 将新Token放入响应头，前端可读取并更新本地存储
                    response.setHeader("X-New-Token", newToken);
                }

                // 判断当前处理器是否为Controller方法（排除静态资源等）
                if (handler instanceof HandlerMethod) {
                    HandlerMethod hm = (HandlerMethod) handler;
                    Method method = hm.getMethod();
                    // 获取方法上的@RequireRole权限注解
                    RequireRole methodAnnotation = method.getAnnotation(RequireRole.class);
                    // 获取类上的@RequireRole权限注解
                    RequireRole classAnnotation = hm.getBeanType().getAnnotation(RequireRole.class);

                    // 优先使用方法级别的注解，方法没有注解时使用类级别的注解
                    RequireRole effective = methodAnnotation != null ? methodAnnotation : classAnnotation;
                    if (effective != null) {
                        // 获取允许访问的角色列表
                        List<String> requiredRoles = Arrays.asList(effective.value());
                        // 校验当前用户角色是否在允许列表中
                        if (role == null || !requiredRoles.contains(role)) {
                            // 设置响应内容类型为JSON，编码为UTF-8
                            response.setContentType("application/json;charset=UTF-8");
                            // 设置HTTP状态码为403 Forbidden（禁止访问）
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            ObjectMapper mapper = new ObjectMapper();
                            // 返回权限不足的错误信息
                            mapper.writeValue(response.getOutputStream(), Result.error(403, "权限不足，需要角色：" + String.join("/", requiredRoles)));
                            return false;
                        }
                    }
                }

                // 所有校验通过，放行请求
                return true;
            }
        }

        // Token无效或不存在，返回401未授权错误
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), Result.error(401, "未登录或登录已过期"));
        return false;
    }
}
