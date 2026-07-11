package com.jchotel.controller;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.CleaningTask;
import com.jchotel.service.CleaningTaskService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cleaning-tasks")
public class CleaningTaskController {

    @Autowired
    private CleaningTaskService cleaningTaskService;

    @GetMapping
    public Result<PageResult<CleaningTask>> list(PageQuery query) {
        return cleaningTaskService.list(query);
    }

    @GetMapping("/{id}")
    public Result<CleaningTask> detail(@PathVariable Long id) {
        return cleaningTaskService.detail(id);
    }

    @GetMapping("/pending")
    public Result<List<CleaningTask>> findPendingAndAssigned() {
        return cleaningTaskService.findPendingAndAssigned();
    }

    @PostMapping("/{id}/assign")
    public Result assign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long assigneeId = Long.valueOf(body.get("assigneeId").toString());
        String assigneeName = (String) body.get("assigneeName");
        return cleaningTaskService.assign(id, assigneeId, assigneeName);
    }

    @PostMapping("/{id}/start")
    public Result startCleaning(@PathVariable Long id) {
        return cleaningTaskService.startCleaning(id);
    }

    @PostMapping("/{id}/finish")
    public Result finishCleaning(@PathVariable Long id) {
        return cleaningTaskService.finishCleaning(id);
    }

    @PostMapping("/{id}/inspect")
    public Result inspect(@PathVariable Long id) {
        return cleaningTaskService.inspect(id);
    }

    @PostMapping("/{id}/cancel")
    public Result cancel(@PathVariable Long id) {
        return cleaningTaskService.cancel(id);
    }
}
