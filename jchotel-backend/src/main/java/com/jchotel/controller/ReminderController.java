// 提醒事项控制器包
package com.jchotel.controller;

// ========== 实体类导入 ==========
import com.jchotel.entity.Customer; // 客户实体类
import com.jchotel.entity.Order; // 订单实体类

// ========== 服务层导入 ==========
import com.jchotel.service.ReminderService; // 提醒服务
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
import java.util.List; // 列表集合类
import java.util.Map; // 键值对映射类

/**
 * 提醒事项控制器
 * 负责处理首页各类提醒事项查询，包括生日客户、今日入住/离店、逾期退房、低押金等
 * URL前缀: /api/reminders
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/reminders") // 设置该控制器的基础请求路径
@Tag(name = "提醒事项") // Swagger文档分组标签
@SecurityRequirement(name = "Bearer Authentication") // 需要Bearer Token认证
public class ReminderController {

    @Autowired // 自动注入提醒服务
    private ReminderService reminderService; // 提醒事项业务服务

    /**
     * 获取首页提醒事项接口
     * 功能：查询首页展示的各类提醒事项汇总数据
     * HTTP方法: GET /api/reminders/dashboard
     * @return Result包含各类提醒事项汇总的Map集合
     */
    @Operation(summary = "获取首页提醒事项") // Swagger接口摘要描述
    @GetMapping("/dashboard") // 映射GET请求到/dashboard路径
    public Result<Map<String, Object>> getDashboardReminders() {
        return reminderService.getDashboardReminders(); // 调用服务层获取首页提醒数据
    }

    /**
     * 获取生日客户提醒接口
     * 功能：查询今日生日的客户列表
     * HTTP方法: GET /api/reminders/birthdays
     * @return Result包含今日生日客户的List集合
     */
    @Operation(summary = "获取生日客户提醒") // Swagger接口摘要描述
    @GetMapping("/birthdays") // 映射GET请求到/birthdays路径
    public Result<List<Customer>> getBirthdayCustomers() {
        return reminderService.getBirthdayCustomers(); // 调用服务层获取今日生日客户列表
    }

    /**
     * 获取今日入住提醒接口
     * 功能：查询今日预计入住的订单列表
     * HTTP方法: GET /api/reminders/today-arrivals
     * @return Result包含今日入住订单的List集合
     */
    @Operation(summary = "获取今日入住提醒") // Swagger接口摘要描述
    @GetMapping("/today-arrivals") // 映射GET请求到/today-arrivals路径
    public Result<List<Order>> getTodayArrivals() {
        return reminderService.getTodayArrivals(); // 调用服务层获取今日入住订单列表
    }

    /**
     * 获取今日离店提醒接口
     * 功能：查询今日预计离店的订单列表
     * HTTP方法: GET /api/reminders/today-departures
     * @return Result包含今日离店订单的List集合
     */
    @Operation(summary = "获取今日离店提醒") // Swagger接口摘要描述
    @GetMapping("/today-departures") // 映射GET请求到/today-departures路径
    public Result<List<Order>> getTodayDepartures() {
        return reminderService.getTodayDepartures(); // 调用服务层获取今日离店订单列表
    }

    /**
     * 获取逾期退房提醒接口
     * 功能：查询已超过预计离店时间但未退房的订单列表
     * HTTP方法: GET /api/reminders/overdue-checkouts
     * @return Result包含逾期退房订单的List集合
     */
    @Operation(summary = "获取逾期退房提醒") // Swagger接口摘要描述
    @GetMapping("/overdue-checkouts") // 映射GET请求到/overdue-checkouts路径
    public Result<List<Order>> getOverdueCheckouts() {
        return reminderService.getOverdueCheckouts(); // 调用服务层获取逾期退房订单列表
    }

    /**
     * 获取低押金提醒接口
     * 功能：查询押金余额不足的订单列表
     * HTTP方法: GET /api/reminders/low-deposit
     * @return Result包含低押金订单的List集合
     */
    @Operation(summary = "获取低押金提醒") // Swagger接口摘要描述
    @GetMapping("/low-deposit") // 映射GET请求到/low-deposit路径
    public Result<List<Order>> getLowDepositOrders() {
        return reminderService.getLowDepositOrders(); // 调用服务层获取低押金订单列表
    }
}