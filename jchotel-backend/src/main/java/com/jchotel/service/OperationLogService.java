package com.jchotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.OperationLog;
import com.jchotel.utils.Result;

public interface OperationLogService extends IService<OperationLog> {
    Result<PageResult<OperationLog>> list(PageQuery query);
}
