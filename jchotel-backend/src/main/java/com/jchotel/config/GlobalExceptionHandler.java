package com.jchotel.config;

import com.jchotel.exception.BusinessException;
import com.jchotel.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import jakarta.validation.ConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * <p>
 * 使用@RestControllerAdvice实现全局异常统一捕获和处理，
 * 将系统中各类异常转换为标准化的JSON格式错误响应，
 * 避免异常堆栈直接暴露给前端，同时提供友好的错误提示。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 日志记录器实例
     */
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理自定义业务异常
     * <p>
     * 捕获业务逻辑中主动抛出的BusinessException，返回业务定义的错误码和错误信息。
     * </p>
     *
     * @param e 业务异常对象，包含自定义错误码和错误消息
     * @return Result&lt;String&gt; 统一错误响应结果
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<String> handleBusiness(BusinessException e) {
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理@Valid @RequestBody参数校验异常
     * <p>
     * 捕获请求体参数校验失败异常（如JSON参数校验不通过），
     * 收集所有字段的校验错误信息并拼接返回。
     * </p>
     *
     * @param e 方法参数无效异常对象
     * @return Result&lt;String&gt; 统一错误响应结果，包含所有校验错误信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return Result.error(400, msg);
    }

    /**
     * 处理表单数据绑定异常
     * <p>
     * 捕获表单参数绑定失败异常（如@ModelAttribute或表单提交参数校验失败），
     * 收集所有字段错误信息拼接返回。
     * </p>
     *
     * @param e 数据绑定异常对象
     * @return Result&lt;String&gt; 统一错误响应结果
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleBind(BindException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("；"));
        return Result.error(400, msg);
    }

    /**
     * 处理约束违反异常
     * <p>
     * 捕获@Validated注解在类级别或方法参数上触发的约束违反异常。
     * </p>
     *
     * @param e 约束违反异常对象
     * @return Result&lt;String&gt; 统一错误响应结果
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleConstraint(ConstraintViolationException e) {
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理HTTP请求体解析异常
     * <p>
     * 捕获请求体JSON格式错误、无法解析等异常，返回友好提示。
     * </p>
     *
     * @param e HTTP消息不可读异常对象
     * @return Result&lt;String&gt; 统一错误响应结果
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体解析失败: {}", e.getMessage());
        return Result.error(400, "请求参数格式错误，请检查输入");
    }

    /**
     * 处理缺少必填请求参数异常
     * <p>
     * 捕获请求中缺少必填参数的异常，返回缺失的参数名称。
     * </p>
     *
     * @param e 缺少请求参数异常对象
     * @return Result&lt;String&gt; 统一错误响应结果，提示缺失的参数名
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.error(400, "缺少必要参数：" + e.getParameterName());
    }

    /**
     * 处理参数类型不匹配异常
     * <p>
     * 捕获请求参数类型转换失败异常（如需要数字但传入字符串）。
     * </p>
     *
     * @param e 参数类型不匹配异常对象
     * @return Result&lt;String&gt; 统一错误响应结果，提示类型错误的参数名
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.error(400, "参数类型错误：" + e.getName());
    }

    /**
     * 处理HTTP请求方法不支持异常
     * <p>
     * 捕获使用了接口不允许的HTTP方法（如POST接口用GET方式访问）。
     * </p>
     *
     * @param e 请求方法不支持异常对象
     * @return Result&lt;String&gt; 统一错误响应结果
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<String> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return Result.error(405, "请求方法不支持：" + e.getMethod());
    }

    /**
     * 处理资源未找到异常（404错误）
     * <p>
     * 捕获请求的URL路径不存在的异常。
     * </p>
     *
     * @param e 资源未找到异常对象
     * @return Result&lt;String&gt; 统一错误响应结果
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<String> handleNotFound(NoResourceFoundException e) {
        return Result.error(404, "请求资源不存在");
    }

    /**
     * 处理非法参数异常
     * <p>
     * 捕获代码中手动抛出的IllegalArgumentException。
     * </p>
     *
     * @param e 非法参数异常对象
     * @return Result&lt;String&gt; 统一错误响应结果
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("参数异常: {}", e.getMessage());
        return Result.error(400, e.getMessage());
    }

    /**
     * 处理运行时异常（通用兜底）
     * <p>
     * 捕获所有未被前面具体处理器处理的RuntimeException，
     * 记录完整错误日志并返回500错误。
     * </p>
     *
     * @param e 运行时异常对象
     * @return Result&lt;String&gt; 统一错误响应结果
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleRuntimeException(RuntimeException e) {
        log.error("运行时异常", e);
        return Result.error(500, e.getMessage() != null ? e.getMessage() : "服务器内部错误");
    }

    /**
     * 处理所有其他异常（最终兜底处理器）
     * <p>
     * 捕获Exception类型异常，处理所有未被前面处理器捕获的异常，
     * 防止异常栈信息泄露给前端，返回通用友好提示。
     * </p>
     *
     * @param e 异常对象
     * @return Result&lt;String&gt; 统一错误响应结果
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<String> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error(500, "系统异常，请稍后重试");
    }
}
