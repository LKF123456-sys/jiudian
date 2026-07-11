package com.jchotel.service;

import com.jchotel.entity.Customer;
import com.jchotel.entity.Order;
import com.jchotel.utils.Result;

import java.util.List;
import java.util.Map;

public interface ReminderService {
    Result<Map<String, Object>> getDashboardReminders();
    Result<List<Customer>> getBirthdayCustomers();
    Result<List<Order>> getTodayArrivals();
    Result<List<Order>> getTodayDepartures();
    Result<List<Order>> getOverdueCheckouts();
    Result<List<Order>> getLowDepositOrders();
}
