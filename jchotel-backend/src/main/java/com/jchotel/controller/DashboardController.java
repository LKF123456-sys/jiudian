// 首页看板控制器包
package com.jchotel.controller;

// ========== 服务层导入 ==========
import com.jchotel.service.DashboardService; // 首页看板服务
import com.jchotel.utils.Result; // 统一响应结果封装类

// ========== Swagger/OpenAPI 文档注解导入 ==========
import io.swagger.v3.oas.annotations.Operation; // API操作描述注解
import io.swagger.v3.oas.annotations.tags.Tag; // API分组标签注解
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // 安全要求注解

// ========== Spring框架相关导入 ==========
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.web.bind.annotation.GetMapping; // GET请求映射注解
import org.springframework.web.bind.annotation.RequestMapping; // 请求路径映射注解
import org.springframework.web.bind.annotation.RestController; // REST控制器注解

// ========== Java 标准库导入 ==========
import java.util.Map; // 键值对映射类

/**
 * 首页看板控制器
 * 负责处理系统首页的统计看板数据，包括今日入住/离店、房态统计、营收统计等汇总数据
 * URL前缀: /api/dashboard
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/dashboard") // 设置该控制器的基础请求路径
@Tag(name = "首页看板") // Swagger文档分组标签
@SecurityRequirement(name = "Bearer Authentication") // 需要Bearer Token认证
public class DashboardController {

    @Autowired // 自动注入首页看板服务
    private DashboardService dashboardService; // 首页看板业务服务

    /**
     * 获取首页看板数据接口
     * 功能：获取系统首页展示的所有汇总统计数据
     * HTTP方法: GET /api/dashboard
     * @return Result包含首页看板数据的Map集合（包含今日订单数、入住率、营收等）
     */
    @Operation(summary = "获取首页看板数据") // Swagger接口摘要描述
    @GetMapping // 映射GET请求到基础路径
    public Result<Map<String, Object>> dashboard() {
        return dashboardService.getDashboardData(); // 调用服务层获取首页看板汇总数据
    }
}