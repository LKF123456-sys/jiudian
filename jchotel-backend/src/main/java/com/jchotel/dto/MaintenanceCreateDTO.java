package com.jchotel.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class MaintenanceCreateDTO {
    @NotNull(message = "房间ID不能为空")
    private Long roomId;

    @NotBlank(message = "维修标题不能为空")
    private String title;

    private String description;

    private String category;

    private String priority;
}
