package com.jchotel.controller;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.CleaningTask;
import com.jchotel.service.CleaningTaskService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cleaning-tasks")
@Tag(name = "清扫管理")
public class CleaningTaskController {

    @Autowired
    private CleaningTaskService cleaningTaskService;

    @Operation(summary = "分页查询清扫任务列表")
    @GetMapping
    public Result<PageResult<CleaningTask>> list(PageQuery query) {
        return cleaningTaskService.list(query);
    }

    @Operation(summary = "查询清扫任务详情")
    @GetMapping("/{id}")
    public Result<CleaningTask> detail(@PathVariable Long id) {
        return cleaningTaskService.detail(id);
    }

    @Operation(summary = "查询待处理和已分配的清扫任务")
    @GetMapping("/pending")
    public Result<List<CleaningTask>> findPendingAndAssigned() {
        return cleaningTaskService.findPendingAndAssigned();
    }

    @Operation(summary = "分配清扫任务")
    @PostMapping("/{id}/assign")
    public Result assign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long assigneeId = Long.valueOf(body.get("assigneeId").toString());
        String assigneeName = (String) body.get("assigneeName");
        return cleaningTaskService.assign(id, assigneeId, assigneeName);
    }

    @Operation(summary = "开始清扫")
    @PostMapping("/{id}/start")
    public Result startCleaning(@PathVariable Long id) {
        return cleaningTaskService.startCleaning(id);
    }

    @Operation(summary = "完成清扫")
    @PostMapping("/{id}/finish")
    public Result finishCleaning(@PathVariable Long id) {
        return cleaningTaskService.finishCleaning(id);
    }

    @Operation(summary = "检查清扫")
    @PostMapping("/{id}/inspect")
    public Result inspect(@PathVariable Long id) {
        return cleaningTaskService.inspect(id);
    }

    @Operation(summary = "取消清扫任务")
    @PostMapping("/{id}/cancel")
    public Result cancel(@PathVariable Long id) {
        return cleaningTaskService.cancel(id);
    }
}
