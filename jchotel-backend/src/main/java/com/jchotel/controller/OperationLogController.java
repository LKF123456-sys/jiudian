// 操作日志控制器包
package com.jchotel.controller;

// ========== 自定义注解和DTO导入 ==========
import com.jchotel.annotation.RequireRole; // 自定义角色权限校验注解
import com.jchotel.dto.PageQuery; // 分页查询参数DTO
import com.jchotel.dto.PageResult; // 分页查询结果DTO
import com.jchotel.entity.OperationLog; // 操作日志实体类

// ========== 服务层导入 ==========
import com.jchotel.service.OperationLogService; // 操作日志服务
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

/**
 * 操作日志控制器
 * 负责处理操作日志的分页查询功能
 * URL前缀: /api/operation-logs
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/operation-logs") // 设置该控制器的基础请求路径
@Tag(name = "操作日志") // Swagger文档分组标签
@SecurityRequirement(name = "Bearer Authentication") // 需要Bearer Token认证
@RequireRole({"admin", "manager"}) // 接口权限要求：仅管理员和经理角色可访问
public class OperationLogController {

    @Autowired // 自动注入操作日志服务
    private OperationLogService operationLogService; // 操作日志业务服务

    /**
     * 分页查询操作日志列表接口
     * 功能：按分页参数查询系统操作日志记录
     * HTTP方法: GET /api/operation-logs
     * @param query 分页查询参数（PageQuery对象，包含页码、页大小等）
     * @return Result包含分页操作日志数据的PageResult
     */
    @Operation(summary = "分页查询操作日志列表") // Swagger接口摘要描述
    @GetMapping // 映射GET请求到基础路径
    public Result<PageResult<OperationLog>> list(PageQuery query) {
        return operationLogService.list(query); // 调用服务层分页查询操作日志列表
    }
}