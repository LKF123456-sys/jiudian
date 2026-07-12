package com.jchotel.controller;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.MaintenanceOrder;
import com.jchotel.entity.User;
import com.jchotel.mapper.UserMapper;
import com.jchotel.service.MaintenanceOrderService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
@Tag(name = "维修管理")
public class MaintenanceOrderController {

    @Autowired
    private MaintenanceOrderService maintenanceOrderService;

    @Autowired
    private UserMapper userMapper;

    @Operation(summary = "分页查询维修单列表")
    @GetMapping
    public Result<PageResult<MaintenanceOrder>> list(PageQuery query) {
        return maintenanceOrderService.list(query);
    }

    @Operation(summary = "查询维修单详情")
    @GetMapping("/{id}")
    public Result<MaintenanceOrder> detail(@PathVariable Long id) {
        return maintenanceOrderService.detail(id);
    }

    @Operation(summary = "创建维修单")
    @PostMapping
    public Result create(@RequestBody MaintenanceOrder order, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.selectById(userId);
        String reporterName = user != null ? user.getRealName() : null;
        return maintenanceOrderService.create(order, userId, reporterName);
    }

    @Operation(summary = "分配维修单")
    @PostMapping("/{id}/assign")
    public Result assign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long assigneeId = Long.valueOf(body.get("assigneeId").toString());
        String assigneeName = (String) body.get("assigneeName");
        return maintenanceOrderService.assign(id, assigneeId, assigneeName);
    }

    @Operation(summary = "开始维修")
    @PostMapping("/{id}/start")
    public Result startProcessing(@PathVariable Long id) {
        return maintenanceOrderService.startProcessing(id);
    }

    @Operation(summary = "完成维修")
    @PostMapping("/{id}/finish")
    public Result finish(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String solution = (String) body.get("solution");
        BigDecimal cost = body.get("cost") != null ? new BigDecimal(body.get("cost").toString()) : null;
        return maintenanceOrderService.finish(id, solution, cost);
    }

    @Operation(summary = "验收维修")
    @PostMapping("/{id}/verify")
    public Result verify(@PathVariable Long id) {
        return maintenanceOrderService.verify(id);
    }

    @Operation(summary = "取消维修单")
    @PostMapping("/{id}/cancel")
    public Result cancel(@PathVariable Long id) {
        return maintenanceOrderService.cancel(id);
    }
}
