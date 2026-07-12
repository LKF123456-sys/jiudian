package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 房型实体类
 * 对应数据库表 t_room_type
 * 用于管理酒店客房类型信息，包括房型名称、床型、容纳人数、价格、设施配置等
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("t_room_type") // MyBatis-Plus注解，映射数据库表t_room_type
public class RoomType {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 房型名称，对应数据库列name，如"标准间"、"大床房"、"豪华套房"等 */
    private String name;

    /** 床型描述，对应数据库列bed_type，如"单人床"、"双人床"、"大床"、"双床"等 */
    private String bedType;

    /** 容纳人数，对应数据库列capacity，该房型最多可入住人数 */
    private Integer capacity;

    /** 周末价格，对应数据库列weekend_price，单位为元，周五、周六的房价 */
    private BigDecimal weekendPrice;

    /** 会员价格，对应数据库列member_price，单位为元，VIP会员享受的优惠房价 */
    private BigDecimal memberPrice;

    /** 房型描述，对应数据库列description，详细介绍房型特点、面积、景观等 */
    private String description;

    /** 设施配置，对应数据库列facilities，多个设施用逗号分隔，如"wifi,空调,电视,迷你吧" */
    private String facilities;

    /** 房型图片URL，对应数据库列image_url，展示房型的主图片地址 */
    private String imageUrl;

    /** 创建时间，对应数据库列create_time，房型记录创建时间 */
    private LocalDateTime createTime;
}
