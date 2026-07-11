package com.jchotel.task;

import com.jchotel.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderTimeoutTask {

    private static final Logger log = LoggerFactory.getLogger(OrderTimeoutTask.class);

    @Autowired
    private OrderService orderService;

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
