package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 维修工单实体类
 * 对应数据库表 t_maintenance_order
 * 用于管理客房设施设备的维修工单，包括报修信息、派工、处理过程及费用等
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("t_maintenance_order") // MyBatis-Plus注解，映射数据库表t_maintenance_order
public class MaintenanceOrder {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 工单编号，对应数据库列order_no，系统自动生成的唯一维修工单号 */
    private String orderNo;

    /** 房间ID，对应数据库列room_id，需要维修的房间ID */
    private Long roomId;

    /** 房间号，对应数据库列room_no，冗余存储便于展示 */
    private String roomNo;

    /** 维修标题，对应数据库列title，简要描述维修问题 */
    private String title;

    /** 问题描述，对应数据库列description，详细描述故障现象 */
    private String description;

    /** 维修分类，对应数据库列category，如"水电"、"空调"、"家具"、"电器"等 */
    private String category;

    /** 优先级，对应数据库列priority，如"low-低"、"medium-中"、"high-高"、"urgent-紧急" */
    private String priority;

    /** 报修人ID，对应数据库列reporter_id，提交维修申请的员工ID */
    private Long reporterId;

    /** 报修人姓名，对应数据库列reporter_name，冗余存储便于展示 */
    private String reporterName;

    /** 维修人员ID，对应数据库列assignee_id，指派的维修人员ID */
    private Long assigneeId;

    /** 维修人员姓名，对应数据库列assignee_name，冗余存储便于展示 */
    private String assigneeName;

    /** 工单状态，对应数据库列status，取值：pending-待派单，assigned-已派单，processing-处理中，completed-已完成，verified-已验收 */
    private String status;

    /** 解决方案，对应数据库列solution，记录维修处理过程和方法 */
    private String solution;

    /** 维修费用，对应数据库列cost，维修产生的材料或人工费用，单位为元 */
    private BigDecimal cost;

    /** 完成时间，对应数据库列finish_time，维修人员完成维修的时间 */
    private LocalDateTime finishTime;

    /** 验收时间，对应数据库列verify_time，管理人员验收确认的时间 */
    private LocalDateTime verifyTime;

    /** 创建时间，对应数据库列create_time，工单创建时间 */
    private LocalDateTime createTime;

    /** 更新时间，对应数据库列update_time，工单最后更新时间 */
    private LocalDateTime updateTime;
}
