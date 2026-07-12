// 房型管理控制器包
package com.jchotel.controller;

// ========== 实体类导入 ==========
import com.jchotel.entity.RoomType; // 房型实体类

// ========== 服务层导入 ==========
import com.jchotel.service.RoomTypeService; // 房型服务
import com.jchotel.utils.Result; // 统一响应结果封装类

// ========== Swagger/OpenAPI 文档注解导入 ==========
import io.swagger.v3.oas.annotations.Operation; // API操作描述注解
import io.swagger.v3.oas.annotations.tags.Tag; // API分组标签注解

// ========== Spring框架相关导入 ==========
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.web.bind.annotation.*; // Spring MVC注解（包含GetMapping/PostMapping/PutMapping/DeleteMapping/RequestBody/PathVariable等）

// ========== Java 标准库导入 ==========
import java.util.List; // 列表集合类

/**
 * 房型管理控制器
 * 负责处理房型的CRUD操作，包括查询所有房型、新增房型、更新房型、删除房型
 * URL前缀: /api/room-types
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/room-types") // 设置该控制器的基础请求路径
@Tag(name = "房型管理") // Swagger文档分组标签
public class RoomTypeController {

    @Autowired // 自动注入房型服务
    private RoomTypeService roomTypeService; // 房型业务服务

    /**
     * 查询所有房型列表接口
     * 功能：获取系统中所有房型（用于下拉选择等场景）
     * HTTP方法: GET /api/room-types
     * @return Result包含所有房型列表的List集合
     */
    @Operation(summary = "查询所有房型列表") // Swagger接口摘要描述
    @GetMapping // 映射GET请求到基础路径
    public Result<List<RoomType>> list() {
        return roomTypeService.listAll(); // 调用服务层查询所有房型
    }

    /**
     * 新增房型接口
     * 功能：创建新的房型记录
     * HTTP方法: POST /api/room-types
     * @param roomType 房型对象（RequestBody，请求体JSON）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "新增房型") // Swagger接口摘要描述
    @PostMapping // 映射POST请求到基础路径
    public Result<String> add(@RequestBody RoomType roomType) { // 从请求体中解析房型对象
        return roomTypeService.add(roomType); // 调用服务层新增房型
    }

    /**
     * 更新房型接口
     * 功能：根据ID更新房型信息
     * HTTP方法: PUT /api/room-types/{id}
     * @param id 房型ID（PathVariable，路径参数）
     * @param roomType 更新后的房型对象（RequestBody，请求体JSON）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "更新房型") // Swagger接口摘要描述
    @PutMapping("/{id}") // 映射PUT请求到/{id}路径
    public Result<String> update(@PathVariable Long id, @RequestBody RoomType roomType) { // 获取路径ID和请求体中的房型对象
        roomType.setId(id); // 设置房型ID为路径参数中的ID，确保更新正确的记录
        return roomTypeService.update(roomType); // 调用服务层更新房型
    }

    /**
     * 删除房型接口
     * 功能：根据ID删除房型记录
     * HTTP方法: DELETE /api/room-types/{id}
     * @param id 房型ID（PathVariable，路径参数）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "删除房型") // Swagger接口摘要描述
    @DeleteMapping("/{id}") // 映射DELETE请求到/{id}路径
    public Result<String> delete(@PathVariable Long id) { // 从路径中获取房型ID
        return roomTypeService.delete(id); // 调用服务层删除房型
    }
}
