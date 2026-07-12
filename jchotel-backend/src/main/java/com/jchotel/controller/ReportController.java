// 报表中心控制器包
package com.jchotel.controller;

// ========== 自定义注解和服务层导入 ==========
import com.jchotel.annotation.RequireRole; // 自定义角色权限校验注解
import com.jchotel.service.ExportService; // 导出服务
import com.jchotel.service.ReportService; // 报表服务
import com.jchotel.utils.Result; // 统一响应结果封装类

// ========== Swagger/OpenAPI 文档注解导入 ==========
import io.swagger.v3.oas.annotations.Operation; // API操作描述注解
import io.swagger.v3.oas.annotations.tags.Tag; // API分组标签注解

// ========== Java EE/Servlet 相关导入 ==========
import jakarta.servlet.http.HttpServletResponse; // HTTP响应对象

// ========== Spring框架相关导入 ==========
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.format.annotation.DateTimeFormat; // 日期时间格式化注解
import org.springframework.web.bind.annotation.GetMapping; // GET请求映射注解
import org.springframework.web.bind.annotation.RequestMapping; // 请求路径映射注解
import org.springframework.web.bind.annotation.RequestParam; // 请求参数注解
import org.springframework.web.bind.annotation.RestController; // REST控制器注解

// ========== Java 标准库导入 ==========
import java.io.IOException; // IO异常类
import java.time.LocalDate; // 本地日期类
import java.util.List; // 列表集合类
import java.util.Map; // 键值对映射类

/**
 * 报表中心控制器
 * 负责处理报表查询和数据导出相关业务，包括交班报表、入住率报表、日报表、支付统计、房型收入等
 * URL前缀: /api/reports
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/reports") // 设置该控制器的基础请求路径
@Tag(name = "报表中心") // Swagger文档分组标签
@RequireRole({"admin", "manager"}) // 接口权限要求：仅管理员和经理角色可访问
public class ReportController {

    @Autowired // 自动注入报表服务
    private ReportService reportService; // 报表业务服务

    @Autowired // 自动注入导出服务
    private ExportService exportService; // 数据导出业务服务

    /**
     * 获取交班报表接口
     * 功能：查询当前班次的交班统计数据
     * HTTP方法: GET /api/reports/shift
     * @return Result包含交班报表数据的Map集合
     */
    @Operation(summary = "获取交班报表") // Swagger接口摘要描述
    @GetMapping("/shift") // 映射GET请求到/shift路径
    public Result<Map<String, Object>> getShiftReport() {
        return reportService.getShiftReport(); // 调用服务层获取交班报表数据
    }

    /**
     * 获取入住率报表接口
     * 功能：按时间范围查询客房入住率统计数据
     * HTTP方法: GET /api/reports/occupancy
     * @param startTime 开始时间（可选，RequestParam）
     * @param endTime 结束时间（可选，RequestParam）
     * @return Result包含入住率报表数据的Map集合
     */
    @Operation(summary = "获取入住率报表") // Swagger接口摘要描述
    @GetMapping("/occupancy") // 映射GET请求到/occupancy路径
    public Result<Map<String, Object>> getOccupancyReport(
            @RequestParam(required = false) String startTime, // 可选的查询开始时间参数
            @RequestParam(required = false) String endTime) { // 可选的查询结束时间参数
        return reportService.getOccupancyReport(startTime, endTime); // 调用服务层获取入住率报表
    }

    /**
     * 获取日报表接口
     * 功能：查询指定日期的营业日报表数据
     * HTTP方法: GET /api/reports/daily-shift
     * @param date 查询日期（必填，RequestParam，格式yyyy-MM-dd）
     * @return Result包含日报表数据的Map集合
     */
    @Operation(summary = "获取日报表") // Swagger接口摘要描述
    @GetMapping("/daily-shift") // 映射GET请求到/daily-shift路径
    public Result<Map<String, Object>> getDailyShiftReport(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) { // 必填的日期参数，指定日期格式
        return reportService.getDailyShiftReport(date); // 调用服务层获取日报表数据
    }

    /**
     * 获取支付统计报表接口
     * 功能：按时间范围查询各支付方式的统计数据
     * HTTP方法: GET /api/reports/payment-stats
     * @param startTime 开始时间（可选，RequestParam）
     * @param endTime 结束时间（可选，RequestParam）
     * @return Result包含支付方式统计列表的List集合
     */
    @Operation(summary = "获取支付统计报表") // Swagger接口摘要描述
    @GetMapping("/payment-stats") // 映射GET请求到/payment-stats路径
    public Result<List<Map<String, Object>>> getPaymentStats(
            @RequestParam(required = false) String startTime, // 可选的查询开始时间参数
            @RequestParam(required = false) String endTime) { // 可选的查询结束时间参数
        return reportService.getPaymentStats(startTime, endTime); // 调用服务层获取支付统计数据
    }

    /**
     * 获取房型收入报表接口
     * 功能：按时间范围查询各房型的收入统计数据
     * HTTP方法: GET /api/reports/room-type-revenue
     * @param startTime 开始时间（可选，RequestParam）
     * @param endTime 结束时间（可选，RequestParam）
     * @return Result包含房型收入统计列表的List集合
     */
    @Operation(summary = "获取房型收入报表") // Swagger接口摘要描述
    @GetMapping("/room-type-revenue") // 映射GET请求到/room-type-revenue路径
    public Result<List<Map<String, Object>>> getRoomTypeRevenue(
            @RequestParam(required = false) String startTime, // 可选的查询开始时间参数
            @RequestParam(required = false) String endTime) { // 可选的查询结束时间参数
        return reportService.getRoomTypeRevenue(startTime, endTime); // 调用服务层获取房型收入报表
    }

    /**
     * 导出订单Excel接口
     * 功能：按条件导出订单数据为Excel文件
     * HTTP方法: GET /api/reports/export/orders/excel
     * @param startTime 开始时间（可选，RequestParam）
     * @param endTime 结束时间（可选，RequestParam）
     * @param status 订单状态（可选，RequestParam）
     * @param response HTTP响应对象，用于输出文件流
     * @throws IOException 文件输出IO异常
     */
    @Operation(summary = "导出订单Excel") // Swagger接口摘要描述
    @GetMapping("/export/orders/excel") // 映射GET请求到/export/orders/excel路径
    public void exportOrdersExcel(
            @RequestParam(required = false) String startTime, // 可选的开始时间筛选参数
            @RequestParam(required = false) String endTime, // 可选的结束时间筛选参数
            @RequestParam(required = false) String status, // 可选的订单状态筛选参数
            HttpServletResponse response) throws IOException { // HTTP响应对象，用于写入导出文件
        exportService.exportOrders(startTime, endTime, status, response); // 调用服务层导出订单Excel
    }

    /**
     * 导出客房Excel接口
     * 功能：导出所有客房数据为Excel文件
     * HTTP方法: GET /api/reports/export/rooms/excel
     * @param response HTTP响应对象，用于输出文件流
     * @throws IOException 文件输出IO异常
     */
    @Operation(summary = "导出客房Excel") // Swagger接口摘要描述
    @GetMapping("/export/rooms/excel") // 映射GET请求到/export/rooms/excel路径
    public void exportRoomsExcel(HttpServletResponse response) throws IOException { // HTTP响应对象
        exportService.exportRooms(response); // 调用服务层导出客房Excel
    }

    /**
     * 导出客户Excel接口
     * 功能：导出所有客户数据为Excel文件
     * HTTP方法: GET /api/reports/export/customers/excel
     * @param response HTTP响应对象，用于输出文件流
     * @throws IOException 文件输出IO异常
     */
    @Operation(summary = "导出客户Excel") // Swagger接口摘要描述
    @GetMapping("/export/customers/excel") // 映射GET请求到/export/customers/excel路径
    public void exportCustomersExcel(HttpServletResponse response) throws IOException { // HTTP响应对象
        exportService.exportCustomers(response); // 调用服务层导出客户Excel
    }

    /**
     * 导出财务Excel接口
     * 功能：按时间范围导出财务数据为Excel文件
     * HTTP方法: GET /api/reports/export/finance/excel
     * @param startTime 开始时间（可选，RequestParam）
     * @param endTime 结束时间（可选，RequestParam）
     * @param response HTTP响应对象，用于输出文件流
     * @throws IOException 文件输出IO异常
     */
    @Operation(summary = "导出财务Excel") // Swagger接口摘要描述
    @GetMapping("/export/finance/excel") // 映射GET请求到/export/finance/excel路径
    public void exportFinanceExcel(
            @RequestParam(required = false) String startTime, // 可选的开始时间筛选参数
            @RequestParam(required = false) String endTime, // 可选的结束时间筛选参数
            HttpServletResponse response) throws IOException { // HTTP响应对象
        exportService.exportFinance(startTime, endTime, response); // 调用服务层导出财务Excel
    }

    /**
     * 导出订单PDF接口
     * 功能：按条件导出订单数据为PDF文件
     * HTTP方法: GET /api/reports/export/orders/pdf
     * @param startTime 开始时间（可选，RequestParam）
     * @param endTime 结束时间（可选，RequestParam）
     * @param status 订单状态（可选，RequestParam）
     * @param response HTTP响应对象，用于输出文件流
     * @throws IOException 文件输出IO异常
     */
    @Operation(summary = "导出订单PDF") // Swagger接口摘要描述
    @GetMapping("/export/orders/pdf") // 映射GET请求到/export/orders/pdf路径
    public void exportOrdersPdf(
            @RequestParam(required = false) String startTime, // 可选的开始时间筛选参数
            @RequestParam(required = false) String endTime, // 可选的结束时间筛选参数
            @RequestParam(required = false) String status, // 可选的订单状态筛选参数
            HttpServletResponse response) throws IOException { // HTTP响应对象
        exportService.exportOrdersPdf(startTime, endTime, status, response); // 调用服务层导出订单PDF
    }

    /**
     * 导出财务PDF接口
     * 功能：按时间范围导出财务数据为PDF文件
     * HTTP方法: GET /api/reports/export/finance/pdf
     * @param startTime 开始时间（可选，RequestParam）
     * @param endTime 结束时间（可选，RequestParam）
     * @param response HTTP响应对象，用于输出文件流
     * @throws IOException 文件输出IO异常
     */
    @Operation(summary = "导出财务PDF") // Swagger接口摘要描述
    @GetMapping("/export/finance/pdf") // 映射GET请求到/export/finance/pdf路径
    public void exportFinancePdf(
            @RequestParam(required = false) String startTime, // 可选的开始时间筛选参数
            @RequestParam(required = false) String endTime, // 可选的结束时间筛选参数
            HttpServletResponse response) throws IOException { // HTTP响应对象
        exportService.exportFinancePdf(startTime, endTime, response); // 调用服务层导出财务PDF
    }
}
