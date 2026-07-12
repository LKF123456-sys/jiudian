package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_room_type")
public class RoomType {
    @TableId(type = IdType.AUTO)
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
