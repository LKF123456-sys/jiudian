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

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);
    private static final int MAX_LENGTH = 4000;

    @Autowired
    private OperationLogMapper operationLogMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Pointcut("execution(* com.jchotel.controller..*.*(..)) && !execution(* com.jchotel.controller.AuthController.login(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        OperationLog opLog = new OperationLog();
        opLog.setCreateTime(LocalDateTime.now());

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            Object userId = request.getAttribute("userId");
            Object username = request.getAttribute("username");
            opLog.setUserId(userId != null ? Long.valueOf(userId.toString()) : null);
            opLog.setUsername(username != null ? username.toString() : null);
            opLog.setMethod(request.getMethod() + " " + request.getRequestURI());
            opLog.setIp(getClientIp(request));
        }

        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        opLog.setModule(className.replace("Controller", ""));
        opLog.setOperation(methodName);

        try {
            Object[] args = joinPoint.getArgs();
            String params = buildParams(args, joinPoint);
            opLog.setParams(params);
        } catch (Exception ignored) {}

        Object result;
        try {
            result = joinPoint.proceed();
            opLog.setStatus(1);
            try {
                String resultStr = objectMapper.writeValueAsString(result);
                if (resultStr.length() > MAX_LENGTH) {
                    resultStr = resultStr.substring(0, MAX_LENGTH);
                }
                opLog.setResult(resultStr);
            } catch (Exception ignored) {}
        } catch (Throwable e) {
            opLog.setStatus(0);
            String err = e.getMessage();
            if (err != null && err.length() > MAX_LENGTH) err = err.substring(0, MAX_LENGTH);
            opLog.setErrorMsg(err);
            throw e;
        } finally {
            opLog.setCostTime(System.currentTimeMillis() - startTime);
            try {
                operationLogMapper.insert(opLog);
            } catch (Exception e) {
                log.error("操作日志写入失败", e);
            }
        }
        return result;
    }

    private String buildParams(Object[] args, ProceedingJoinPoint joinPoint) {
        if (args == null || args.length == 0) return "";
        String[] paramNames = null;
        try {
            org.aspectj.lang.reflect.MethodSignature sig = (org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature();
            paramNames = sig.getParameterNames();
        } catch (Exception ignored) {}

        Map<String, Object> paramMap = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) continue;
            if (arg instanceof MultipartFile) {
                paramMap.put(paramNames != null ? paramNames[i] : "param" + i, "[File]");
                continue;
            }
            String key = paramNames != null ? paramNames[i] : "param" + i;
            if (arg != null && (arg instanceof String || arg instanceof Number || arg instanceof Boolean
                    || arg instanceof java.time.LocalDateTime || arg instanceof java.time.LocalDate)) {
                paramMap.put(key, arg);
            } else {
                try {
                    String json = objectMapper.writeValueAsString(arg);
                    if (json.length() > MAX_LENGTH) json = json.substring(0, MAX_LENGTH);
                    paramMap.put(key, objectMapper.readValue(json, Object.class));
                } catch (Exception e) {
                    paramMap.put(key, arg != null ? arg.toString() : null);
                }
            }
        }
        try {
            String json = objectMapper.writeValueAsString(paramMap);
            return json.length() > MAX_LENGTH ? json.substring(0, MAX_LENGTH) : json;
        } catch (Exception e) {
            return "";
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
