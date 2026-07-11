package com.jchotel.service;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.MaintenanceOrder;
import com.jchotel.utils.Result;

import java.math.BigDecimal;

public interface MaintenanceOrderService {
    Result<PageResult<MaintenanceOrder>> list(PageQuery query);
    Result<MaintenanceOrder> detail(Long id);
    Result create(MaintenanceOrder order, Long reporterId, String reporterName);
    Result assign(Long id, Long assigneeId, String assigneeName);
    Result startProcessing(Long id);
    Result finish(Long id, String solution, BigDecimal cost);
    Result verify(Long id);
    Result cancel(Long id);
    int countByStatus(String status);
}
