// 客房管理控制器包
package com.jchotel.controller;

// ========== DTO、实体类和Mapper导入 ==========
import com.jchotel.dto.PageQuery; // 分页查询参数DTO
import com.jchotel.dto.PageResult; // 分页查询结果DTO
import com.jchotel.entity.Room; // 客房实体类
import com.jchotel.mapper.OrderMapper; // 订单数据访问Mapper

// ========== 服务层导入 ==========
import com.jchotel.service.RoomService; // 客房服务
import com.jchotel.utils.Result; // 统一响应结果封装类

// ========== Swagger/OpenAPI 文档注解导入 ==========
import io.swagger.v3.oas.annotations.Operation; // API操作描述注解
import io.swagger.v3.oas.annotations.tags.Tag; // API分组标签注解
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // 安全要求注解

// ========== Spring框架相关导入 ==========
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.format.annotation.DateTimeFormat; // 日期时间格式化注解
import org.springframework.web.bind.annotation.*; // Spring MVC注解（包含GetMapping/PostMapping/PutMapping/DeleteMapping/RequestBody/PathVariable/RequestParam等）

// ========== Java 标准库导入 ==========
import java.time.LocalDateTime; // 本地日期时间类
import java.time.format.DateTimeFormatter; // 日期时间格式化器
import java.util.List; // 列表集合类
import java.util.Map; // 键值对映射类

/**
 * 客房管理控制器
 * 负责处理客房信息管理，包括CRUD操作、房态看板、状态统计、可用客房查询、状态更新等
 * URL前缀: /api/rooms
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/rooms") // 设置该控制器的基础请求路径
@Tag(name = "客房管理") // Swagger文档分组标签
@SecurityRequirement(name = "Bearer Authentication") // 需要Bearer Token认证
public class RoomController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // 日期时间格式化常量，统一日期格式

    @Autowired // 自动注入客房服务
    private RoomService roomService; // 客房业务服务

    @Autowired // 自动注入订单Mapper
    private OrderMapper orderMapper; // 订单数据访问接口

    /**
     * 分页查询客房列表接口
     * 功能：按分页参数和条件查询客房列表
     * HTTP方法: GET /api/rooms
     * @param query 分页查询参数（PageQuery对象，包含页码、页大小等）
     * @return Result包含分页客房数据的PageResult
     */
    @Operation(summary = "分页查询客房列表") // Swagger接口摘要描述
    @GetMapping // 映射GET请求到基础路径
    public Result<PageResult<Room>> list(PageQuery query) {
        return roomService.list(query); // 调用服务层分页查询客房列表
    }

    /**
     * 查询房态看板接口
     * 功能：获取所有客房的实时状态，用于前台房态看板展示
     * HTTP方法: GET /api/rooms/board
     * @return Result包含所有客房列表的List集合（含实时状态）
     */
    @Operation(summary = "查询房态看板") // Swagger接口摘要描述
    @GetMapping("/board") // 映射GET请求到/board路径
    public Result<List<Room>> board() {
        return roomService.board(); // 调用服务层查询房态看板数据
    }

    /**
     * 查询客房状态统计接口
     * 功能：统计各状态（空闲、入住、清扫、维修等）的客房数量
     * HTTP方法: GET /api/rooms/status-stats
     * @return Result包含客房状态统计数据的Map集合
     */
    @Operation(summary = "查询客房状态统计") // Swagger接口摘要描述
    @GetMapping("/status-stats") // 映射GET请求到/status-stats路径
    public Result<Map<String, Object>> statusStats() {
        return roomService.statusStats(); // 调用服务层查询客房状态统计
    }

    /**
     * 查询可用客房接口
     * 功能：根据入住时间和离店时间查询可用的客房
     * HTTP方法: GET /api/rooms/available
     * @param typeId 房型ID（可选，RequestParam，按房型筛选）
     * @param checkInTime 入住时间（必填，RequestParam，格式yyyy-MM-dd HH:mm:ss）
     * @param expectedCheckOutTime 预计离店时间（必填，RequestParam，格式yyyy-MM-dd HH:mm:ss）
     * @return Result包含可用客房列表的List集合
     */
    @Operation(summary = "查询可用客房") // Swagger接口摘要描述
    @GetMapping("/available") // 映射GET请求到/available路径
    public Result<List<Room>> availableRooms(
            @RequestParam(required = false) Long typeId, // 可选的房型ID筛选参数
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkInTime, // 必填的入住时间参数，指定日期格式
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime expectedCheckOutTime) { // 必填的预计离店时间参数，指定日期格式
        String checkIn = checkInTime.format(FMT); // 将入住时间格式化为字符串
        String checkOut = expectedCheckOutTime.format(FMT); // 将预计离店时间格式化为字符串
        return roomService.available(typeId, checkIn, checkOut); // 调用服务层查询可用客房
    }

    /**
     * 查询客房详情接口
     * 功能：根据ID查询单个客房的详细信息
     * HTTP方法: GET /api/rooms/{id}
     * @param id 客房ID（PathVariable，路径参数）
     * @return Result包含客房详情的Room对象
     */
    @Operation(summary = "查询客房详情") // Swagger接口摘要描述
    @GetMapping("/{id}") // 映射GET请求到/{id}路径
    public Result<Room> detail(@PathVariable Long id) { // 从路径中获取客房ID
        return roomService.detail(id); // 调用服务层查询客房详情
    }

    /**
     * 新增客房接口
     * 功能：创建新的客房记录
     * HTTP方法: POST /api/rooms
     * @param room 客房对象（RequestBody，请求体JSON）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "新增客房") // Swagger接口摘要描述
    @PostMapping // 映射POST请求到基础路径
    public Result<String> add(@RequestBody Room room) { // 从请求体中解析客房对象
        return roomService.add(room); // 调用服务层新增客房
    }

    /**
     * 更新客房接口
     * 功能：根据ID更新客房信息
     * HTTP方法: PUT /api/rooms/{id}
     * @param id 客房ID（PathVariable，路径参数）
     * @param room 更新后的客房对象（RequestBody，请求体JSON）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "更新客房") // Swagger接口摘要描述
    @PutMapping("/{id}") // 映射PUT请求到/{id}路径
    public Result<String> update(@PathVariable Long id, @RequestBody Room room) { // 获取路径ID和请求体中的客房对象
        room.setId(id); // 设置客房ID为路径参数中的ID，确保更新正确的记录
        return roomService.update(room); // 调用服务层更新客房
    }

    /**
     * 删除客房接口
     * 功能：根据ID删除客房记录
     * HTTP方法: DELETE /api/rooms/{id}
     * @param id 客房ID（PathVariable，路径参数）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "删除客房") // Swagger接口摘要描述
    @DeleteMapping("/{id}") // 映射DELETE请求到/{id}路径
    public Result<String> delete(@PathVariable Long id) { // 从路径中获取客房ID
        return roomService.delete(id); // 调用服务层删除客房
    }

    /**
     * 更新客房状态接口
     * 功能：单独更新客房的状态（如设置为维修、清扫等）
     * HTTP方法: PUT /api/rooms/{id}/status
     * @param id 客房ID（PathVariable，路径参数）
     * @param room 客房对象（RequestBody，请求体JSON，只取status字段）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "更新客房状态") // Swagger接口摘要描述
    @PutMapping("/{id}/status") // 映射PUT请求到/{id}/status路径
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Room room) { // 获取路径ID和请求体中的客房对象
        return roomService.updateStatus(id, room.getStatus()); // 调用服务层更新客房状态，只传ID和新状态
    }
}