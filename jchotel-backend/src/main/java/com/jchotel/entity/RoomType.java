package com.jchotel.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class RoomType {
    private Long id;
    private String name;
    private String bedType;
    private Integer capacity;
    private BigDecimal weekendPrice;
    private BigDecimal memberPrice;
    private String description;
    private String facilities;
    private String imageUrl;
    private LocalDateTime createTime;
}
