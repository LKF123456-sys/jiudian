// 客户管理控制器包
package com.jchotel.controller;

// ========== DTO和实体类导入 ==========
import com.jchotel.dto.PageQuery; // 分页查询参数DTO
import com.jchotel.dto.PageResult; // 分页查询结果DTO
import com.jchotel.entity.Customer; // 客户实体类
import com.jchotel.entity.Room; // 客房实体类

// ========== 服务层导入 ==========
import com.jchotel.service.CustomerService; // 客户服务
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
import java.util.List; // 列表集合类
import java.util.Map; // 键值对映射类

/**
 * 客户管理控制器
 * 负责处理客户信息管理，包括CRUD操作、黑名单管理、生日客户查询、客房推荐等
 * URL前缀: /api/customers
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/customers") // 设置该控制器的基础请求路径
@Tag(name = "客户管理") // Swagger文档分组标签
@SecurityRequirement(name = "Bearer Authentication") // 需要Bearer Token认证
public class CustomerController {

    @Autowired // 自动注入客户服务
    private CustomerService customerService; // 客户业务服务

    /**
     * 分页查询客户列表接口
     * 功能：按分页参数和条件查询客户列表
     * HTTP方法: GET /api/customers
     * @param query 分页查询参数（PageQuery对象，包含页码、页大小等）
     * @return Result包含分页客户数据的PageResult
     */
    @Operation(summary = "分页查询客户列表") // Swagger接口摘要描述
    @GetMapping // 映射GET请求到基础路径
    public Result<PageResult<Customer>> list(PageQuery query) {
        return customerService.list(query); // 调用服务层分页查询客户列表
    }

    /**
     * 查询客户详情接口
     * 功能：根据ID查询单个客户的详细信息
     * HTTP方法: GET /api/customers/{id}
     * @param id 客户ID（PathVariable，路径参数）
     * @return Result包含客户详情的Customer对象
     */
    @Operation(summary = "查询客户详情") // Swagger接口摘要描述
    @GetMapping("/{id}") // 映射GET请求到/{id}路径
    public Result<Customer> detail(@PathVariable Long id) { // 从路径中获取客户ID
        return customerService.detail(id); // 调用服务层查询客户详情
    }

    /**
     * 查询客户统计信息接口
     * 功能：查询指定客户的入住统计、消费统计等数据
     * HTTP方法: GET /api/customers/{id}/stats
     * @param id 客户ID（PathVariable，路径参数）
     * @return Result包含客户统计数据的Map集合
     */
    @Operation(summary = "查询客户统计信息") // Swagger接口摘要描述
    @GetMapping("/{id}/stats") // 映射GET请求到/{id}/stats路径
    public Result<Map<String, Object>> stats(@PathVariable Long id) { // 从路径中获取客户ID
        return customerService.getCustomerStats(id); // 调用服务层获取客户统计信息
    }

    /**
     * 新增客户接口
     * 功能：创建新的客户档案
     * HTTP方法: POST /api/customers
     * @param customer 客户对象（RequestBody，请求体JSON）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "新增客户") // Swagger接口摘要描述
    @PostMapping // 映射POST请求到基础路径
    public Result<String> add(@RequestBody Customer customer) { // 从请求体中解析客户对象
        return customerService.add(customer); // 调用服务层新增客户
    }

    /**
     * 更新客户接口
     * 功能：根据ID更新客户信息
     * HTTP方法: PUT /api/customers/{id}
     * @param id 客户ID（PathVariable，路径参数）
     * @param customer 更新后的客户对象（RequestBody，请求体JSON）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "更新客户") // Swagger接口摘要描述
    @PutMapping("/{id}") // 映射PUT请求到/{id}路径
    public Result<String> update(@PathVariable Long id, @RequestBody Customer customer) { // 获取路径ID和请求体中的客户对象
        customer.setId(id); // 设置客户ID为路径参数中的ID，确保更新正确的记录
        return customerService.update(customer); // 调用服务层更新客户
    }

    /**
     * 删除客户接口
     * 功能：根据ID删除客户记录
     * HTTP方法: DELETE /api/customers/{id}
     * @param id 客户ID（PathVariable，路径参数）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "删除客户") // Swagger接口摘要描述
    @DeleteMapping("/{id}") // 映射DELETE请求到/{id}路径
    public Result<String> delete(@PathVariable Long id) { // 从路径中获取客户ID
        return customerService.delete(id); // 调用服务层删除客户
    }

    /**
     * 加入黑名单接口
     * 功能：将指定客户加入黑名单，记录拉黑原因
     * HTTP方法: POST /api/customers/{id}/blacklist
     * @param id 客户ID（PathVariable，路径参数）
     * @param body 请求体，包含拉黑原因reason
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "加入黑名单") // Swagger接口摘要描述
    @PostMapping("/{id}/blacklist") // 映射POST请求到/{id}/blacklist路径
    public Result<String> addToBlacklist(@PathVariable Long id, @RequestBody Map<String, String> body) { // 获取路径ID和请求体参数
        return customerService.addToBlacklist(id, body.get("reason")); // 调用服务层将客户加入黑名单
    }

    /**
     * 移出黑名单接口
     * 功能：将指定客户从黑名单中移除
     * HTTP方法: DELETE /api/customers/{id}/blacklist
     * @param id 客户ID（PathVariable，路径参数）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "移出黑名单") // Swagger接口摘要描述
    @DeleteMapping("/{id}/blacklist") // 映射DELETE请求到/{id}/blacklist路径
    public Result<String> removeFromBlacklist(@PathVariable Long id) { // 从路径中获取客户ID
        return customerService.removeFromBlacklist(id); // 调用服务层将客户移出黑名单
    }

    /**
     * 查询黑名单列表接口
     * 功能：获取所有在黑名单中的客户列表
     * HTTP方法: GET /api/customers/blacklist
     * @return Result包含黑名单客户列表的List集合
     */
    @Operation(summary = "查询黑名单列表") // Swagger接口摘要描述
    @GetMapping("/blacklist") // 映射GET请求到/blacklist路径
    public Result<List<Customer>> blacklist() {
        return customerService.blacklist(); // 调用服务层查询黑名单列表
    }

    /**
     * 查询生日客户列表接口
     * 功能：获取今日生日的客户列表
     * HTTP方法: GET /api/customers/birthdays
     * @return Result包含今日生日客户列表的List集合
     */
    @Operation(summary = "查询生日客户列表") // Swagger接口摘要描述
    @GetMapping("/birthdays") // 映射GET请求到/birthdays路径
    public Result<List<Customer>> birthdays() {
        return customerService.birthdayList(); // 调用服务层查询生日客户列表
    }

    /**
     * 推荐客房接口
     * 功能：根据房型和入住离店时间为客户推荐可用客房
     * HTTP方法: GET /api/customers/recommend-rooms
     * @param typeId 房型ID（可选，RequestParam）
     * @param checkIn 入住时间（必填，RequestParam，格式yyyy-MM-dd HH:mm:ss）
     * @param checkOut 离店时间（必填，RequestParam，格式yyyy-MM-dd HH:mm:ss）
     * @return Result包含推荐客房列表的List集合
     */
    @Operation(summary = "推荐客房") // Swagger接口摘要描述
    @GetMapping("/recommend-rooms") // 映射GET请求到/recommend-rooms路径
    public Result<List<Room>> recommendRooms(
            @RequestParam(required = false) Long typeId, // 可选的房型ID参数
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkIn, // 必填的入住时间参数，指定日期格式
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkOut) { // 必填的离店时间参数，指定日期格式
        return customerService.recommendRooms(typeId, checkIn, checkOut); // 调用服务层推荐客房
    }
}