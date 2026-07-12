package com.jchotel.controller;

import com.jchotel.dto.LoginDTO;
import com.jchotel.dto.PasswordDTO;
import com.jchotel.service.UserService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
@Tag(name = "认证管理")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("退出成功", null);
    }

    @Operation(summary = "修改密码")
    @PostMapping("/password")
    public Result<String> changePassword(@Valid @RequestBody PasswordDTO passwordDTO, HttpServletRequest request) {
        return userService.changePassword(passwordDTO, request);
    }
}
