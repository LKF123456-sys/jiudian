// 发票管理控制器包
package com.jchotel.controller;

// ========== DTO、实体类和Mapper导入 ==========
import com.jchotel.dto.PageQuery; // 分页查询参数DTO
import com.jchotel.dto.PageResult; // 分页查询结果DTO
import com.jchotel.entity.Invoice; // 发票实体类
import com.jchotel.entity.User; // 用户实体类
import com.jchotel.mapper.UserMapper; // 用户数据访问Mapper

// ========== 服务层导入 ==========
import com.jchotel.service.InvoiceService; // 发票服务
import com.jchotel.utils.Result; // 统一响应结果封装类

// ========== Swagger/OpenAPI 文档注解导入 ==========
import io.swagger.v3.oas.annotations.Operation; // API操作描述注解
import io.swagger.v3.oas.annotations.tags.Tag; // API分组标签注解
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // 安全要求注解

// ========== Spring框架相关导入 ==========
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.web.bind.annotation.*; // Spring MVC注解（包含GetMapping/PostMapping/RequestBody/PathVariable等）

// ========== Java EE/Servlet 相关导入 ==========
import jakarta.servlet.http.HttpServletRequest; // HTTP请求对象

// ========== Java 标准库导入 ==========
import java.util.List; // 列表集合类
import java.util.Map; // 键值对映射类

/**
 * 发票管理控制器
 * 负责处理发票相关业务，包括查询、开具、作废、红冲等操作
 * URL前缀: /api/invoices
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/invoices") // 设置该控制器的基础请求路径
@Tag(name = "发票管理") // Swagger文档分组标签
@SecurityRequirement(name = "Bearer Authentication") // 需要Bearer Token认证
public class InvoiceController {

    @Autowired // 自动注入发票服务
    private InvoiceService invoiceService; // 发票业务服务

    @Autowired // 自动注入用户Mapper
    private UserMapper userMapper; // 用户数据访问接口

    /**
     * 分页查询发票列表接口
     * 功能：按分页参数查询发票列表
     * HTTP方法: GET /api/invoices
     * @param query 分页查询参数（PageQuery对象，包含页码、页大小等）
     * @return Result包含分页发票数据的PageResult
     */
    @Operation(summary = "分页查询发票列表") // Swagger接口摘要描述
    @GetMapping // 映射GET请求到基础路径
    public Result<PageResult<Invoice>> list(PageQuery query) {
        return invoiceService.list(query); // 调用服务层分页查询发票列表
    }

    /**
     * 查询发票详情接口
     * 功能：根据ID查询单个发票的详细信息
     * HTTP方法: GET /api/invoices/{id}
     * @param id 发票ID（PathVariable，路径参数）
     * @return Result包含发票详情的Invoice对象
     */
    @Operation(summary = "查询发票详情") // Swagger接口摘要描述
    @GetMapping("/{id}") // 映射GET请求到/{id}路径
    public Result<Invoice> detail(@PathVariable Long id) { // 从路径中获取发票ID
        return invoiceService.detail(id); // 调用服务层查询发票详情
    }

    /**
     * 查询订单发票列表接口
     * 功能：根据订单ID查询该订单关联的所有发票
     * HTTP方法: GET /api/invoices/order/{orderId}
     * @param orderId 订单ID（PathVariable，路径参数）
     * @return Result包含该订单发票列表的List集合
     */
    @Operation(summary = "查询订单发票列表") // Swagger接口摘要描述
    @GetMapping("/order/{orderId}") // 映射GET请求到/order/{orderId}路径
    public Result<List<Invoice>> listByOrder(@PathVariable Long orderId) { // 从路径中获取订单ID
        return invoiceService.listByOrder(orderId); // 调用服务层查询订单的发票列表
    }

    /**
     * 开具发票接口
     * 功能：为指定订单开具新发票，自动记录开票人信息
     * HTTP方法: POST /api/invoices
     * @param body 请求体，包含订单ID、发票抬头、税号、发票类型、内容、备注、金额等信息
     * @param request HTTP请求对象，用于获取当前登录用户ID
     * @return Result操作结果
     */
    @Operation(summary = "开具发票") // Swagger接口摘要描述
    @PostMapping // 映射POST请求到基础路径
    public Result create(@RequestBody Map<String, Object> body, HttpServletRequest request) { // 从请求体获取发票参数，从request获取用户信息
        Long orderId = Long.valueOf(body.get("orderId").toString()); // 从请求体中获取并转换订单ID
        Invoice invoice = new Invoice(); // 创建新的发票对象
        invoice.setTitle((String) body.get("title")); // 设置发票抬头
        invoice.setTaxNo((String) body.get("taxNo")); // 设置发票税号
        invoice.setType((String) body.get("type")); // 设置发票类型
        invoice.setContent((String) body.get("content")); // 设置发票内容
        invoice.setRemark((String) body.get("remark")); // 设置发票备注
        if (body.get("amount") != null) { // 判断金额参数是否存在
            invoice.setAmount(new java.math.BigDecimal(body.get("amount").toString())); // 设置发票金额（转换为BigDecimal）
        }
        Long userId = (Long) request.getAttribute("userId"); // 从请求属性中获取当前登录用户ID（由拦截器设置）
        User user = userMapper.selectById(userId); // 根据用户ID查询用户信息
        String operatorName = user != null ? user.getRealName() : null; // 获取开票人真实姓名，如果用户不存在则为null
        return invoiceService.create(orderId, invoice, userId, operatorName); // 调用服务层开具发票
    }

    /**
     * 作废发票接口
     * 功能：将已开具的发票作废处理
     * HTTP方法: POST /api/invoices/{id}/cancel
     * @param id 发票ID（PathVariable，路径参数）
     * @param body 请求体，包含作废原因reason
     * @return Result操作结果
     */
    @Operation(summary = "作废发票") // Swagger接口摘要描述
    @PostMapping("/{id}/cancel") // 映射POST请求到/{id}/cancel路径
    public Result cancel(@PathVariable Long id, @RequestBody Map<String, String> body) { // 获取路径ID和请求体参数
        String reason = body.get("reason"); // 从请求体中获取作废原因
        return invoiceService.cancel(id, reason); // 调用服务层作废发票
    }

    /**
     * 红冲发票接口
     * 功能：对已开具的发票进行红字冲销处理
     * HTTP方法: POST /api/invoices/{id}/red
     * @param id 发票ID（PathVariable，路径参数）
     * @param body 请求体，包含红冲原因reason
     * @return Result操作结果
     */
    @Operation(summary = "红冲发票") // Swagger接口摘要描述
    @PostMapping("/{id}/red") // 映射POST请求到/{id}/red路径
    public Result redInvoice(@PathVariable Long id, @RequestBody Map<String, String> body) { // 获取路径ID和请求体参数
        String reason = body.get("reason"); // 从请求体中获取红冲原因
        return invoiceService.redInvoice(id, reason); // 调用服务层红冲发票
    }
}