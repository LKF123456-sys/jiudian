// 维修管理控制器包
package com.jchotel.controller;

// ========== DTO、实体类和Mapper导入 ==========
import com.jchotel.dto.PageQuery; // 分页查询参数DTO
import com.jchotel.dto.PageResult; // 分页查询结果DTO
import com.jchotel.entity.MaintenanceOrder; // 维修单实体类
import com.jchotel.entity.User; // 用户实体类
import com.jchotel.mapper.UserMapper; // 用户数据访问Mapper

// ========== 服务层导入 ==========
import com.jchotel.service.MaintenanceOrderService; // 维修单服务
import com.jchotel.utils.Result; // 统一响应结果封装类

// ========== Swagger/OpenAPI 文档注解导入 ==========
import io.swagger.v3.oas.annotations.Operation; // API操作描述注解
import io.swagger.v3.oas.annotations.tags.Tag; // API分组标签注解

// ========== Spring框架相关导入 ==========
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.web.bind.annotation.*; // Spring MVC注解（包含GetMapping/PostMapping/RequestBody/PathVariable等）

// ========== Java EE/Servlet 相关导入 ==========
import jakarta.servlet.http.HttpServletRequest; // HTTP请求对象

// ========== Java 标准库导入 ==========
import java.math.BigDecimal; // 高精度小数类（用于金额）
import java.util.Map; // 键值对映射类

/**
 * 维修管理控制器
 * 负责处理维修单的全生命周期管理，包括创建、分配、开始维修、完成维修、验收、取消等流程
 * URL前缀: /api/maintenance
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/maintenance") // 设置该控制器的基础请求路径
@Tag(name = "维修管理") // Swagger文档分组标签
public class MaintenanceOrderController {

    @Autowired // 自动注入维修单服务
    private MaintenanceOrderService maintenanceOrderService; // 维修单业务服务

    @Autowired // 自动注入用户Mapper
    private UserMapper userMapper; // 用户数据访问接口

    /**
     * 分页查询维修单列表接口
     * 功能：按分页参数查询维修单列表
     * HTTP方法: GET /api/maintenance
     * @param query 分页查询参数（PageQuery对象，包含页码、页大小等）
     * @return Result包含分页维修单数据的PageResult
     */
    @Operation(summary = "分页查询维修单列表") // Swagger接口摘要描述
    @GetMapping // 映射GET请求到基础路径
    public Result<PageResult<MaintenanceOrder>> list(PageQuery query) {
        return maintenanceOrderService.list(query); // 调用服务层分页查询维修单列表
    }

    /**
     * 查询维修单详情接口
     * 功能：根据ID查询单个维修单的详细信息
     * HTTP方法: GET /api/maintenance/{id}
     * @param id 维修单ID（PathVariable，路径参数）
     * @return Result包含维修单详情的MaintenanceOrder对象
     */
    @Operation(summary = "查询维修单详情") // Swagger接口摘要描述
    @GetMapping("/{id}") // 映射GET请求到/{id}路径
    public Result<MaintenanceOrder> detail(@PathVariable Long id) { // 从路径中获取维修单ID
        return maintenanceOrderService.detail(id); // 调用服务层查询维修单详情
    }

    /**
     * 创建维修单接口
     * 功能：创建新的维修工单，自动记录报修人信息
     * HTTP方法: POST /api/maintenance
     * @param order 维修单对象（RequestBody，请求体JSON）
     * @param request HTTP请求对象，用于获取当前登录用户ID
     * @return Result操作结果
     */
    @Operation(summary = "创建维修单") // Swagger接口摘要描述
    @PostMapping // 映射POST请求到基础路径
    public Result create(@RequestBody MaintenanceOrder order, HttpServletRequest request) { // 从请求体获取维修单对象，从request获取用户信息
        Long userId = (Long) request.getAttribute("userId"); // 从请求属性中获取当前登录用户ID（由拦截器设置）
        User user = userMapper.selectById(userId); // 根据用户ID查询用户信息
        String reporterName = user != null ? user.getRealName() : null; // 获取报修人真实姓名，如果用户不存在则为null
        return maintenanceOrderService.create(order, userId, reporterName); // 调用服务层创建维修单
    }

    /**
     * 分配维修单接口
     * 功能：将维修单分配给指定维修人员
     * HTTP方法: POST /api/maintenance/{id}/assign
     * @param id 维修单ID（PathVariable，路径参数）
     * @param body 请求体，包含assigneeId（维修人员ID）和assigneeName（维修人员姓名）
     * @return Result操作结果
     */
    @Operation(summary = "分配维修单") // Swagger接口摘要描述
    @PostMapping("/{id}/assign") // 映射POST请求到/{id}/assign路径
    public Result assign(@PathVariable Long id, @RequestBody Map<String, Object> body) { // 获取路径ID和请求体参数
        Long assigneeId = Long.valueOf(body.get("assigneeId").toString()); // 从请求体中获取并转换维修人员ID
        String assigneeName = (String) body.get("assigneeName"); // 从请求体中获取维修人员姓名
        return maintenanceOrderService.assign(id, assigneeId, assigneeName); // 调用服务层分配维修单
    }

    /**
     * 开始维修接口
     * 功能：维修人员开始处理维修单，将状态更新为维修中
     * HTTP方法: POST /api/maintenance/{id}/start
     * @param id 维修单ID（PathVariable，路径参数）
     * @return Result操作结果
     */
    @Operation(summary = "开始维修") // Swagger接口摘要描述
    @PostMapping("/{id}/start") // 映射POST请求到/{id}/start路径
    public Result startProcessing(@PathVariable Long id) { // 从路径中获取维修单ID
        return maintenanceOrderService.startProcessing(id); // 调用服务层开始维修
    }

    /**
     * 完成维修接口
     * 功能：维修人员完成维修，填写解决方案和维修费用
     * HTTP方法: POST /api/maintenance/{id}/finish
     * @param id 维修单ID（PathVariable，路径参数）
     * @param body 请求体，包含solution（解决方案）和cost（维修费用）
     * @return Result操作结果
     */
    @Operation(summary = "完成维修") // Swagger接口摘要描述
    @PostMapping("/{id}/finish") // 映射POST请求到/{id}/finish路径
    public Result finish(@PathVariable Long id, @RequestBody Map<String, Object> body) { // 获取路径ID和请求体参数
        String solution = (String) body.get("solution"); // 从请求体中获取解决方案描述
        BigDecimal cost = body.get("cost") != null ? new BigDecimal(body.get("cost").toString()) : null; // 从请求体中获取维修费用，为空则为null
        return maintenanceOrderService.finish(id, solution, cost); // 调用服务层完成维修
    }

    /**
     * 验收维修接口
     * 功能：管理人员验收维修结果，将状态更新为已完成
     * HTTP方法: POST /api/maintenance/{id}/verify
     * @param id 维修单ID（PathVariable，路径参数）
     * @return Result操作结果
     */
    @Operation(summary = "验收维修") // Swagger接口摘要描述
    @PostMapping("/{id}/verify") // 映射POST请求到/{id}/verify路径
    public Result verify(@PathVariable Long id) { // 从路径中获取维修单ID
        return maintenanceOrderService.verify(id); // 调用服务层验收维修
    }

    /**
     * 取消维修单接口
     * 功能：取消待处理的维修单
     * HTTP方法: POST /api/maintenance/{id}/cancel
     * @param id 维修单ID（PathVariable，路径参数）
     * @return Result操作结果
     */
    @Operation(summary = "取消维修单") // Swagger接口摘要描述
    @PostMapping("/{id}/cancel") // 映射POST请求到/{id}/cancel路径
    public Result cancel(@PathVariable Long id) { // 从路径中获取维修单ID
        return maintenanceOrderService.cancel(id); // 调用服务层取消维修单
    }
}
