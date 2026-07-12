package com.jchotel.exception; // 定义包名，exception包存放自定义异常类

/**
 * 业务异常类
 * 自定义运行时异常，用于封装业务逻辑错误
 * 包含错误码和错误信息，由GlobalExceptionHandler统一处理返回给前端
 * 继承RuntimeException，属于非受检异常，无需在方法签名声明抛出
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
public class BusinessException extends RuntimeException { // 继承RuntimeException

    private final Integer code; // 错误码，用于标识具体的业务错误类型

    /**
     * 构造方法（默认错误码400）
     * 使用400作为默认错误码，表示请求参数错误或业务逻辑错误
     *
     * @param message 错误提示信息
     */
    public BusinessException(String message) {
        super(message); // 调用父类构造方法设置错误消息
        this.code = 400; // 默认错误码为400（Bad Request）
    } // 结束单参数构造方法

    /**
     * 构造方法（自定义错误码）
     *
     * @param code 自定义错误码
     * @param message 错误提示信息
     */
    public BusinessException(Integer code, String message) {
        super(message); // 调用父类构造方法设置错误消息
        this.code = code; // 设置自定义错误码
    } // 结束双参数构造方法

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public Integer getCode() {
        return code; // 返回错误码
    } // 结束getCode方法
} // 结束BusinessException类
