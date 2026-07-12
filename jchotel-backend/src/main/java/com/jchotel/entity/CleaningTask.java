package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 清洁任务实体类
 * 对应数据库表 t_cleaning_task
 * 用于管理客房清洁任务，客户退房后自动生成或手动创建，分配给保洁人员进行打扫
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("t_cleaning_task") // MyBatis-Plus注解，映射数据库表t_cleaning_task
public class CleaningTask {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 房间ID，对应数据库列room_id，需要清洁的房间ID */
    private Long roomId;

    /** 房间号，对应数据库列room_no，冗余存储便于展示 */
    private String roomNo;

    /** 关联订单ID，对应数据库列order_id，退房产生清洁任务时关联的订单ID */
    private Long orderId;

    /** 保洁人员ID，对应数据库列assignee_id，分配的保洁人员ID */
    private Long assigneeId;

    /** 保洁人员姓名，对应数据库列assignee_name，冗余存储便于展示 */
    private String assigneeName;

    /** 任务状态，对应数据库列status，取值：pending-待分配，assigned-已分配，processing-清洁中，completed-已完成，inspected-已检查合格 */
    private String status;

    /** 优先级，对应数据库列priority，如"low-低"、"medium-中"、"high-高"、"urgent-紧急" */
    private String priority;

    /** 备注，对应数据库列remark，记录清洁要求或特殊说明 */
    private String remark;

    /** 完成时间，对应数据库列finish_time，保洁人员完成清洁的时间 */
    private LocalDateTime finishTime;

    /** 检查时间，对应数据库列inspect_time，管理人员检查验收的时间 */
    private LocalDateTime inspectTime;

    /** 创建时间，对应数据库列create_time，任务创建时间 */
    private LocalDateTime createTime;

    /** 更新时间，对应数据库列update_time，任务最后更新时间 */
    private LocalDateTime updateTime;
}
