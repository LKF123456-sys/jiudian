package com.jchotel.controller;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.MaintenanceOrder;
import com.jchotel.entity.User;
import com.jchotel.mapper.UserMapper;
import com.jchotel.service.MaintenanceOrderService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceOrderController {

    @Autowired
    private MaintenanceOrderService maintenanceOrderService;

    @Autowired
    private UserMapper userMapper;

    @GetMapping
    public Result<PageResult<MaintenanceOrder>> list(PageQuery query) {
        return maintenanceOrderService.list(query);
    }

    @GetMapping("/{id}")
    public Result<MaintenanceOrder> detail(@PathVariable Long id) {
        return maintenanceOrderService.detail(id);
    }

    @PostMapping
    public Result create(@RequestBody MaintenanceOrder order, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        User user = userMapper.findById(userId);
        String reporterName = user != null ? user.getRealName() : null;
        return maintenanceOrderService.create(order, userId, reporterName);
    }

    @PostMapping("/{id}/assign")
    public Result assign(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long assigneeId = Long.valueOf(body.get("assigneeId").toString());
        String assigneeName = (String) body.get("assigneeName");
        return maintenanceOrderService.assign(id, assigneeId, assigneeName);
    }

    @PostMapping("/{id}/start")
    public Result startProcessing(@PathVariable Long id) {
        return maintenanceOrderService.startProcessing(id);
    }

    @PostMapping("/{id}/finish")
    public Result finish(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String solution = (String) body.get("solution");
        BigDecimal cost = body.get("cost") != null ? new BigDecimal(body.get("cost").toString()) : null;
        return maintenanceOrderService.finish(id, solution, cost);
    }

    @PostMapping("/{id}/verify")
    public Result verify(@PathVariable Long id) {
        return maintenanceOrderService.verify(id);
    }

    @PostMapping("/{id}/cancel")
    public Result cancel(@PathVariable Long id) {
        return maintenanceOrderService.cancel(id);
    }
}
