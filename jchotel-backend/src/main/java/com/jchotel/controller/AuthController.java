// 认证管理控制器包
package com.jchotel.controller;

// ========== DTO导入 ==========
import com.jchotel.dto.LoginDTO; // 登录请求DTO
import com.jchotel.dto.PasswordDTO; // 修改密码请求DTO

// ========== 服务层导入 ==========
import com.jchotel.service.UserService; // 用户服务（认证相关）
import com.jchotel.utils.Result; // 统一响应结果封装类

// ========== Swagger/OpenAPI 文档注解导入 ==========
import io.swagger.v3.oas.annotations.Operation; // API操作描述注解
import io.swagger.v3.oas.annotations.tags.Tag; // API分组标签注解

// ========== Spring框架相关导入 ==========
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.validation.annotation.Validated; // 类级别参数校验注解
import org.springframework.web.bind.annotation.*; // Spring MVC注解（包含PostMapping/RequestBody/Valid等）

// ========== Java EE/Servlet/Validation 相关导入 ==========
import jakarta.servlet.http.HttpServletRequest; // HTTP请求对象
import jakarta.validation.Valid; // 参数校验注解

// ========== Java 标准库导入 ==========
import java.util.Map; // 键值对映射类

/**
 * 认证管理控制器
 * 负责处理用户认证相关业务，包括登录、登出、修改密码
 * URL前缀: /api/auth
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/auth") // 设置该控制器的基础请求路径
@Validated // 启用类级别的参数校验
@Tag(name = "认证管理") // Swagger文档分组标签
public class AuthController {

    @Autowired // 自动注入用户服务
    private UserService userService; // 用户业务服务（处理认证逻辑）

    /**
     * 用户登录接口
     * 功能：验证用户名密码，生成登录token，返回用户信息和token
     * HTTP方法: POST /api/auth/login
     * @param loginDTO 登录信息DTO（RequestBody，包含用户名、密码，@Valid启用参数校验）
     * @return Result包含登录结果的Map集合（含token、用户信息等）
     */
    @Operation(summary = "用户登录") // Swagger接口摘要描述
    @PostMapping("/login") // 映射POST请求到/login路径
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) { // 校验并接收登录DTO
        return userService.login(loginDTO); // 调用服务层执行登录逻辑
    }

    /**
     * 用户登出接口
     * 功能：处理用户退出登录（前端清除token即可，后端无需特殊处理）
     * HTTP方法: POST /api/auth/logout
     * @return Result包含退出成功提示
     */
    @Operation(summary = "用户登出") // Swagger接口摘要描述
    @PostMapping("/logout") // 映射POST请求到/logout路径
    public Result<String> logout() {
        return Result.success("退出成功", null); // 直接返回退出成功响应
    }

    /**
     * 修改密码接口
     * 功能：当前登录用户修改自己的登录密码
     * HTTP方法: POST /api/auth/password
     * @param passwordDTO 修改密码DTO（RequestBody，包含原密码、新密码，@Valid启用参数校验）
     * @param request HTTP请求对象，用于获取当前登录用户ID
     * @return Result包含修改结果提示信息的String
     */
    @Operation(summary = "修改密码") // Swagger接口摘要描述
    @PostMapping("/password") // 映射POST请求到/password路径
    public Result<String> changePassword(@Valid @RequestBody PasswordDTO passwordDTO, HttpServletRequest request) { // 校验并接收修改密码DTO，获取请求对象
        return userService.changePassword(passwordDTO, request); // 调用服务层执行修改密码逻辑
    }
}
