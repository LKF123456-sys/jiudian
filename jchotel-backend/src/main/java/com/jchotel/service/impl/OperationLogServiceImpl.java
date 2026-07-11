package com.jchotel.service.impl;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.OperationLog;
import com.jchotel.mapper.OperationLogMapper;
import com.jchotel.service.OperationLogService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationLogServiceImpl implements OperationLogService {

    @Autowired
    private OperationLogMapper operationLogMapper;

    private void initPage(PageQuery query) {
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1);
        }
        if (query.getSize() == null || query.getSize() < 1) {
            query.setSize(10);
        }
        query.setOffset((query.getPage() - 1) * query.getSize());
    }

    @Override
    public Result<PageResult<OperationLog>> list(PageQuery query) {
        initPage(query);
        Long total = operationLogMapper.count(query);
        List<OperationLog> list = operationLogMapper.findList(query);
        PageResult<OperationLog> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }
}
