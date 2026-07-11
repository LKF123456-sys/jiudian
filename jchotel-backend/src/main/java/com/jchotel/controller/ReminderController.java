package com.jchotel.controller;

import com.jchotel.entity.Customer;
import com.jchotel.entity.Order;
import com.jchotel.service.ReminderService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    @Autowired
    private ReminderService reminderService;

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardReminders() {
        return reminderService.getDashboardReminders();
    }

    @GetMapping("/birthdays")
    public Result<List<Customer>> getBirthdayCustomers() {
        return reminderService.getBirthdayCustomers();
    }

    @GetMapping("/today-arrivals")
    public Result<List<Order>> getTodayArrivals() {
        return reminderService.getTodayArrivals();
    }

    @GetMapping("/today-departures")
    public Result<List<Order>> getTodayDepartures() {
        return reminderService.getTodayDepartures();
    }

    @GetMapping("/overdue-checkouts")
    public Result<List<Order>> getOverdueCheckouts() {
        return reminderService.getOverdueCheckouts();
    }

    @GetMapping("/low-deposit")
    public Result<List<Order>> getLowDepositOrders() {
        return reminderService.getLowDepositOrders();
    }
}
