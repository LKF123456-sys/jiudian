package com.jchotel.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ChargeItem {
    private Long id;
    private String name;
    private String category;
    private BigDecimal price;
    private String unit;
    private Integer status;
    private Integer sort;
    private LocalDateTime createTime;
}
