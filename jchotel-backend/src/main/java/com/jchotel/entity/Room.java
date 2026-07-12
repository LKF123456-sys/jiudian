package com.jchotel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_room")
public class Room {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roomNo;
    private Long typeId;
    @TableField(exist = false)
    private String typeName;
    private Integer floor;
    private BigDecimal price;
    private String status;
    private String remark;
    private LocalDateTime createTime;
}
