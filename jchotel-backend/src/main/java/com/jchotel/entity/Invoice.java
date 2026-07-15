package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 发票实体类
 * 对应数据库表 t_invoice
 * 用于管理客户退房后的发票开具记录，包括发票抬头、税号、金额、类型及状态等信息
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
@TableName("t_invoice") // MyBatis-Plus注解，映射数据库表t_invoice
public class Invoice {
    /** 主键ID，自增，对应数据库列id */
    @TableId(type = IdType.AUTO) // MyBatis-Plus注解，标识主键字段，主键生成策略为数据库自增
    private Long id;

    /** 发票编号，对应数据库列invoice_no，系统自动生成的唯一发票流水号 */
    private String invoiceNo;

    /** 关联订单ID，对应数据库列order_id，关联t_order表的id */
    private Long orderId;

    /** 订单编号，对应数据库列order_no，冗余存储便于查询 */
    private String orderNo;

    /** 客户ID，对应数据库列customer_id，关联t_customer表的id */
    private Long customerId;

    /** 客户姓名，对应数据库列customer_name，冗余存储便于展示 */
    private String customerName;

    /** 发票抬头，对应数据库列title，公司名称或个人姓名 */
    private String title;

    /** 纳税人识别号，对应数据库列tax_no，企业开票时必填的税号 */
    private String taxNo;

    /** 开票金额，对应数据库列amount，单位为元，使用BigDecimal保证精度 */
    private BigDecimal amount;

    /** 发票类型，对应数据库列type，如"增值税普通发票"、"增值税专用发票"等 */
    private String type;

    /** 发票状态，对应数据库列status，取值：issued-已开具，cancelled-已作废，red-已红冲 */
    private String status;

    /** 发票内容，对应数据库列content，如"住宿费"、"餐饮费"等开票项目 */
    private String content;

    /** 备注，对应数据库列remark，记录开票相关的补充说明 */
    private String remark;

    /** 操作员ID，对应数据库列operator_id，执行开票操作的员工ID */
    private Long operatorId;

    /** 操作员姓名，对应数据库列operator_name，冗余存储便于展示 */
    private String operatorName;

    /** 创建时间/开票时间，对应数据库列create_time，记录开票时间 */
    private LocalDateTime createTime;

    /** 作废时间，对应数据库列cancel_time，记录发票作废/红冲的时间 */
    private LocalDateTime cancelTime;

    /**
     * 获取开票时间（兼容前端字段名issueTime）
     * @return 开票时间
     */
    public LocalDateTime getIssueTime() {
        return createTime;
    }
}
