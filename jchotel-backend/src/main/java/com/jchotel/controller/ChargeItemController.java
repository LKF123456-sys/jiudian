package com.jchotel.controller;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.ChargeItem;
import com.jchotel.service.ChargeItemService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charge-items")
@Tag(name = "消费品管理")
public class ChargeItemController {

    @Autowired
    private ChargeItemService chargeItemService;

    @Operation(summary = "分页查询消费品列表")
    @GetMapping
    public Result<PageResult<ChargeItem>> list(PageQuery query) {
        return chargeItemService.list(query);
    }

    @Operation(summary = "查询所有启用的消费品")
    @GetMapping("/all-enabled")
    public Result<List<ChargeItem>> listAllEnabled() {
        return chargeItemService.listAllEnabled();
    }

    @Operation(summary = "查询消费品详情")
    @GetMapping("/{id}")
    public Result<ChargeItem> detail(@PathVariable Long id) {
        return chargeItemService.detail(id);
    }

    @Operation(summary = "新增消费品")
    @PostMapping
    public Result add(@RequestBody ChargeItem item) {
        return chargeItemService.add(item);
    }

    @Operation(summary = "更新消费品")
    @PutMapping("/{id}")
    public Result update(@PathVariable Long id, @RequestBody ChargeItem item) {
        item.setId(id);
        return chargeItemService.update(item);
    }

    @Operation(summary = "删除消费品")
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return chargeItemService.delete(id);
    }
}
