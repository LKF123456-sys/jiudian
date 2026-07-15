// 订单消费品控制器包
package com.jchotel.controller;

// ========== 自定义注解和实体类导入 ==========
import com.jchotel.annotation.RequireRole; // 自定义角色权限校验注解
import com.jchotel.entity.OrderItem; // 订单消费品实体类

// ========== 服务层导入 ==========
import com.jchotel.service.OrderItemService; // 订单消费品服务
import com.jchotel.utils.Result; // 统一响应结果封装类

// ========== Swagger/OpenAPI 文档注解导入 ==========
import io.swagger.v3.oas.annotations.Operation; // API操作描述注解
import io.swagger.v3.oas.annotations.tags.Tag; // API分组标签注解
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // 安全要求注解

// ========== Spring框架相关导入 ==========
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.web.bind.annotation.*; // Spring MVC注解（包含GetMapping/PostMapping/DeleteMapping/RequestBody/PathVariable等）

// ========== Java EE/Servlet 相关导入 ==========
import jakarta.servlet.http.HttpServletRequest; // HTTP请求对象

// ========== Java 标准库导入 ==========
import java.math.BigDecimal; // 高精度小数类（用于金额）
import java.util.List; // 列表集合类

/**
 * 订单消费品控制器
 * 负责处理订单关联的消费品管理，包括查询列表、添加消费品、删除消费品、计算总金额
 * URL前缀: /api/orders/{orderId}/items（嵌套在订单资源下）
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/orders/{orderId}/items") // 设置该控制器的基础请求路径（嵌套路由）
@Tag(name = "订单消费品") // Swagger文档分组标签
@SecurityRequirement(name = "Bearer Authentication") // 需要Bearer Token认证
public class OrderItemController {

    @Autowired // 自动注入订单消费品服务
    private OrderItemService orderItemService; // 订单消费品业务服务

    /**
     * 查询订单消费品列表接口
     * 功能：查询指定订单下的所有消费品明细
     * HTTP方法: GET /api/orders/{orderId}/items
     * @param orderId 订单ID（PathVariable，路径参数）
     * @return Result包含订单消费品列表的List集合
     */
    @Operation(summary = "查询订单消费品列表") // Swagger接口摘要描述
    @GetMapping // 映射GET请求到基础路径
    public Result<List<OrderItem>> list(@PathVariable Long orderId) { // 从路径中获取订单ID
        return orderItemService.listByOrder(orderId); // 调用服务层查询订单的消费品列表
    }

    /**
     * 添加订单消费品接口
     * 功能：为指定订单添加消费品消费记录
     * HTTP方法: POST /api/orders/{orderId}/items
     * @param orderId 订单ID（PathVariable，路径参数）
     * @param item 消费品对象（RequestBody，请求体JSON）
     * @param request HTTP请求对象，用于获取当前登录用户ID（操作人）
     * @return Result操作结果
     * 权限要求：需要admin、manager或receptionist角色
     */
    @Operation(summary = "添加订单消费品") // Swagger接口摘要描述
    @PostMapping // 映射POST请求到基础路径
    @RequireRole({"admin", "manager", "receptionist"}) // 接口权限要求：管理员、经理、前台可操作
    public Result add(@PathVariable Long orderId, @RequestBody OrderItem item, HttpServletRequest request) { // 获取路径订单ID、请求体消费品对象、请求对象
        item.setOrderId(orderId); // 设置消费品所属的订单ID，确保关联正确
        Long userId = (Long) request.getAttribute("userId"); // 从请求属性中获取当前登录用户ID（操作人）
        return orderItemService.addItem(orderId, item, userId); // 调用服务层添加订单消费品
    }

    /**
     * 删除订单消费品接口
     * 功能：删除订单中的指定消费品记录
     * HTTP方法: DELETE /api/orders/{orderId}/items/{itemId}
     * @param orderId 订单ID（PathVariable，路径参数）
     * @param itemId 消费品记录ID（PathVariable，路径参数）
     * @return Result操作结果
     * 权限要求：需要admin、manager或receptionist角色
     */
    @Operation(summary = "删除订单消费品") // Swagger接口摘要描述
    @DeleteMapping("/{itemId}") // 映射DELETE请求到/{itemId}路径
    @RequireRole({"admin", "manager", "receptionist"}) // 接口权限要求：管理员、经理、前台可操作
    public Result remove(@PathVariable Long orderId, @PathVariable Long itemId) { // 从路径中获取订单ID和消费品记录ID
        return orderItemService.removeItem(itemId); // 调用服务层删除订单消费品
    }

    /**
     * 查询消费品总金额接口
     * 功能：计算指定订单所有消费品的总消费金额
     * HTTP方法: GET /api/orders/{orderId}/items/total
     * @param orderId 订单ID（PathVariable，路径参数）
     * @return Result包含消费品总金额的BigDecimal
     */
    @Operation(summary = "查询消费品总金额") // Swagger接口摘要描述
    @GetMapping("/total") // 映射GET请求到/total路径
    public Result<BigDecimal> total(@PathVariable Long orderId) { // 从路径中获取订单ID
        return orderItemService.getTotalExtra(orderId); // 调用服务层计算订单消费品总金额
    }
}