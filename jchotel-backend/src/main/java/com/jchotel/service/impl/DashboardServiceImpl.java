package com.jchotel.service.impl;

// 订单实体类
import com.jchotel.entity.Order;
// 订单数据访问Mapper
import com.jchotel.mapper.OrderMapper;
// 保洁任务服务
import com.jchotel.service.CleaningTaskService;
// 仪表盘数据服务接口
import com.jchotel.service.DashboardService;
// 维修工单服务
import com.jchotel.service.MaintenanceOrderService;
// 提醒服务
import com.jchotel.service.ReminderService;
// 房间服务
import com.jchotel.service.RoomService;
// 统一响应结果封装
import com.jchotel.utils.Result;
// Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// Spring服务层注解
import org.springframework.stereotype.Service;

// HashMap集合
import java.util.HashMap;
// List集合
import java.util.List;
// Map接口
import java.util.Map;

/**
 * 仪表盘数据聚合服务实现类
 * 首页展示：今日入住/退房/待处理/超时离店数量，今日营收，房态统计，待处理保洁/维修数量，各类提醒事项
 */
@Service // 标记为Spring服务组件
public class DashboardServiceImpl implements DashboardService {

    @Autowired // 自动注入订单Mapper，查询订单统计数据
    private OrderMapper orderMapper;

    @Autowired // 自动注入提醒服务，获取各类提醒
    private ReminderService reminderService;

    @Autowired // 自动注入房间服务，获取房态统计
    private RoomService roomService;

    @Autowired // 自动注入保洁任务服务，获取待处理保洁数量
    private CleaningTaskService cleaningTaskService;

    @Autowired // 自动注入维修工单服务，获取待处理维修数量
    private MaintenanceOrderService maintenanceOrderService;

    /**
     * 获取首页仪表盘所有聚合数据
     * @return 包含今日入住数、当前入住数、待处理预约数、空闲房间数、总房间数、
     *         超时离店数及列表、今日营收、房态统计、保洁/维修待处理数、各类提醒的Map
     */
    @Override
    public Result<Map<String, Object>> getDashboardData() {
        // 今日预计入住数
        int todayCheckIn = orderMapper.todayCheckInCount();
        // 当前在住数量
        int currentCheckIn = orderMapper.currentCheckInCount();
        // 待处理预约数
        int pendingCount = orderMapper.pendingCount();
        // 总房间数
        int totalRooms = orderMapper.totalRoomCount();
        // 空闲房间数
        int idleRoom = orderMapper.idleRoomCount();
        // 超时未退房数量
        int overdueCount = orderMapper.overdueCheckoutCount();

        // 如果有超时离店，取前5条列表
        List<Order> overdueList = null;
        if (overdueCount > 0) {
            overdueList = orderMapper.findOverdueCheckouts(1, 5);
        }

        // 获取房态统计
        Map<String, Object> roomStats = roomService.statusStats().getData();
        // 获取提醒事项
        Result<Map<String, Object>> remindersResult = reminderService.getDashboardReminders();

        // 聚合所有数据
        Map<String, Object> data = new HashMap<>();
        data.put("todayCheckIn", todayCheckIn);
        data.put("currentCheckIn", currentCheckIn);
        data.put("pendingCount", pendingCount);
        data.put("idleRoom", idleRoom);
        data.put("totalRooms", totalRooms);
        data.put("overdueCheckoutCount", overdueCount);
        data.put("overdueList", overdueList);
        // 今日营收
        data.put("todayRevenue", orderMapper.todayRevenue());
        data.put("roomStats", roomStats);
        // 保洁待分配/已分配数量
        data.put("cleaningPendingCount", cleaningTaskService.countByStatus("pending"));
        data.put("cleaningAssignedCount", cleaningTaskService.countByStatus("assigned"));
        // 维修待处理数量
        data.put("maintenancePendingCount", maintenanceOrderService.countByStatus("pending"));
        data.put("reminders", remindersResult.getData());
        return Result.success(data);
    }
}
