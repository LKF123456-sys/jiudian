package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 入住订单实体类
 * 对应数据库表 t_order
 * 是酒店系统的核心业务实体，记录客户入住的完整信息，包括客户、房间、时间、费用、状态等
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("t_order") // MyBatis-Plus注解，映射数据库表t_order
public class Order {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 订单编号，对应数据库列order_no，系统自动生成的唯一订单号 */
    private String orderNo;

    /** 客户ID，对应数据库列customer_id，关联t_customer表的id */
    private Long customerId;

    /** 客户姓名，非数据库字段，@TableField(exist=false)表示不映射数据库列，关联查询时填充 */
    @TableField(exist = false) // MyBatis-Plus注解，标识该字段不是数据库表中的列
    private String customerName;

    /** 客户手机号，非数据库字段，@TableField(exist=false)表示不映射数据库列，关联查询时填充 */
    @TableField(exist = false) // MyBatis-Plus注解，标识该字段不是数据库表中的列
    private String customerPhone;

    /** 房间ID，对应数据库列room_id，关联t_room表的id */
    private Long roomId;

    /** 房间号，非数据库字段，@TableField(exist=false)表示不映射数据库列，关联查询时填充 */
    @TableField(exist = false) // MyBatis-Plus注解，标识该字段不是数据库表中的列
    private String roomNo;

    /** 房型名称，非数据库字段，@TableField(exist=false)表示不映射数据库列，关联查询时填充 */
    @TableField(exist = false) // MyBatis-Plus注解，标识该字段不是数据库表中的列
    private String roomTypeName;

    /** 接待员工ID，对应数据库列user_id，办理入住的前台员工ID */
    private Long userId;

    /** 接待员工姓名，非数据库字段，@TableField(exist=false)表示不映射数据库列，关联查询时填充 */
    @TableField(exist = false) // MyBatis-Plus注解，标识该字段不是数据库表中的列
    private String userName;

    /** 实际入住时间，对应数据库列check_in_time，客户办理入住的时间 */
    private LocalDateTime checkInTime;

    /** 预计退房时间，对应数据库列expected_check_out_time，入住时约定的退房时间 */
    private LocalDateTime expectedCheckOutTime;

    /** 实际退房时间，对应数据库列actual_check_out_time，客户办理退房结账的时间 */
    private LocalDateTime actualCheckOutTime;

    /** 押金金额，对应数据库列deposit，入住时收取的押金，单位为元 */
    private BigDecimal deposit;

    /** 押金退还状态，对应数据库列deposit_refunded，取值：0-未退还，1-已退还 */
    private Integer depositRefunded;

    /** 房费金额，对应数据库列room_amount，住宿期间的房费总计，单位为元 */
    private BigDecimal roomAmount;

    /** 附加消费金额，对应数据库列extra_amount，迷你吧、服务等附加消费总计，单位为元 */
    private BigDecimal extraAmount;

    /** 订单总金额，对应数据库列total_amount，房费+附加消费的总计金额，单位为元 */
    private BigDecimal totalAmount;

    /** 是否换房，对应数据库列room_changed，取值：0-未换房，1-已换房 */
    private Integer roomChanged;

    /** 原房间ID，对应数据库列original_room_id，换房前的原始房间ID */
    private Long originalRoomId;

    /** 父订单ID，对应数据库列parent_order_id，续住时关联原订单的ID */
    private Long parentOrderId;

    /** 订单渠道，对应数据库列channel，如"前台"、"线上预订"、"电话预订"等 */
    private String channel;

    /** 订单状态，对应数据库列status，取值：reserved-预订，checked_in-已入住，checked_out-已退房，cancelled-已取消 */
    private String status;

    /** 备注，对应数据库列remark，记录订单相关的特殊说明 */
    private String remark;

    /** 入住天数，非数据库字段，@TableField(exist=false)表示不映射数据库列，计算得出 */
    @TableField(exist = false) // MyBatis-Plus注解，标识该字段不是数据库表中的列
    private Integer days;

    /** 创建时间，对应数据库列create_time，订单创建时间 */
    private LocalDateTime createTime;
}
