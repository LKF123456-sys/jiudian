package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 换房记录实体类
 * 对应数据库表 t_room_change_log
 * 用于记录客户入住期间的换房操作历史，包括原房间、新房间、差价及原因等信息
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("t_room_change_log") // MyBatis-Plus注解，映射数据库表t_room_change_log
public class RoomChangeLog {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 关联订单ID，对应数据库列order_id，关联t_order表的id */
    private Long orderId;

    /** 订单编号，对应数据库列order_no，冗余存储便于查询 */
    private String orderNo;

    /** 原房间ID，对应数据库列from_room_id，换房前的房间ID */
    private Long fromRoomId;

    /** 原房间号，对应数据库列from_room_no，冗余存储便于展示 */
    private String fromRoomNo;

    /** 目标房间ID，对应数据库列to_room_id，换房后的房间ID */
    private Long toRoomId;

    /** 目标房间号，对应数据库列to_room_no，冗余存储便于展示 */
    private String toRoomNo;

    /** 换房原因，对应数据库列reason，记录客户要求换房的具体原因 */
    private String reason;

    /** 房费差价，对应数据库列price_diff，新房型与原房型的价格差额，正数表示补差价，负数表示退差价 */
    private BigDecimal priceDiff;

    /** 操作员ID，对应数据库列operator_id，执行换房操作的员工ID */
    private Long operatorId;

    /** 操作员姓名，对应数据库列operator_name，冗余存储便于展示 */
    private String operatorName;

    /** 创建时间，对应数据库列create_time，记录换房操作的时间 */
    private LocalDateTime createTime;
}
