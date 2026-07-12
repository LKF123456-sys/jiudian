package com.jchotel.utils; // 定义包名，utils包存放工具类

// Lombok注解
import lombok.Data; // 自动生成getter、setter、toString、equals、hashCode方法

/**
 * 统一返回结果类
 * 封装所有API接口的返回数据格式，统一响应结构
 * 包含状态码、消息、数据三个字段
 * 提供静态工厂方法方便创建成功/失败结果
 *
 * @param <T> 数据类型泛型
 * @author 锦程酒店
 * @since 1.0.0
 */
@Data // Lombok注解，自动生成getter、setter、toString、equals、hashCode等方法
public class Result<T> { // 泛型类，T为返回数据的类型
    private Integer code; // 状态码：0表示成功，其他表示失败
    private String message; // 返回消息（成功提示或错误信息）
    private T data; // 返回数据（泛型，可承载任意类型数据）

    /**
     * 返回成功结果（无数据）
     * 用于不需要返回数据的操作（如删除、更新等）
     *
     * @param <T> 数据类型泛型
     * @return 成功结果对象
     */
    public static <T> Result<T> success() {
        return success(null); // 调用带数据的success方法，数据为null
    } // 结束无参success方法

    /**
     * 返回成功结果（带数据）
     * 用于查询等需要返回数据的操作
     *
     * @param data 返回的数据
     * @param <T> 数据类型泛型
     * @return 成功结果对象
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>(); // 创建结果对象
        result.setCode(0); // 设置状态码为0（成功）
        result.setMessage("操作成功"); // 设置默认成功消息
        result.setData(data); // 设置返回数据
        return result; // 返回结果对象
    } // 结束带数据success方法

    /**
     * 返回成功结果（自定义消息+数据）
     * 用于需要自定义成功提示消息的场景
     *
     * @param message 自定义成功消息
     * @param data 返回的数据
     * @param <T> 数据类型泛型
     * @return 成功结果对象
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>(); // 创建结果对象
        result.setCode(0); // 设置状态码为0（成功）
        result.setMessage(message); // 设置自定义消息
        result.setData(data); // 设置返回数据
        return result; // 返回结果对象
    } // 结束自定义消息success方法

    /**
     * 返回错误结果（默认错误码1）
     * 使用默认错误码1表示通用错误
     *
     * @param message 错误消息
     * @param <T> 数据类型泛型
     * @return 错误结果对象
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>(); // 创建结果对象
        result.setCode(1); // 设置错误码为1（通用错误）
        result.setMessage(message); // 设置错误消息
        return result; // 返回结果对象
    } // 结束单参数error方法

    /**
     * 返回错误结果（自定义错误码+消息）
     * 用于需要指定具体错误码的场景（如401未登录、403权限不足等）
     *
     * @param code 自定义错误码
     * @param message 错误消息
     * @param <T> 数据类型泛型
     * @return 错误结果对象
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>(); // 创建结果对象
        result.setCode(code); // 设置自定义错误码
        result.setMessage(message); // 设置错误消息
        return result; // 返回结果对象
    } // 结束双参数error方法
} // 结束Result类
