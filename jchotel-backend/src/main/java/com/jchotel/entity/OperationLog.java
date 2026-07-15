package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 对应数据库表 t_operation_log
 * 用于记录系统用户的所有操作行为日志，包括操作模块、操作类型、请求参数、执行结果、IP地址、耗时等信息，便于审计和问题追溯
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("t_operation_log") // MyBatis-Plus注解，映射数据库表t_operation_log
public class OperationLog {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 操作用户ID，对应数据库列user_id，关联sys_user表的id */
    private Long userId;

    /** 操作用户名，对应数据库列username，冗余存储便于查询展示 */
    private String username;

    /** 操作模块，对应数据库列module，如"用户管理"、"客房管理"、"订单管理"等 */
    private String module;

    /** 操作类型，对应数据库列operation，如"新增"、"修改"、"删除"、"查询"、"登录"、"退房"等 */
    private String operation;

    /** 请求方法，对应数据库列method，记录执行的类名和方法名 */
    private String method;

    /** 请求参数，对应数据库列params，JSON格式存储请求参数，用于问题排查 */
    private String params;

    /** 返回结果，对应数据库列result，JSON格式存储返回结果摘要 */
    private String result;

    /** 操作IP地址，对应数据库列ip，记录操作发起的IP地址 */
    private String ip;

    /** 操作状态，对应数据库列status，取值：0-失败，1-成功 */
    private Integer status;

    /** 错误信息，对应数据库列error_msg，操作失败时记录异常堆栈或错误信息 */
    private String errorMsg;

    /** 执行耗时，对应数据库列cost_time，单位为毫秒，记录接口响应时间 */
    private Long costTime;

    /** 创建时间/操作时间，对应数据库列create_time，记录操作发生的时间 */
    private LocalDateTime createTime;

    /**
     * 获取操作状态文本描述
     * @return 状态文本，"success"表示成功，"fail"表示失败
     */
    public String getStatusText() {
        return status != null && status == 1 ? "success" : "fail";
    }

    /**
     * 获取操作耗时（别名，兼容前端字段名duration）
     * @return 执行耗时，单位毫秒
     */
    public Long getDuration() {
        return costTime;
    }
}
