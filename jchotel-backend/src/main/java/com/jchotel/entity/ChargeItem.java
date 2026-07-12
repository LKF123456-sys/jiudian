package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 收费项目实体类
 * 对应数据库表 t_charge_item
 * 用于管理酒店的各类收费项目，如商品、服务等，可在入住期间附加消费
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("t_charge_item") // MyBatis-Plus注解，映射数据库表t_charge_item
public class ChargeItem {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 收费项目名称，对应数据库列name，如"矿泉水"、"早餐"等 */
    private String name;

    /** 收费项目分类，对应数据库列category，如"食品"、"饮品"、"服务"等 */
    private String category;

    /** 单价，对应数据库列price，单位为元，使用BigDecimal保证精度 */
    private BigDecimal price;

    /** 计量单位，对应数据库列unit，如"瓶"、"份"、"次"等 */
    private String unit;

    /** 状态，对应数据库列status，取值：0-禁用，1-启用 */
    private Integer status;

    /** 排序号，对应数据库列sort，数值越小排序越靠前 */
    private Integer sort;

    /** 创建时间，对应数据库列create_time，记录项目创建时间 */
    private LocalDateTime createTime;
}
