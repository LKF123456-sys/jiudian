package com.jchotel.dto;

import lombok.Data;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class InvoiceCreateDTO {
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    @NotBlank(message = "发票抬头不能为空")
    private String title;

    private String taxNo;

    @NotNull(message = "开票金额不能为空")
    @DecimalMin(value = "0.01", message = "开票金额必须大于0")
    private BigDecimal amount;

    private String type;

    private String content;

    private String remark;
}
