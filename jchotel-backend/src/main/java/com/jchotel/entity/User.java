package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体类
 * 对应数据库表 sys_user
 * 用于管理酒店系统的员工用户信息，包括登录账号、密码、角色、状态及账户安全相关信息
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("sys_user") // MyBatis-Plus注解，映射数据库表sys_user
public class User {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 登录用户名，对应数据库列username，员工登录系统的账号 */
    private String username;

    /** 登录密码，对应数据库列password，加密存储的密码 */
    private String password;

    /** 真实姓名，对应数据库列real_name，员工的真实姓名 */
    private String realName;

    /** 用户角色，对应数据库列role，如"admin-管理员"、"front_desk-前台"、"housekeeping-保洁"、"maintenance-维修"等 */
    private String role;

    /** 账号状态，对应数据库列status，取值：0-禁用，1-启用 */
    private Integer status;

    /** 登录失败次数，对应数据库列login_fail_count，连续登录失败的次数，用于账户锁定 */
    private Integer loginFailCount;

    /** 账户锁定截止时间，对应数据库列locked_until，超过此时间后账户自动解锁 */
    private LocalDateTime lockedUntil;

    /** 最后登录时间，对应数据库列last_login_time，记录最近一次成功登录的时间 */
    private LocalDateTime lastLoginTime;

    /** 创建时间，对应数据库列create_time，用户账号创建时间 */
    private LocalDateTime createTime;
}
