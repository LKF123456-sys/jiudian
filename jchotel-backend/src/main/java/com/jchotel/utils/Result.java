package com.jchotel.utils;

import lombok.Data;

/**
 * 统一API响应结果封装类
 * <p>
 * 封装所有后端API接口的返回数据格式，统一响应结构，
 * 包含状态码、提示消息、业务数据三个字段，
 * 提供静态工厂方法方便快速创建成功/失败响应。
 * </p>
 *
 * @param <T> 响应数据的泛型类型
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
@Data
public class Result<T> {

    /**
     * 响应状态码
     * 0表示成功，其他值表示失败（如400参数错误、401未登录、403权限不足、500服务器错误等）
     */
    private Integer code;

    /**
     * 响应消息
     * 成功时返回操作成功提示，失败时返回具体错误信息
     */
    private String message;

    /**
     * 响应业务数据
     * 泛型类型，可承载任意类型的业务返回数据
     */
    private T data;

    /**
     * 返回成功结果（无数据）
     * <p>
     * 用于不需要返回业务数据的操作，如删除、更新等写操作。
     * </p>
     *
     * @param <T> 数据类型泛型
     * @return Result&lt;T&gt; 成功结果对象
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 返回成功结果（带数据）
     * <p>
     * 用于查询等需要返回业务数据的操作。
     * </p>
     *
     * @param data 返回的业务数据
     * @param <T> 数据类型泛型
     * @return Result&lt;T&gt; 成功结果对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 返回成功结果（自定义消息+数据）
     * <p>
     * 用于需要自定义成功提示消息的场景。
     * </p>
     *
     * @param message 自定义成功提示消息
     * @param data 返回的业务数据
     * @param <T> 数据类型泛型
     * @return Result&lt;T&gt; 成功结果对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 返回错误结果（默认错误码1）
     * <p>
     * 使用默认错误码1表示通用业务错误。
     * </p>
     *
     * @param message 错误提示消息
     * @param <T> 数据类型泛型
     * @return Result&lt;T&gt; 错误结果对象
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMessage(message);
        return result;
    }

    /**
     * 返回错误结果（自定义错误码+消息）
     * <p>
     * 用于需要指定具体错误码的场景（如401未登录、403权限不足等）。
     * </p>
     *
     * @param code 自定义错误码
     * @param message 错误提示消息
     * @param <T> 数据类型泛型
     * @return Result&lt;T&gt; 错误结果对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
