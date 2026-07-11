package com.jchotel.service;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.OperationLog;
import com.jchotel.utils.Result;

public interface OperationLogService {
    Result<PageResult<OperationLog>> list(PageQuery query);
}
