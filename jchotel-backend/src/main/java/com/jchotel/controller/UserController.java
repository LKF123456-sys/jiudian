package com.jchotel.controller;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.User;
import com.jchotel.service.UserService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/info")
    public Result<User> info(HttpServletRequest request) {
        return userService.info(request);
    }

    @GetMapping("/list")
    public Result<PageResult<User>> list(PageQuery query) {
        return userService.list(query);
    }

    @GetMapping("/{id}")
    public Result<User> detail(@PathVariable Long id) {
        return userService.detail(id);
    }

    @PostMapping
    public Result<String> add(@RequestBody User user) {
        return userService.add(user);
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return userService.update(user);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id, HttpServletRequest request) {
        return userService.delete(id, request);
    }

    @PutMapping("/{id}/status/{status}")
    public Result<String> toggleStatus(@PathVariable Long id, @PathVariable Integer status, HttpServletRequest request) {
        return userService.toggleStatus(id, status, request);
    }

    @PutMapping("/{id}/reset-password")
    public Result<String> resetPassword(@PathVariable Long id) {
        return userService.resetPassword(id);
    }
}
