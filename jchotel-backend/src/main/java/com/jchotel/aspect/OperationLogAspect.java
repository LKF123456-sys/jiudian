package com.jchotel.aspect; // 定义包名，aspect包存放AOP切面类

// 项目自定义类
import com.jchotel.entity.OperationLog; // 操作日志实体类
import com.jchotel.mapper.OperationLogMapper; // 操作日志Mapper接口
// Jackson JSON处理
import com.fasterxml.jackson.databind.ObjectMapper; // JSON对象映射器
// AspectJ AOP相关
import org.aspectj.lang.ProceedingJoinPoint; // 环绕通知连接点
import org.aspectj.lang.annotation.Around; // 环绕通知注解
import org.aspectj.lang.annotation.Aspect; // 切面注解
import org.aspectj.lang.annotation.Pointcut; // 切入点注解
// 日志相关
import org.slf4j.Logger; // 日志接口
import org.slf4j.LoggerFactory; // 日志工厂
// Spring注解
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.stereotype.Component; // Spring组件注解
// Spring Web上下文
import org.springframework.web.context.request.RequestContextHolder; // 请求上下文持有者
import org.springframework.web.context.request.ServletRequestAttributes; // Servlet请求属性
import org.springframework.web.multipart.MultipartFile; // 上传文件对象

// Jakarta Servlet API
import jakarta.servlet.http.HttpServletRequest; // HTTP请求对象
import jakarta.servlet.http.HttpServletResponse; // HTTP响应对象
// Java时间和工具类
import java.time.LocalDateTime; // 本地日期时间
import java.util.LinkedHashMap; // 有序HashMap（保持插入顺序）
import java.util.Map; // Map接口

/**
 * 操作日志切面
 * 使用AOP环绕通知记录Controller层的所有操作日志
 * 包括用户信息、请求方法、IP地址、参数、返回结果、执行时间、错误信息等
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
@Aspect // 标记这是一个AOP切面类
@Component // 标记为Spring组件，交由Spring容器管理
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class); // 获取日志记录器
    private static final int MAX_LENGTH = 4000; // 参数/结果/错误信息的最大存储长度，避免数据库字段溢出

    @Autowired // Spring自动注入操作日志Mapper
    private OperationLogMapper operationLogMapper; // 操作日志数据库访问接口

    private final ObjectMapper objectMapper = new ObjectMapper(); // JSON对象映射器实例，用于对象序列化/反序列化

    /**
     * 定义切入点
     * 匹配com.jchotel.controller包及其子包下的所有类的所有方法
     * 排除AuthController的login方法（登录接口不记录日志，避免密码泄露）
     */
    @Pointcut("execution(* com.jchotel.controller..*.*(..)) && !execution(* com.jchotel.controller.AuthController.login(..))") // 定义切入点表达式
    public void controllerPointcut() {} // 切入点签名方法，空实现即可

    /**
     * 环绕通知
     * 在目标方法执行前后进行拦截，记录完整的操作日志
     *
     * @param joinPoint 连接点对象，可获取目标方法、参数等信息
     * @return 目标方法的返回结果
     * @throws Throwable 目标方法可能抛出的异常
     */
    @Around("controllerPointcut()") // 指定环绕通知应用到controllerPointcut切入点
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis(); // 记录方法开始执行时间（毫秒）
        OperationLog opLog = new OperationLog(); // 创建操作日志对象
        opLog.setCreateTime(LocalDateTime.now()); // 设置日志创建时间为当前时间

        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes(); // 获取当前请求的Servlet属性
        if (attrs != null) { // 判断请求属性是否存在
            HttpServletRequest request = attrs.getRequest(); // 获取HTTP请求对象
            Object userId = request.getAttribute("userId"); // 从request属性中获取用户ID（由LoginInterceptor设置）
            Object username = request.getAttribute("username"); // 从request属性中获取用户名（由LoginInterceptor设置）
            opLog.setUserId(userId != null ? Long.valueOf(userId.toString()) : null); // 设置操作用户ID，如果不存在则为null
            opLog.setUsername(username != null ? username.toString() : null); // 设置操作用户名，如果不存在则为null
            opLog.setMethod(request.getMethod() + " " + request.getRequestURI()); // 设置请求方法和URI（如GET /api/rooms）
            opLog.setIp(getClientIp(request)); // 设置客户端IP地址
        } // 结束请求属性处理

        String className = joinPoint.getTarget().getClass().getSimpleName(); // 获取目标类的简单类名（如RoomController）
        String methodName = joinPoint.getSignature().getName(); // 获取目标方法名
        opLog.setModule(className.replace("Controller", "")); // 设置所属模块（去掉Controller后缀，如Room）
        opLog.setOperation(methodName); // 设置操作方法名

        try { // 尝试记录请求参数
            Object[] args = joinPoint.getArgs(); // 获取方法参数数组
            String params = buildParams(args, joinPoint); // 构建参数字符串
            opLog.setParams(params); // 设置请求参数
        } catch (Exception ignored) {} // 参数构建失败时忽略，不影响主业务流程

        Object result; // 定义返回结果变量
        try { // 执行业务方法
            result = joinPoint.proceed(); // 执行目标方法（即Controller方法）
            opLog.setStatus(1); // 设置操作状态为成功（1=成功）
            try { // 尝试序列化返回结果
                String resultStr = objectMapper.writeValueAsString(result); // 将返回结果序列化为JSON字符串
                if (resultStr.length() > MAX_LENGTH) { // 如果结果超过最大长度
                    resultStr = resultStr.substring(0, MAX_LENGTH); // 截断到最大长度
                } // 结束长度判断
                opLog.setResult(resultStr); // 设置返回结果
            } catch (Exception ignored) {} // 结果序列化失败时忽略
        } catch (Throwable e) { // 捕获业务方法抛出的异常
            opLog.setStatus(0); // 设置操作状态为失败（0=失败）
            String err = e.getMessage(); // 获取错误消息
            if (err != null && err.length() > MAX_LENGTH) err = err.substring(0, MAX_LENGTH); // 错误消息超过最大长度则截断
            opLog.setErrorMsg(err); // 设置错误信息
            throw e; // 继续抛出异常，不影响全局异常处理
        } finally { // 无论成功失败都执行
            opLog.setCostTime(System.currentTimeMillis() - startTime); // 计算方法执行耗时（毫秒）
            try { // 尝试写入数据库
                operationLogMapper.insert(opLog); // 插入操作日志到数据库
            } catch (Exception e) { // 日志写入失败
                log.error("操作日志写入失败", e); // 记录错误日志，但不影响主流程
            } // 结束日志写入异常处理
        } // 结束finally块
        return result; // 返回目标方法的执行结果
    } // 结束around方法

    /**
     * 构建请求参数字符串
     * 将方法参数序列化为JSON格式，跳过Servlet相关对象和文件对象
     *
     * @param args 方法参数数组
     * @param joinPoint 连接点对象，用于获取参数名
     * @return 序列化后的参数字符串（JSON格式）
     */
    private String buildParams(Object[] args, ProceedingJoinPoint joinPoint) {
        if (args == null || args.length == 0) return ""; // 无参数直接返回空字符串
        String[] paramNames = null; // 定义参数名数组
        try { // 尝试获取参数名
            org.aspectj.lang.reflect.MethodSignature sig = (org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature(); // 获取方法签名
            paramNames = sig.getParameterNames(); // 获取参数名列表（需要编译时保留参数名）
        } catch (Exception ignored) {} // 获取参数名失败时忽略

        Map<String, Object> paramMap = new LinkedHashMap<>(); // 使用有序Map存储参数，保持参数顺序
        for (int i = 0; i < args.length; i++) { // 遍历所有参数
            Object arg = args[i]; // 获取当前参数
            if (arg instanceof HttpServletRequest || arg instanceof HttpServletResponse) continue; // 跳过ServletRequest和ServletResponse对象
            if (arg instanceof MultipartFile) { // 如果是上传文件
                paramMap.put(paramNames != null ? paramNames[i] : "param" + i, "[File]"); // 文件参数只记录[File]标记，不序列化文件内容
                continue; // 继续下一个参数
            } // 结束文件参数判断
            String key = paramNames != null ? paramNames[i] : "param" + i; // 确定参数名：有参数名用参数名，否则用param+索引
            if (arg != null && (arg instanceof String || arg instanceof Number || arg instanceof Boolean
                    || arg instanceof java.time.LocalDateTime || arg instanceof java.time.LocalDate)) { // 判断是否是简单类型
                paramMap.put(key, arg); // 简单类型直接放入Map
            } else { // 复杂类型（如DTO对象）
                try { // 尝试序列化复杂对象
                    String json = objectMapper.writeValueAsString(arg); // 序列化为JSON
                    if (json.length() > MAX_LENGTH) json = json.substring(0, MAX_LENGTH); // 超过长度则截断
                    paramMap.put(key, objectMapper.readValue(json, Object.class)); // 反序列化为Object放入Map
                } catch (Exception e) { // 序列化失败
                    paramMap.put(key, arg != null ? arg.toString() : null); // 使用toString()作为兜底
                } // 结束复杂对象序列化异常处理
            } // 结束参数类型判断
        } // 结束参数遍历循环
        try { // 尝试将参数Map序列化为JSON
            String json = objectMapper.writeValueAsString(paramMap); // 序列化为JSON字符串
            return json.length() > MAX_LENGTH ? json.substring(0, MAX_LENGTH) : json; // 超过长度则截断
        } catch (Exception e) { // 序列化失败
            return ""; // 返回空字符串
        } // 结束整体序列化异常处理
    } // 结束buildParams方法

    /**
     * 获取客户端真实IP地址
     * 处理反向代理（如Nginx）转发后的真实IP获取
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For"); // 先从X-Forwarded-For获取（代理服务器设置）
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) { // 如果X-Forwarded-For无效
            ip = request.getHeader("X-Real-IP"); // 尝试从X-Real-IP获取（Nginx常用）
        } // 结束X-Forwarded-For判断
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) { // 如果X-Real-IP也无效
            ip = request.getRemoteAddr(); // 使用远程地址（直连时的IP）
        } // 结束X-Real-IP判断
        if (ip != null && ip.contains(",")) { // X-Forwarded-For可能包含多个IP（多级代理），格式为IP1, IP2, IP3
            ip = ip.split(",")[0].trim(); // 取第一个IP（真实客户端IP）
        } // 结束多级代理IP处理
        return ip; // 返回客户端IP
    } // 结束getClientIp方法
} // 结束OperationLogAspect类
