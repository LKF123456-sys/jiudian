package com.jchotel.task;

import com.jchotel.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时处理定时任务
 * <p>
 * 定期检查并自动取消超时未入住的预约订单，
 * 使用Spring @Scheduled注解实现定时执行，防止用户预约后未按时入住导致房间被长期占用。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
@Component
public class OrderTimeoutTask {

    /**
     * 日志记录器
     */
    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);

    /**
     * 订单服务实例
     */
    @Autowired
    private OrderService orderService;

    /**
     * 定时取消超时未入住的预约订单
     * <p>
     * 每10分钟执行一次，检查所有处于"已预约"状态的订单，
     * 如果当前时间超过预约入住时间仍未办理入住，则自动取消订单并释放房间。
     * </p>
     */
    @Scheduled(fixedRate = 600000)
    public void cancelExpiredPendingOrders() {
        try {
            int count = orderService.cancelExpiredPendingOrders();
            if (count > 0) {
                log.info("自动取消超时未入住预约订单: {} 条", count);
            }
        } catch (Exception e) {
            log.error("自动取消超时订单任务异常", e);
        }
    }
}
