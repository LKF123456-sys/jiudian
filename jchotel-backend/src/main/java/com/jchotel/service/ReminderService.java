package com.jchotel.service;

// 客户实体类
import com.jchotel.entity.Customer;
// 订单实体类
import com.jchotel.entity.Order;
// 统一响应结果封装类
import com.jchotel.utils.Result;

// List集合，返回提醒列表
import java.util.List;
// Map集合，返回汇总提醒数据
import java.util.Map;

/**
 * 提醒事项服务接口
 * 负责酒店运营中的各类待办事项和预警提醒聚合，为首页工作台和提醒中心提供数据
 * 包括生日提醒、今日到店、今日离店、逾期未退房、押金不足预警等
 */
public interface ReminderService {

    /**
     * 获取首页仪表盘提醒汇总数据
     * 聚合各类提醒的数量概览，用于首页红点提示和数字角标
     * @return 提醒汇总数据Map，包含各类提醒的数量统计
     */
    Result<Map<String, Object>> getDashboardReminders();

    /**
     * 获取生日客户列表
     * 查询当天或近期过生日的住客，用于客户关怀
     * @return 生日客户列表
     */
    Result<List<Customer>> getBirthdayCustomers();

    /**
     * 获取今日预计到店订单列表
     * @return 今日应办理入住的订单列表
     */
    Result<List<Order>> getTodayArrivals();

    /**
     * 获取今日预计离店订单列表
     * @return 今日应办理退房的订单列表
     */
    Result<List<Order>> getTodayDepartures();

    /**
     * 获取逾期未退房订单列表
     * 已过退房时间但仍未办理退房的订单，用于前台催退
     * @return 逾期未退房的订单列表
     */
    Result<List<Order>> getOverdueCheckouts();

    /**
     * 获取押金不足预警订单列表
     * 押金余额低于阈值（如消费已超过押金）的在住订单，提醒催缴
     * @return 押金不足的订单列表
     */
    Result<List<Order>> getLowDepositOrders();
}
