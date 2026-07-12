package com.jchotel.controller;

import com.jchotel.annotation.RequireRole;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.User;
import com.jchotel.service.UserService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理")
@RequireRole({"admin"})
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        return userService.info(request);
    }

    @Operation(summary = "分页查询用户列表")
    @GetMapping("/list")
    public Result<PageResult<User>> list(PageQuery query) {
        return userService.list(query);
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return userService.detail(id);
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<String> add(@RequestBody User user) {
        return userService.add(user);
    }

    @Operation(summary = "更新用户")
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.update(user);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        return userService.delete(id, request);
    }

    @Operation(summary = "切换用户状态")
    @PutMapping("/{id}/status/{status}")
    public Result<String> toggleStatus(@PathVariable Long id, @PathVariable Integer status, HttpServletRequest request) {
        return userService.toggleStatus(id, status, request);
    }

    @Operation(summary = "重置用户密码")
    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        return userService.resetPassword(id);
    }
}
