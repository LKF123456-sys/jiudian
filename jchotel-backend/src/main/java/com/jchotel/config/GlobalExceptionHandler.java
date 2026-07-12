package com.jchotel.config; // 定义包名，config包存放配置类

// 项目自定义类
import com.jchotel.exception.BusinessException; // 业务异常类
import com.jchotel.utils.Result; // 统一返回结果类
// 日志相关
import org.slf4j.Logger; // 日志接口
import org.slf4j.LoggerFactory; // 日志工厂
// Spring HTTP状态码
import org.springframework.http.HttpStatus; // HTTP状态码枚举
// Spring参数解析异常
import org.springframework.http.converter.HttpMessageNotReadableException; // HTTP消息不可读异常
// Spring校验异常
import org.springframework.validation.BindException; // 数据绑定异常
import org.springframework.validation.FieldError; // 字段错误信息
// Spring Web异常
import org.springframework.web.HttpRequestMethodNotSupportedException; // HTTP请求方法不支持异常
import org.springframework.web.bind.MethodArgumentNotValidException; // 方法参数无效异常
import org.springframework.web.bind.MissingServletRequestParameterException; // 缺少请求参数异常
import org.springframework.web.bind.annotation.ExceptionHandler; // 异常处理器注解
import org.springframework.web.bind.annotation.ResponseStatus; // 响应状态码注解
import org.springframework.web.bind.annotation.RestControllerAdvice; // RestController增强注解
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException; // 参数类型不匹配异常
import org.springframework.web.servlet.resource.NoResourceFoundException; // 资源未找到异常

// Jakarta校验
import jakarta.validation.ConstraintViolationException; // 约束违反异常
// Java工具类
import java.util.stream.Collectors; // 流收集器

/**
 * 全局异常处理器
 * 统一处理系统中抛出的各类异常，返回标准化的错误响应
 * 使用@RestControllerAdvice实现全局异常捕获
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
@RestControllerAdvice // 全局异常处理注解，相当于@ControllerAdvice + @ResponseBody
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class); // 获取日志记录器实例

    /**
     * 处理业务异常
     * 捕获自定义业务异常，返回业务错误码和错误信息
     *
     * @param e 业务异常对象，包含错误码和错误信息
     * @return 统一错误响应结果
     */
    @ExceptionHandler(BusinessException.class) // 指定处理BusinessException类型异常
    @ResponseStatus(HttpStatus.OK) // 设置HTTP响应状态码为200（业务错误码在body中返回）
    public Result<String> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage()); // 返回包含错误码和错误信息的统一结果
    } // 结束handleBusiness方法

    /**
     * 处理方法参数校验异常（@Valid注解触发）
     * 捕获@Valid @RequestBody参数校验失败异常，拼接所有字段错误信息
     *
     * @param e 参数校验异常对象
     * @return 统一错误响应结果，包含所有校验错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class) // 指定处理参数无效异常
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置HTTP状态码为400
    public Result<String> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream() // 获取字段错误列表并转为流
                .map(FieldError::getDefaultMessage) // 提取每个字段错误的默认提示信息
                .collect(Collectors.joining("；")); // 用分号拼接所有错误信息
        return Result.error(400, msg); // 返回400错误和拼接后的错误信息
    } // 结束handleValidation方法

    /**
     * 处理表单绑定异常
     * 捕获表单参数绑定失败异常（如@ModelAttribute参数）
     *
     * @param e 数据绑定异常对象
     * @return 统一错误响应结果
     */
    @ExceptionHandler(BindException.class) // 指定处理BindException异常
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置HTTP状态码为400
    public Result<String> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream() // 获取字段错误列表并转为流
                .map(FieldError::getDefaultMessage) // 提取每个字段错误的默认提示信息
                .collect(Collectors.joining("；")); // 用分号拼接所有错误信息
        return Result.error(400, msg); // 返回400错误和拼接后的错误信息
    } // 结束handleBind方法

    /**
     * 处理约束违反异常
     * 捕获@Validated注解在类级别触发的约束违反异常
     *
     * @param e 约束违反异常对象
     * @return 统一错误响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class) // 指定处理约束违反异常
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置HTTP状态码为400
    public Result<String> handleConstraint(ConstraintViolationException e) {
        return Result.error(400, e.getMessage()); // 直接返回异常消息
    } // 结束handleConstraint方法

    /**
     * 处理HTTP消息不可读异常
     * 捕获请求体JSON解析失败等异常（如格式错误的JSON）
     *
     * @param e HTTP消息不可读异常对象
     * @return 统一错误响应结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class) // 指定处理HTTP消息不可读异常
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置HTTP状态码为400
    public Result<String> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage()); // 记录警告日志，不打印完整堆栈
        return Result.error(400, "请求参数格式错误，请检查输入"); // 返回友好提示信息
    } // 结束handleHttpMessageNotReadable方法

    /**
     * 处理缺少请求参数异常
     * 捕获必填参数缺失的异常
     *
     * @param e 缺少参数异常对象
     * @return 统一错误响应结果，提示缺失的参数名
     */
    @ExceptionHandler(MissingServletRequestParameterException.class) // 指定处理缺少参数异常
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置HTTP状态码为400
    public Result<String> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.error(400, "缺少必要参数：" + e.getParameterName()); // 返回提示和缺失的参数名
    } // 结束handleMissingParam方法

    /**
     * 处理参数类型不匹配异常
     * 捕获参数类型转换失败异常（如字符串转数字失败）
     *
     * @param e 参数类型不匹配异常对象
     * @return 统一错误响应结果，提示类型错误的参数名
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class) // 指定处理参数类型不匹配异常
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置HTTP状态码为400
    public Result<String> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.error(400, "参数类型错误：" + e.getName()); // 返回提示和错误参数名
    } // 结束handleTypeMismatch方法

    /**
     * 处理HTTP请求方法不支持异常
     * 捕获使用了不允许的HTTP方法（如POST接口用GET访问）
     *
     * @param e 请求方法不支持异常对象
     * @return 统一错误响应结果，提示不支持的方法
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class) // 指定处理请求方法不支持异常
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED) // 设置HTTP状态码为405
    public Result<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.error(405, "请求方法不支持：" + e.getMethod()); // 返回405错误和不支持的方法
    } // 结束handleMethodNotSupported方法

    /**
     * 处理资源未找到异常
     * 捕获404错误，请求的资源不存在
     *
     * @param e 资源未找到异常对象
     * @return 统一错误响应结果
     */
    @ExceptionHandler(NoResourceFoundException.class) // 指定处理资源未找到异常
    @ResponseStatus(HttpStatus.NOT_FOUND) // 设置HTTP状态码为404
    public Result<String> handleNotFound(NoResourceFoundException e) {
        return Result.error(404, "请求资源不存在"); // 返回404错误提示
    } // 结束handleNotFound方法

    /**
     * 处理非法参数异常
     * 捕获手动抛出的IllegalArgumentException
     *
     * @param e 非法参数异常对象
     * @return 统一错误响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class) // 指定处理非法参数异常
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 设置HTTP状态码为400
    public Result<String> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage()); // 记录警告日志
        return Result.error(400, e.getMessage()); // 返回异常消息
    } // 结束handleIllegalArgument方法

    /**
     * 处理运行时异常
     * 捕获所有未被前面处理器处理的RuntimeException，作为兜底处理
     *
     * @param e 运行时异常对象
     * @return 统一错误响应结果
     */
    @ExceptionHandler(RuntimeException.class) // 指定处理所有运行时异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 设置HTTP状态码为500
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e); // 记录错误日志，打印完整堆栈信息
        return Result.error(500, e.getMessage() != null ? e.getMessage() : "服务器内部错误"); // 返回异常消息或默认提示
    } // 结束handleRuntimeException方法

    /**
     * 处理所有其他异常（最终兜底）
     * 捕获Exception类型异常，处理所有未被捕获的异常
     *
     * @param e 异常对象
     * @return 统一错误响应结果
     */
    @ExceptionHandler(Exception.class) // 指定处理所有Exception类型异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 设置HTTP状态码为500
    public Result<String> handleException(Exception e) {
        log.error("系统异常", e); // 记录错误日志，打印完整堆栈信息
        return Result.error(500, "系统异常，请稍后重试"); // 返回友好的通用错误提示
    } // 结束handleException方法
} // 结束GlobalExceptionHandler类
