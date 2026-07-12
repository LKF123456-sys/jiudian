package com.jchotel.config; // 定义包名，config包存放配置类

// 项目自定义类
import com.jchotel.annotation.RequireRole; // 角色要求注解
import com.jchotel.utils.JwtUtil; // JWT工具类
import com.jchotel.utils.Result; // 统一返回结果类
// Jackson JSON处理
import com.fasterxml.jackson.databind.ObjectMapper; // JSON对象映射器
// JWT相关
import io.jsonwebtoken.Claims; // JWT声明（载荷）
// Spring注解
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.stereotype.Component; // Spring组件注解
// Spring MVC相关
import org.springframework.web.method.HandlerMethod; // 处理器方法封装类
import org.springframework.web.servlet.HandlerInterceptor; // Spring MVC拦截器接口

// Jakarta Servlet API
import jakarta.servlet.http.HttpServletRequest; // HTTP请求对象
import jakarta.servlet.http.HttpServletResponse; // HTTP响应对象
// Java反射
import java.lang.reflect.Method; // 反射方法类
// Java工具类
import java.util.Arrays; // 数组工具类
import java.util.Date; // 日期类
import java.util.List; // 列表接口

/**
 * 登录拦截器
 * 实现HandlerInterceptor接口，用于：
 * 1. JWT令牌验证
 * 2. 用户信息解析与传递
 * 3. 令牌自动刷新（接近过期时）
 * 4. 基于@RequireRole注解的权限控制
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
@Component // 标记为Spring组件，交由Spring容器管理
public class LoginInterceptor implements HandlerInterceptor { // 实现Spring MVC拦截器接口

    private static final long REFRESH_THRESHOLD_MS = 30 * 60 * 1000; // Token刷新阈值：剩余有效期小于30分钟时自动刷新

    @Autowired // Spring自动注入JWT工具类
    private JwtUtil jwtUtil; // JWT工具类实例，用于令牌的生成、验证、解析

    /**
     * 请求预处理方法
     * 在Controller方法执行前进行拦截处理，完成登录验证、权限校验、令牌刷新等逻辑
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  当前请求的处理器（通常是HandlerMethod）
     * @return true表示放行继续执行，false表示中断请求
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override // 重写接口方法
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) { // 处理OPTIONS预检请求（CORS跨域时浏览器会先发OPTIONS请求）
            return true; // OPTIONS请求直接放行，不做拦截
        } // 结束预检请求判断

        String token = request.getHeader("Authorization"); // 从请求头获取Authorization字段
        if (token != null && token.startsWith("Bearer ")) { // 判断token是否存在且以Bearer开头（JWT标准格式）
            token = token.substring(7); // 截取Bearer后面的实际token字符串（"Bearer "共7个字符）
            if (jwtUtil.validateToken(token)) { // 验证token是否有效（未过期、签名正确）
                Claims claims = jwtUtil.parseToken(token); // 解析token获取声明信息
                Long userId = Long.valueOf(claims.getSubject()); // 从subject获取用户ID（Long类型）
                String username = (String) claims.get("username"); // 从自定义声明中获取用户名
                String role = (String) claims.get("role"); // 从自定义声明中获取用户角色

                request.setAttribute("userId", userId); // 将用户ID存入request属性，供后续Controller使用
                request.setAttribute("username", username); // 将用户名存入request属性
                request.setAttribute("role", role); // 将角色存入request属性

                Date expiration = claims.getExpiration(); // 获取token过期时间
                if (expiration != null && expiration.getTime() - System.currentTimeMillis() < REFRESH_THRESHOLD_MS) { // 判断是否需要刷新token（剩余有效期小于阈值）
                    String newToken = jwtUtil.generateToken(userId, username, role != null ? role : "receptionist"); // 生成新token，默认角色为前台
                    response.setHeader("X-New-Token", newToken); // 将新token放到响应头X-New-Token中，前端可读取更新
                } // 结束token刷新判断

                if (handler instanceof HandlerMethod) { // 判断当前处理器是否是Controller方法（排除静态资源等）
                    HandlerMethod hm = (HandlerMethod) handler; // 将handler强转为HandlerMethod
                    Method method = hm.getMethod(); // 获取对应的Method对象
                    RequireRole methodAnnotation = method.getAnnotation(RequireRole.class); // 获取方法上的@RequireRole注解
                    RequireRole classAnnotation = hm.getBeanType().getAnnotation(RequireRole.class); // 获取类上的@RequireRole注解

                    RequireRole effective = methodAnnotation != null ? methodAnnotation : classAnnotation; // 优先使用方法上的注解，方法没有则使用类上的
                    if (effective != null) { // 如果存在角色要求注解
                        List<String> requiredRoles = Arrays.asList(effective.value()); // 获取注解中指定的允许角色列表
                        if (role == null || !requiredRoles.contains(role)) { // 判断当前用户角色是否在允许列表中
                            response.setContentType("application/json;charset=UTF-8"); // 设置响应内容类型为JSON
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 设置HTTP状态码为403（禁止访问）
                            ObjectMapper mapper = new ObjectMapper(); // 创建JSON对象映射器
                            mapper.writeValue(response.getOutputStream(), Result.error(403, "权限不足，需要角色：" + String.join("/", requiredRoles))); // 写入权限不足的错误响应
                            return false; // 拦截请求，不继续执行
                        } // 结束角色校验失败判断
                    } // 结束角色要求存在判断
                } // 结束HandlerMethod判断

                return true; // 验证通过，放行请求
            } // 结束token有效判断
        } // 结束token存在且格式正确判断

        response.setContentType("application/json;charset=UTF-8"); // 设置响应内容类型为JSON
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 设置HTTP状态码为401（未授权）
        ObjectMapper mapper = new ObjectMapper(); // 创建JSON对象映射器
        mapper.writeValue(response.getOutputStream(), Result.error(401, "未登录或登录已过期")); // 写入未登录的错误响应
        return false; // 拦截请求，返回未登录错误
    } // 结束preHandle方法
} // 结束LoginInterceptor类
