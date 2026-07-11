package com.jchotel.controller;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.Customer;
import com.jchotel.entity.Room;
import com.jchotel.service.CustomerService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @GetMapping
    public Result<PageResult<Customer>> list(PageQuery query) {
        return customerService.list(query);
    }

    @GetMapping("/{id}")
    public Result<Customer> detail(@PathVariable Long id) {
        return customerService.detail(id);
    }

    @GetMapping("/{id}/stats")
    public Result<Map<String, Object>> stats(@PathVariable Long id) {
        return customerService.getCustomerStats(id);
    }

    @PostMapping
    public Result<String> add(@RequestBody Customer customer) {
        return customerService.add(customer);
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Customer customer) {
        customer.setId(id);
        return customerService.update(customer);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return customerService.delete(id);
    }

    @PostMapping("/{id}/blacklist")
    public Result<String> addToBlacklist(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return customerService.addToBlacklist(id, body.get("reason"));
    }

    @DeleteMapping("/{id}/blacklist")
    public Result<String> removeFromBlacklist(@PathVariable Long id) {
        return customerService.removeFromBlacklist(id);
    }

    @GetMapping("/blacklist")
    public Result<List<Customer>> blacklist() {
        return customerService.blacklist();
    }

    @GetMapping("/birthdays")
    public Result<List<Customer>> birthdays() {
        return customerService.birthdayList();
    }

    @GetMapping("/recommend-rooms")
    public Result<List<Room>> recommendRooms(
            @RequestParam(required = false) Long typeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkIn,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkOut) {
        return customerService.recommendRooms(typeId, checkIn, checkOut);
    }
}
