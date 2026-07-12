package com.jchotel.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建维修工单DTO
 * 用于接收前台或员工提交维修申请时的参数
 * 使用Lombok @Data注解自动生成getter、setter、toString、equals、hashCode方法
 */
@Data // Lombok注解，自动生成getter/setter/toString/equals/hashCode方法
public class MaintenanceCreateDTO {
    /** 需要维修的房间ID，必填 */
    @NotNull(message = "房间ID不能为空") // JSR380校验注解，值不能为null
    private Long roomId;

    /** 维修标题，必填，简要描述问题 */
    @NotBlank(message = "维修标题不能为空") // JSR380校验注解，字符串不能为null且不能为空串
    private String title;

    /** 问题详细描述，选填，详细说明故障情况 */
    private String description;

    /** 维修分类，选填，如"水电"、"空调"、"家具"、"电器"等 */
    private String category;

    /** 优先级，选填，如"low"、"medium"、"high"、"urgent" */
    private String priority;
}
