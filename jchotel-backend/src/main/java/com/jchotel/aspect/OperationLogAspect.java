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

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);

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
            if (args.length > 0) {
                try {
                    String params = objectMapper.writeValueAsString(args[0]);
                    if (params.length() > 2000) {
                        params = params.substring(0, 2000);
                    }
                    opLog.setParams(params);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {}

        Object result;
        try {
            result = joinPoint.proceed();
            opLog.setStatus(1);
        } catch (Throwable e) {
            opLog.setStatus(0);
            String err = e.getMessage();
            if (err != null && err.length() > 500) err = err.substring(0, 500);
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
