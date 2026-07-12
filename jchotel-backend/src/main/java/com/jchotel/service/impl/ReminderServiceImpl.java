package com.jchotel.service.impl;

// 客户实体类
import com.jchotel.entity.Customer;
// 订单实体类
import com.jchotel.entity.Order;
// 客户数据访问Mapper
import com.jchotel.mapper.CustomerMapper;
// 订单数据访问Mapper
import com.jchotel.mapper.OrderMapper;
// 提醒服务接口
import com.jchotel.service.ReminderService;
// 统一响应结果封装
import com.jchotel.utils.Result;
// Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// Spring服务层注解
import org.springframework.stereotype.Service;

// 日期类
import java.time.LocalDate;
// HashMap集合
import java.util.HashMap;
// List集合
import java.util.List;
// Map接口
import java.util.Map;

/**
 * 提醒事项服务实现类
 * 聚合各类需要提醒的事项：今日生日客户、今日预计入住、今日预计退房、超时未退房、押金不足、待处理预约
 */
@Service // 标记为Spring服务组件
public class ReminderServiceImpl implements ReminderService {

    @Autowired // 自动注入客户Mapper，查询生日客户
    private CustomerMapper customerMapper;

    @Autowired // 自动注入订单Mapper，查询各类订单提醒
    private OrderMapper orderMapper;

    /**
     * 获取仪表盘所有提醒数据（聚合各类提醒）
     * @return 包含生日、入住、退房、超时、押金不足、待处理预约的数量和列表
     */
    @Override
    public Result<Map<String, Object>> getDashboardReminders() {
        Map<String, Object> reminders = new HashMap<>();
        // 查询今日生日客户列表
        List<Customer> birthdays = customerMapper.findBirthdayCustomers(LocalDate.now());
        reminders.put("birthdayCount", birthdays.size());
        reminders.put("birthdayCustomers", birthdays);
        // 查询今日预计到达订单
        List<Order> todayArrivals = orderMapper.findTodayArrivals();
        reminders.put("arrivalCount", todayArrivals.size());
        reminders.put("todayArrivals", todayArrivals);
        // 查询今日预计离店订单
        List<Order> todayDepartures = orderMapper.findTodayDepartures();
        reminders.put("departureCount", todayDepartures.size());
        reminders.put("todayDepartures", todayDepartures);
        // 查询超时未退房订单（取前10条）
        List<Order> overdueCheckouts = orderMapper.findOverdueCheckouts(1, 10);
        reminders.put("overdueCount", overdueCheckouts.size());
        reminders.put("overdueCheckouts", overdueCheckouts);
        // 查询押金不足订单
        List<Order> lowDepositOrders = orderMapper.findLowDepositOrders();
        reminders.put("lowDepositCount", lowDepositOrders.size());
        reminders.put("lowDepositOrders", lowDepositOrders);
        // 查询待处理预约数量
        int pendingCount = orderMapper.pendingCount();
        reminders.put("pendingCount", pendingCount);
        return Result.success(reminders);
    }

    /**
     * 获取今日生日客户列表
     */
    @Override
    public Result<List<Customer>> getBirthdayCustomers() {
        List<Customer> list = customerMapper.findBirthdayCustomers(LocalDate.now());
        return Result.success(list);
    }

    /**
     * 获取今日预计入住订单
     */
    @Override
    public Result<List<Order>> getTodayArrivals() {
        List<Order> list = orderMapper.findTodayArrivals();
        return Result.success(list);
    }

    /**
     * 获取今日预计退房订单
     */
    @Override
    public Result<List<Order>> getTodayDepartures() {
        List<Order> list = orderMapper.findTodayDepartures();
        return Result.success(list);
    }

    /**
     * 获取超时未退房订单列表
     */
    @Override
    public Result<List<Order>> getOverdueCheckouts() {
        List<Order> list = orderMapper.findOverdueCheckouts(1, 100);
        return Result.success(list);
    }

    /**
     * 获取押金不足订单列表
     */
    @Override
    public Result<List<Order>> getLowDepositOrders() {
        List<Order> list = orderMapper.findLowDepositOrders();
        return Result.success(list);
    }
}
