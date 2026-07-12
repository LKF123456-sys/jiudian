package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单消费明细实体类
 * 对应数据库表 t_order_item
 * 用于记录入住订单中的附加消费明细，如迷你吧商品、餐饮、服务等消费项目
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("t_order_item") // MyBatis-Plus注解，映射数据库表t_order_item
public class OrderItem {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 关联订单ID，对应数据库列order_id，关联t_order表的id */
    private Long orderId;

    /** 订单编号，对应数据库列order_no，冗余存储便于查询 */
    private String orderNo;

    /** 消费项目名称，对应数据库列item_name，如"矿泉水"、"早餐"、"洗衣服务"等 */
    private String itemName;

    /** 消费项目分类，对应数据库列category，如"食品"、"饮品"、"服务"等 */
    private String category;

    /** 单价，对应数据库列price，单位为元，消费时的单价 */
    private BigDecimal price;

    /** 数量，对应数据库列quantity，消费数量，必须为正整数 */
    private Integer quantity;

    /** 小计金额，对应数据库列amount，单位为元，计算公式：price * quantity */
    private BigDecimal amount;

    /** 备注，对应数据库列remark，记录消费相关的补充说明 */
    private String remark;

    /** 操作员ID，对应数据库列operator_id，记录该笔消费的操作人员ID */
    private Long operatorId;

    /** 创建时间，对应数据库列create_time，记录消费录入时间 */
    private LocalDateTime createTime;
}
