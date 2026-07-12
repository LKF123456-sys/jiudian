package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 操作日志实体类
 * 对应数据库表 t_operation_log
 * 用于记录系统中用户的所有操作行为，包括操作模块、操作内容、请求参数、执行结果、耗时等，用于审计和问题排查
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("t_operation_log") // MyBatis-Plus注解，映射数据库表t_operation_log
public class OperationLog {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 操作用户ID，对应数据库列user_id，执行操作的员工ID */
    private Long userId;

    /** 操作用户名，对应数据库列username，冗余存储便于查询 */
    private String username;

    /** 操作模块，对应数据库列module，如"订单管理"、"客房管理"、"客户管理"、"系统设置"等 */
    private String module;

    /** 操作描述，对应数据库列operation，如"办理入住"、"退房结账"、"新增客房"等具体操作说明 */
    private String operation;

    /** 请求方法，对应数据库列method，如"GET"、"POST"、"PUT"、"DELETE"等HTTP方法 */
    private String method;

    /** 请求参数，对应数据库列params，记录接口请求的参数内容，JSON格式 */
    private String params;

    /** 返回结果，对应数据库列result，记录接口返回的结果内容，JSON格式 */
    private String result;

    /** 操作IP地址，对应数据库列ip，记录操作人员的IP地址 */
    private String ip;

    /** 操作状态，对应数据库列status，取值：0-失败，1-成功 */
    private Integer status;

    /** 错误信息，对应数据库列error_msg，操作失败时记录异常信息 */
    private String errorMsg;

    /** 执行耗时，对应数据库列cost_time，单位为毫秒，记录接口执行时间 */
    private Long costTime;

    /** 创建时间，对应数据库列create_time，操作执行时间 */
    private LocalDateTime createTime;
}
