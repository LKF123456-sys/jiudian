package com.jchotel.controller;

import com.jchotel.dto.LoginDTO;
import com.jchotel.dto.PasswordDTO;
import com.jchotel.service.UserService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginDTO loginDTO) {
        return userService.login(loginDTO);
    }

    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("退出成功", null);
    }

    @PostMapping("/password")
    public Result<String> changePassword(@Valid @RequestBody PasswordDTO passwordDTO, HttpServletRequest request) {
        return userService.changePassword(passwordDTO, request);
    }
}
