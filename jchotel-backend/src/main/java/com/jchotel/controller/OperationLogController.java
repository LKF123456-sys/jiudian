package com.jchotel.controller;

import com.jchotel.annotation.RequireRole;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.OperationLog;
import com.jchotel.service.OperationLogService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/operation-logs")
@Tag(name = "操作日志")
@RequireRole({"admin", "manager"})
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @Operation(summary = "分页查询操作日志列表")
    @GetMapping
    public Result<PageResult<OperationLog>> list(PageQuery query) {
        return operationLogService.list(query);
    }
}
