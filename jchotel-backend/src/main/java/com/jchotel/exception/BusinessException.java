package com.jchotel.exception;

/**
 * 业务异常类
 * <p>
 * 自定义运行时异常，用于封装业务逻辑处理过程中出现的错误。
 * 包含错误码和错误信息，由GlobalExceptionHandler统一捕获并返回标准化的错误响应给前端。
 * 继承自RuntimeException，属于非受检异常，无需在方法签名上声明throws。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException {

    /**
     * 错误码，用于标识具体的业务错误类型
     */
    private final Integer code;

    /**
     * 构造方法（默认错误码400）
     * <p>
     * 使用400作为默认错误码，表示客户端请求参数错误或通用业务逻辑错误。
     * </p>
     *
     * @param message 错误提示信息，将返回给前端展示
     */
    public BusinessException(String message) {
        super(message);
        this.code = 400;
    }

    /**
     * 构造方法（自定义错误码）
     *
     * @param code 自定义错误码，用于区分不同类型的业务错误
     * @param message 错误提示信息，将返回给前端展示
     */
    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 获取错误码
     *
     * @return Integer 错误码
     */
    public Integer getCode() {
        return code;
    }
}
