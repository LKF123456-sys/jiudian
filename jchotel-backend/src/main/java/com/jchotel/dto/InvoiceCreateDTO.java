package com.jchotel.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 开具发票DTO
 * 用于接收客户申请开具发票时提交的参数
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
public class InvoiceCreateDTO {
    /** 关联订单ID，必填 */
    @NotNull(message = "订单ID不能为空") // JSR380校验注解，值不能为null
    private Long orderId;

    /** 发票抬头，必填，公司名称或个人姓名 */
    @NotBlank(message = "发票抬头不能为空") // JSR380校验注解，字符串不能为null且不能为空串
    private String title;

    /** 纳税人识别号，选填，企业开票时需要提供 */
    private String taxNo;

    /** 开票金额，必填，必须大于0 */
    @NotNull(message = "开票金额不能为空") // JSR380校验注解，值不能为null
    @DecimalMin(value = "0.01", message = "开票金额必须大于0") // JSR380校验注解，BigDecimal最小值为0.01
    private BigDecimal amount;

    /** 发票类型，选填，如"增值税普通发票"、"增值税专用发票"等 */
    private String type;

    /** 发票内容，选填，如"住宿费"、"餐饮费"等 */
    private String content;

    /** 备注，选填，开票补充说明 */
    private String remark;
}
