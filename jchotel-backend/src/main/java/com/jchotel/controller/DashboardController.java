package com.jchotel.controller;

import com.jchotel.service.DashboardService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "首页看板")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @Operation(summary = "获取首页看板数据")
    @GetMapping
    public Result<Map<String, Object>> dashboard() {
        return dashboardService.getDashboardData();
    }
}
