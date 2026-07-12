package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客房实体类
 * 对应数据库表 t_room
 * 用于管理酒店具体的客房信息，包括房间号、所属房型、楼层、价格、状态等
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("t_room") // MyBatis-Plus注解，映射数据库表t_room
public class Room {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 房间号，对应数据库列room_no，客房的唯一编号，如"301"、"502"等 */
    private String roomNo;

    /** 房型ID，对应数据库列type_id，关联t_room_type表的id */
    private Long typeId;

    /** 房型名称，非数据库字段，@TableField(exist=false)表示不映射数据库列，关联查询时填充 */
    @TableField(exist = false) // MyBatis-Plus注解，标识该字段不是数据库表中的列
    private String typeName;

    /** 楼层，对应数据库列floor，房间所在楼层数 */
    private Integer floor;

    /** 房间单价，对应数据库列price，单位为元，该房间的平日售价，可覆盖房型默认价格 */
    private BigDecimal price;

    /** 房间状态，对应数据库列status，取值：available-空闲可售，occupied-已入住，cleaning-清洁中，maintenance-维修中，reserved-已预订，dirty-脏房待打扫 */
    private String status;

    /** 备注，对应数据库列remark，记录房间特殊情况，如朝向、景观、特殊问题等 */
    private String remark;

    /** 创建时间，对应数据库列create_time，房间记录创建时间 */
    private LocalDateTime createTime;
}
