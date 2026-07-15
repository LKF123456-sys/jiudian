// 用户管理控制器包
package com.jchotel.controller;

// ========== 自定义注解和DTO导入 ==========
import com.jchotel.annotation.RequireRole; // 自定义角色权限校验注解
import com.jchotel.dto.PageQuery; // 分页查询参数DTO
import com.jchotel.dto.PageResult; // 分页查询结果DTO
import com.jchotel.entity.User; // 用户实体类

// ========== 服务层导入 ==========
import com.jchotel.service.UserService; // 用户服务
import com.jchotel.utils.Result; // 统一响应结果封装类

// ========== Swagger/OpenAPI 文档注解导入 ==========
import io.swagger.v3.oas.annotations.Operation; // API操作描述注解
import io.swagger.v3.oas.annotations.tags.Tag; // API分组标签注解
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // 安全要求注解

// ========== Spring框架相关导入 ==========
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.web.bind.annotation.*; // Spring MVC注解（包含GetMapping/PostMapping/PutMapping/DeleteMapping/RequestBody/PathVariable等）

// ========== Java EE/Servlet 相关导入 ==========
import jakarta.servlet.http.HttpServletRequest; // HTTP请求对象

/**
 * 用户管理控制器
 * 负责处理系统用户的管理操作，包括获取当前用户信息、用户CRUD、状态切换、密码重置等
 * URL前缀: /api/user
 * 权限要求：整个控制器仅admin角色可访问（除获取当前用户信息外）
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/user") // 设置该控制器的基础请求路径
@Tag(name = "用户管理") // Swagger文档分组标签
@SecurityRequirement(name = "Bearer Authentication") // 需要Bearer Token认证
@RequireRole({"admin"}) // 类级别权限要求：仅管理员角色可访问该控制器所有接口
public class UserController {

    @Autowired // 自动注入用户服务
    private UserService userService; // 用户业务服务

    /**
     * 获取当前用户信息接口
     * 功能：根据请求中的token获取当前登录用户的详细信息
     * HTTP方法: GET /api/user/info
     * @param request HTTP请求对象，用于获取当前登录用户ID
     * @return Result包含当前用户信息的User对象
     */
    @Operation(summary = "获取当前用户信息") // Swagger接口摘要描述
    @GetMapping("/info") // 映射GET请求到/info路径
    public Result<User> info(HttpServletRequest request) { // 获取HTTP请求对象
        return userService.info(request); // 调用服务层获取当前登录用户信息
    }

    /**
     * 分页查询用户列表接口
     * 功能：按分页参数查询系统用户列表
     * HTTP方法: GET /api/user/list
     * @param query 分页查询参数（PageQuery对象，包含页码、页大小等）
     * @return Result包含分页用户数据的PageResult
     */
    @Operation(summary = "分页查询用户列表") // Swagger接口摘要描述
    @GetMapping("/list") // 映射GET请求到/list路径
    public Result<PageResult<User>> list(PageQuery query) {
        return userService.list(query); // 调用服务层分页查询用户列表
    }

    /**
     * 查询用户详情接口
     * 功能：根据ID查询单个用户的详细信息
     * HTTP方法: GET /api/user/{id}
     * @param id 用户ID（PathVariable，路径参数）
     * @return Result包含用户详情的User对象
     */
    @Operation(summary = "查询用户详情") // Swagger接口摘要描述
    @GetMapping("/{id}") // 映射GET请求到/{id}路径
    public Result<User> detail(@PathVariable Long id) { // 从路径中获取用户ID
        return userService.detail(id); // 调用服务层查询用户详情
    }

    /**
     * 新增用户接口
     * 功能：创建新的系统用户账号
     * HTTP方法: POST /api/user
     * @param user 用户对象（RequestBody，请求体JSON）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "新增用户") // Swagger接口摘要描述
    @PostMapping // 映射POST请求到基础路径
    public Result<String> add(@RequestBody User user) { // 从请求体中解析用户对象
        return userService.add(user); // 调用服务层新增用户
    }

    /**
     * 更新用户接口
     * 功能：根据ID更新用户信息
     * HTTP方法: PUT /api/user/{id}
     * @param id 用户ID（PathVariable，路径参数）
     * @param user 更新后的用户对象（RequestBody，请求体JSON）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "更新用户") // Swagger接口摘要描述
    @PutMapping("/{id}") // 映射PUT请求到/{id}路径
    public Result<String> update(@PathVariable Long id, @RequestBody User user) { // 获取路径ID和请求体中的用户对象
        user.setId(id); // 设置用户ID为路径参数中的ID，确保更新正确的记录
        return userService.update(user); // 调用服务层更新用户
    }

    /**
     * 删除用户接口
     * 功能：根据ID删除用户账号
     * HTTP方法: DELETE /api/user/{id}
     * @param id 用户ID（PathVariable，路径参数）
     * @param request HTTP请求对象，用于获取当前操作人信息（防止删除自己）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "删除用户") // Swagger接口摘要描述
    @DeleteMapping("/{id}") // 映射DELETE请求到/{id}路径
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) { // 获取路径ID和HTTP请求对象
        return userService.delete(id, request); // 调用服务层删除用户
    }

    /**
     * 切换用户状态接口
     * 功能：启用或禁用用户账号
     * HTTP方法: PUT /api/user/{id}/status/{status}
     * @param id 用户ID（PathVariable，路径参数）
     * @param status 目标状态（PathVariable，路径参数，1启用/0禁用）
     * @param request HTTP请求对象，用于获取当前操作人信息（防止禁用自己）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "切换用户状态") // Swagger接口摘要描述
    @PutMapping("/{id}/status/{status}") // 映射PUT请求到/{id}/status/{status}路径
    public Result<String> toggleStatus(@PathVariable Long id, @PathVariable Integer status, HttpServletRequest request) { // 获取路径中的用户ID和状态值，获取请求对象
        return userService.toggleStatus(id, status, request); // 调用服务层切换用户状态
    }

    /**
     * 重置用户密码接口
     * 功能：将指定用户的密码重置为默认密码
     * HTTP方法: PUT /api/user/{id}/reset-password
     * @param id 用户ID（PathVariable，路径参数）
     * @return Result包含操作结果提示信息的String
     */
    @Operation(summary = "重置用户密码") // Swagger接口摘要描述
    @PutMapping("/{id}/reset-password") // 映射PUT请求到/{id}/reset-password路径
    public Result<String> resetPassword(@PathVariable Long id) { // 从路径中获取用户ID
        return userService.resetPassword(id); // 调用服务层重置用户密码
    }
}