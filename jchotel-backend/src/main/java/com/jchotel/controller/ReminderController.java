package com.jchotel.controller;

import com.jchotel.entity.Customer;
import com.jchotel.entity.Order;
import com.jchotel.service.ReminderService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
@Tag(name = "提醒事项")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @Operation(summary = "获取首页提醒事项")
    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardReminders() {
        return reminderService.getDashboardReminders();
    }

    @Operation(summary = "获取生日客户提醒")
    @GetMapping("/birthdays")
    public Result<List<Customer>> getBirthdayCustomers() {
        return reminderService.getBirthdayCustomers();
    }

    @Operation(summary = "获取今日入住提醒")
    @GetMapping("/today-arrivals")
    public Result<List<Order>> getTodayArrivals() {
        return reminderService.getTodayArrivals();
    }

    @Operation(summary = "获取今日离店提醒")
    @GetMapping("/today-departures")
    public Result<List<Order>> getTodayDepartures() {
        return reminderService.getTodayDepartures();
    }

    @Operation(summary = "获取逾期退房提醒")
    @GetMapping("/overdue-checkouts")
    public Result<List<Order>> getOverdueCheckouts() {
        return reminderService.getOverdueCheckouts();
    }

    @Operation(summary = "获取低押金提醒")
    @GetMapping("/low-deposit")
    public Result<List<Order>> getLowDepositOrders() {
        return reminderService.getLowDepositOrders();
    }
}
