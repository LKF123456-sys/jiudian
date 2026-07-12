package com.jchotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.CleaningTask;
import com.jchotel.utils.Result;

import java.util.List;

public interface CleaningTaskService extends IService<CleaningTask> {
    Result<PageResult<CleaningTask>> list(PageQuery query);
    Result<CleaningTask> detail(Long id);
    void createFromCheckout(Long roomId, String roomNo, Long orderId, String remark);
    Result assign(Long id, Long assigneeId, String assigneeName);
    Result startCleaning(Long id);
    Result finishCleaning(Long id);
    Result inspect(Long id);
    Result cancel(Long id);
    int countByStatus(String status);
    Result<List<CleaningTask>> findPendingAndAssigned();
}
