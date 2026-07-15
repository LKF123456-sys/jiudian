// 消费品管理控制器包
package com.jchotel.controller;

// ========== DTO和实体类导入 ==========
import com.jchotel.dto.PageQuery; // 分页查询参数DTO
import com.jchotel.dto.PageResult; // 分页查询结果DTO
import com.jchotel.entity.ChargeItem; // 消费品实体类

// ========== 服务层导入 ==========
import com.jchotel.service.ChargeItemService; // 消费品服务
import com.jchotel.utils.Result; // 统一响应结果封装类

// ========== Swagger/OpenAPI 文档注解导入 ==========
import io.swagger.v3.oas.annotations.Operation; // API操作描述注解
import io.swagger.v3.oas.annotations.tags.Tag; // API分组标签注解
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // 安全要求注解

// ========== Spring框架相关导入 ==========
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.web.bind.annotation.*; // Spring MVC注解（包含GetMapping/PostMapping/PutMapping/DeleteMapping/RequestBody/PathVariable等）

// ========== Java 标准库导入 ==========
import java.util.List; // 列表集合类

/**
 * 消费品管理控制器
 * 负责处理消费品的CRUD操作，包括分页查询、查询所有启用、详情查询、新增、更新、删除
 * URL前缀: /api/charge-items
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/charge-items") // 设置该控制器的基础请求路径
@Tag(name = "消费品管理") // Swagger文档分组标签
@SecurityRequirement(name = "Bearer Authentication") // 需要Bearer Token认证
public class ChargeItemController {

    @Autowired // 自动注入消费品服务
    private ChargeItemService chargeItemService; // 消费品业务服务

    /**
     * 分页查询消费品列表接口
     * 功能：按分页参数查询消费品列表
     * HTTP方法: GET /api/charge-items
     * @param query 分页查询参数（PageQuery对象，包含页码、页大小等）
     * @return Result包含分页消费品数据的PageResult
     */
    @Operation(summary = "分页查询消费品列表") // Swagger接口摘要描述
    @GetMapping // 映射GET请求到基础路径
    public Result<PageResult<ChargeItem>> list(PageQuery query) {
        return chargeItemService.list(query); // 调用服务层分页查询消费品列表
    }

    /**
     * 查询所有启用的消费品接口
     * 功能：获取所有状态为启用的消费品列表（用于添加订单消费品时选择）
     * HTTP方法: GET /api/charge-items/all-enabled
     * @return Result包含所有启用消费品的List集合
     */
    @Operation(summary = "查询所有启用的消费品") // Swagger接口摘要描述
    @GetMapping("/all-enabled") // 映射GET请求到/all-enabled路径
    public Result<List<ChargeItem>> listAllEnabled() {
        return chargeItemService.listAllEnabled(); // 调用服务层查询所有启用的消费品
    }

    /**
     * 查询消费品详情接口
     * 功能：根据ID查询单个消费品的详细信息
     * HTTP方法: GET /api/charge-items/{id}
     * @param id 消费品ID（PathVariable，路径参数）
     * @return Result包含消费品详情的ChargeItem对象
     */
    @Operation(summary = "查询消费品详情") // Swagger接口摘要描述
    @GetMapping("/{id}") // 映射GET请求到/{id}路径
    public Result<ChargeItem> detail(@PathVariable Long id) { // 从路径中获取消费品ID
        return chargeItemService.detail(id); // 调用服务层查询消费品详情
    }

    /**
     * 新增消费品接口
     * 功能：创建新的消费品记录
     * HTTP方法: POST /api/charge-items
     * @param item 消费品对象（RequestBody，请求体JSON）
     * @return Result操作结果
     */
    @Operation(summary = "新增消费品") // Swagger接口摘要描述
    @PostMapping // 映射POST请求到基础路径
    public Result add(@RequestBody ChargeItem item) { // 从请求体中解析消费品对象
        return chargeItemService.add(item); // 调用服务层新增消费品
    }

    /**
     * 更新消费品接口
     * 功能：根据ID更新消费品信息
     * HTTP方法: PUT /api/charge-items/{id}
     * @param id 消费品ID（PathVariable，路径参数）
     * @param item 更新后的消费品对象（RequestBody，请求体JSON）
     * @return Result操作结果
     */
    @Operation(summary = "更新消费品") // Swagger接口摘要描述
    @PutMapping("/{id}") // 映射PUT请求到/{id}路径
    public Result update(@PathVariable Long id, @RequestBody ChargeItem item) { // 获取路径ID和请求体中的消费品对象
        item.setId(id); // 设置消费品ID为路径参数中的ID，确保更新正确的记录
        return chargeItemService.update(item); // 调用服务层更新消费品
    }

    /**
     * 删除消费品接口
     * 功能：根据ID删除消费品记录
     * HTTP方法: DELETE /api/charge-items/{id}
     * @param id 消费品ID（PathVariable，路径参数）
     * @return Result操作结果
     */
    @Operation(summary = "删除消费品") // Swagger接口摘要描述
    @DeleteMapping("/{id}") // 映射DELETE请求到/{id}路径
    public Result delete(@PathVariable Long id) { // 从路径中获取消费品ID
        return chargeItemService.delete(id); // 调用服务层删除消费品
    }
}