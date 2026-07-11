package com.jchotel.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Room {
    private Long id;
    private String roomNo;
    private Long typeId;
    private String typeName;
    private Integer floor;
    private BigDecimal price;
    private String status;
    private String remark;
    private LocalDateTime createTime;
}
