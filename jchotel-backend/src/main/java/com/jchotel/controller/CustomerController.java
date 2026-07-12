package com.jchotel.controller;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.Customer;
import com.jchotel.entity.Room;
import com.jchotel.service.CustomerService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
@Tag(name = "客户管理")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @Operation(summary = "分页查询客户列表")
    @GetMapping
    public Result<PageResult<Customer>> list(PageQuery query) {
        return customerService.list(query);
    }

    @Operation(summary = "查询客户详情")
    @GetMapping("/{id}")
    public Result<Customer> detail(@PathVariable Long id) {
        return customerService.detail(id);
    }

    @Operation(summary = "查询客户统计信息")
    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> stats(@PathVariable Long id) {
        return customerService.getCustomerStats(id);
    }

    @Operation(summary = "新增客户")
    @PostMapping
    public Result<String> add(@RequestBody Customer customer) {
        return customerService.add(customer);
    }

    @Operation(summary = "更新客户")
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Customer customer) {
        customer.setId(id);
        return customerService.update(customer);
    }

    @Operation(summary = "删除客户")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return customerService.delete(id);
    }

    @Operation(summary = "加入黑名单")
    @PostMapping("/{id}/blacklist")
    public Result<String> addToBlacklist(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return customerService.addToBlacklist(id, body.get("reason"));
    }

    @Operation(summary = "移出黑名单")
    @DeleteMapping("/{id}/blacklist")
    public Result<String> removeFromBlacklist(@PathVariable Long id) {
        return customerService.removeFromBlacklist(id);
    }

    @Operation(summary = "查询黑名单列表")
    @GetMapping("/blacklist")
    public Result<List<Customer>> blacklist() {
        return customerService.blacklist();
    }

    @Operation(summary = "查询生日客户列表")
    @GetMapping("/birthdays")
    public Result<List<Customer>> birthdays() {
        return customerService.birthdayList();
    }

    @Operation(summary = "推荐客房")
    @GetMapping("/recommend-rooms")
    public Result<List<Room>> recommendRooms(
            @RequestParam(required = false) Long typeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkIn,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkOut) {
        return customerService.recommendRooms(typeId, checkIn, checkOut);
    }
}
