package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付记录实体类
 * 对应数据库表 t_payment
 * 用于记录订单相关的支付流水，包括押金支付、房费结算、退款等资金往来记录
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("t_payment") // MyBatis-Plus注解，映射数据库表t_payment
public class Payment {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 关联订单ID，对应数据库列order_id，关联t_order表的id */
    private Long orderId;

    /** 订单编号，对应数据库列order_no，冗余存储便于查询 */
    private String orderNo;

    /** 支付方式，对应数据库列payment_method，如"cash-现金"、"wechat-微信"、"alipay-支付宝"、"card-银行卡"等 */
    private String paymentMethod;

    /** 支付金额，对应数据库列amount，单位为元，正数表示收款，负数表示退款 */
    private BigDecimal amount;

    /** 支付类型，对应数据库列type，如"deposit-押金"、"room_fee-房费"、"extra_fee-杂费"、"refund-退款"等 */
    private String type;

    /** 备注，对应数据库列remark，记录支付相关的补充说明 */
    private String remark;

    /** 操作员ID，对应数据库列operator_id，执行收款/退款操作的员工ID */
    private Long operatorId;

    /** 创建时间，对应数据库列create_time，记录支付操作时间 */
    private LocalDateTime createTime;
}
