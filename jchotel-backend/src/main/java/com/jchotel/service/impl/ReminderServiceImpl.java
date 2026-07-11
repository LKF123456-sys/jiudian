package com.jchotel.service.impl;

import com.jchotel.entity.Customer;
import com.jchotel.entity.Order;
import com.jchotel.mapper.CustomerMapper;
import com.jchotel.mapper.OrderMapper;
import com.jchotel.service.ReminderService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReminderServiceImpl implements ReminderService {

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Result<Map<String, Object>> getDashboardReminders() {
        Map<String, Object> reminders = new HashMap<>();
        List<Customer> birthdays = customerMapper.findBirthdayCustomers(LocalDate.now());
        reminders.put("birthdayCount", birthdays.size());
        reminders.put("birthdayCustomers", birthdays);
        List<Order> todayArrivals = orderMapper.findTodayArrivals();
        reminders.put("arrivalCount", todayArrivals.size());
        reminders.put("todayArrivals", todayArrivals);
        List<Order> todayDepartures = orderMapper.findTodayDepartures();
        reminders.put("departureCount", todayDepartures.size());
        reminders.put("todayDepartures", todayDepartures);
        List<Order> overdueCheckouts = orderMapper.findOverdueCheckouts(1, 10);
        reminders.put("overdueCount", overdueCheckouts.size());
        reminders.put("overdueCheckouts", overdueCheckouts);
        List<Order> lowDepositOrders = orderMapper.findLowDepositOrders();
        reminders.put("lowDepositCount", lowDepositOrders.size());
        reminders.put("lowDepositOrders", lowDepositOrders);
        int pendingCount = orderMapper.pendingCount();
        reminders.put("pendingCount", pendingCount);
        return Result.success(reminders);
    }

    @Override
    public Result<List<Customer>> getBirthdayCustomers() {
        List<Customer> list = customerMapper.findBirthdayCustomers(LocalDate.now());
        return Result.success(list);
    }

    @Override
    public Result<List<Order>> getTodayArrivals() {
        List<Order> list = orderMapper.findTodayArrivals();
        return Result.success(list);
    }

    @Override
    public Result<List<Order>> getTodayDepartures() {
        List<Order> list = orderMapper.findTodayDepartures();
        return Result.success(list);
    }

    @Override
    public Result<List<Order>> getOverdueCheckouts() {
        List<Order> list = orderMapper.findOverdueCheckouts(1, 100);
        return Result.success(list);
    }

    @Override
    public Result<List<Order>> getLowDepositOrders() {
        List<Order> list = orderMapper.findLowDepositOrders();
        return Result.success(list);
    }
}
