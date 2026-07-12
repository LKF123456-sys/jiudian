package com.jchotel.task; // 定义包名，task包存放定时任务类

// 项目自定义类
import com.jchotel.service.OrderService; // 订单服务接口
// 日志相关
import org.slf4j.Logger; // 日志接口
import org.slf4j.LoggerFactory; // 日志工厂
// Spring注解
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.scheduling.annotation.Scheduled; // 定时任务注解
import org.springframework.stereotype.Component; // Spring组件注解

/**
 * 订单超时处理定时任务
 * 定期检查并自动取消超时未入住的预约订单
 * 使用Spring @Scheduled注解实现定时执行
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
@Component // 标记为Spring组件，交由Spring容器管理
public class OrderTimeoutTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class); // 获取日志记录器

    @Autowired // Spring自动注入订单服务
    private OrderService orderService; // 订单服务实例，用于订单业务处理

    /**
     * 取消超时未入住的预约订单
     * 每10分钟执行一次，检查所有预约状态的订单
     * 如果超过预约入住时间仍未入住，则自动取消订单
     */
    @Scheduled(fixedRate = 600000) // 定时任务注解，fixedRate表示固定频率执行，单位毫秒，600000ms=10分钟
    public void cancelExpiredPendingOrders() {
        try { // 异常捕获，避免定时任务异常导致后续不再执行
            int count = orderService.cancelExpiredPendingOrders(); // 调用服务层方法取消超时订单，返回取消的订单数量
            if (count > 0) { // 只有实际取消了订单才记录日志
                log.info("自动取消超时未入住预约订单: {} 条", count); // 记录信息日志，输出取消的订单数量
            } // 结束count判断
        } catch (Exception e) { // 捕获所有异常
            log.error("自动取消超时订单任务异常", e); // 记录错误日志，打印异常堆栈
        } // 结束异常处理
    } // 结束cancelExpiredPendingOrders方法
} // 结束OrderTimeoutTask类
