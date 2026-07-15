package com.jchotel.aspect;

import com.jchotel.entity.OperationLog;
import com.jchotel.mapper.OperationLogMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 操作日志AOP切面
 * <p>
 * 使用Spring AOP实现Controller层操作日志的自动记录，
 * 拦截所有Controller方法（登录接口除外），记录请求参数、执行结果、
 * 执行耗时、操作人、IP地址等信息，便于审计和问题追踪。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
@Aspect
@Component
public class OperationLogAspect {

    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

    /**
     * 参数/结果最大存储长度，防止数据库字段溢出
     */
    private static final int MAX_LENGTH = 4000;

    /**
     * 操作日志Mapper，用于持久化日志数据
     */
    @Autowired
    private OperationLogMapper operationLogMapper;

    /**
     * JSON对象映射器，用于序列化参数和返回结果
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 切入点定义
     * <p>
     * 拦截com.jchotel.controller包下所有类的所有方法，
     * 排除AuthController.login登录方法（避免记录密码等敏感信息）。
     * </p>
     */
    @Pointcut("execution(* com.jchotel.controller..*.*(..)) && !execution(* com.jchotel.controller.AuthController.login(..))")
    public void controllerPointcut() {}

    /**
     * 环绕通知处理方法
     * <p>
     * 在目标方法执行前后进行拦截，记录操作日志的完整信息：
     * 包括操作人、请求方法、IP、模块、参数、执行结果、耗时等。
     * </p>
     *
     * @param joinPoint 连接点对象，可获取目标方法、参数等信息
     * @return Object 目标方法的返回结果
     * @throws Throwable 目标方法执行过程中可能抛出的异常
     */
    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 记录方法开始时间
        long startTime = System.currentTimeMillis();
        // 创建操作日志对象
        OperationLog opLog = new OperationLog();
        // 设置日志创建时间
        opLog.setCreateTime(LocalDateTime.now());

        // 从RequestContextHolder获取当前请求属性
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            // 从request属性中获取登录拦截器设置的用户信息
            Object userId = request.getAttribute("userId");
            Object username = request.getAttribute("username");
            opLog.setUserId(userId != null ? Long.valueOf(userId.toString()) : null);
            opLog.setUsername(username != null ? username.toString() : null);
            // 记录HTTP请求方法和请求URI
            opLog.setMethod(request.getMethod() + " " + request.getRequestURI());
            // 获取客户端真实IP地址
            opLog.setIp(getClientIp(request));
        }

        // 获取目标类名和方法名
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        // 从类名提取模块名（去掉Controller后缀）
        opLog.setModule(className.replace("Controller", ""));
        opLog.setOperation(methodName);

        // 构建并记录请求参数
        try {
            Object[] args = joinPoint.getArgs();
            String params = buildParams(args, joinPoint);
            opLog.setParams(params);
        } catch (Exception ignored) {
            // 参数构建失败不影响主流程
        }

        Object result;
        try {
            // 执行目标方法
            result = joinPoint.proceed();
            // 执行成功，设置状态为1（成功）
            opLog.setStatus(1);
            // 序列化返回结果
            try {
                String resultStr = objectMapper.writeValueAsString(result);
                if (resultStr.length() > MAX_LENGTH) {
                    resultStr = resultStr.substring(0, MAX_LENGTH);
                }
                opLog.setResult(resultStr);
            } catch (Exception ignored) {
                // 结果序列化失败不影响主流程
            }
        } catch (Throwable e) {
            // 执行失败，设置状态为0（失败）
            opLog.setStatus(0);
            // 记录错误信息
            String err = e.getMessage();
            if (err != null && err.length() > MAX_LENGTH) err = err.substring(0, MAX_LENGTH);
            opLog.setErrorMsg(err);
            // 异常继续向上抛出
            throw e;
        } finally {
            // 计算方法执行耗时
            opLog.setCostTime(System.currentTimeMillis() - startTime);
            // 持久化日志到数据库
            try {
                operationLogMapper.insert(opLog);
            } catch (Exception e) {
                log.error("操作日志写入失败", e);
            }
        }
        return result;
    }

    /**
     * 构建请求参数字符串
     * <p>
     * 将方法参数转换为JSON格式字符串，过滤掉HttpServletRequest/HttpServletResponse等无法序列化的对象，
     * 对MultipartFile文件参数标记为[File]，对大参数进行截断处理。
     * </p>
     *
     * @param args 方法参数数组
     * @param joinPoint 连接点对象，用于获取参数名称
     * @return String 参数的JSON字符串表示
     */
    private String buildParams(Object[] args, ProceedingJoinPoint joinPoint) {
        if (args == null || args.length == 0) return "";

        // 获取参数名称
        String[] paramNames = null;
        try {
            org.aspectj.lang.reflect.MethodSignature sig = (org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature();
            paramNames = sig.getParameterNames();
        } catch (Exception ignored) {
            // 获取参数名失败时使用默认名称
        }

        // 使用有序Map保证参数顺序
        Map<String, Object> paramMap = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            // 跳过Servlet API对象
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) continue;
            // 处理文件上传参数
            if (arg instanceof MultipartFile) {
                paramMap.put(paramNames != null ? paramNames[i] : "param" + i, "[File]");
                continue;
            }
            String key = paramNames != null ? paramNames[i] : "param" + i;
            // 基本类型直接放入，复杂类型序列化为JSON
            if (arg != null && (arg instanceof String || arg instanceof Number || arg instanceof Boolean
                    || arg instanceof java.time.LocalDateTime || arg instanceof java.time.LocalDate)) {
                paramMap.put(key, arg);
            } else {
                try {
                    String json = objectMapper.writeValueAsString(arg);
                    if (json.length() > MAX_LENGTH) json = json.substring(0, MAX_LENGTH);
                    paramMap.put(key, objectMapper.readValue(json, Object.class));
                } catch (Exception e) {
                    // 序列化失败时使用toString
                    paramMap.put(key, arg != null ? arg.toString() : null);
                }
            }
        }
        // 将参数Map序列化为JSON字符串
        try {
            String json = objectMapper.writeValueAsString(paramMap);
            return json.length() > MAX_LENGTH ? json.substring(0, MAX_LENGTH) : json;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 获取客户端真实IP地址
     * <p>
     * 依次从X-Forwarded-For、X-Real-IP请求头获取IP，
     * 最后使用request.getRemoteAddr()获取，
     * 并处理反向代理、IPv6本地地址等情况。
     * </p>
     *
     * @param request HTTP请求对象
     * @return String 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // X-Forwarded-For可能包含多个IP，第一个为真实客户端IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        // 将IPv6本地地址转换为IPv4格式
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            ip = "127.0.0.1";
        }
        return ip;
    }
}
