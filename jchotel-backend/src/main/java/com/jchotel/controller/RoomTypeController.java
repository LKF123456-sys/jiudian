package com.jchotel.controller;

import com.jchotel.entity.RoomType;
import com.jchotel.service.RoomTypeService;
import com.jchotel.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
@Tag(name = "房型管理")
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    @Operation(summary = "查询所有房型列表")
    @GetMapping
    public Result<List<RoomType>> list() {
        return roomTypeService.listAll();
    }

    @Operation(summary = "新增房型")
    @PostMapping
    public Result<String> add(@RequestBody RoomType roomType) {
        return roomTypeService.add(roomType);
    }

    @Operation(summary = "更新房型")
    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody RoomType roomType) {
        roomType.setId(id);
        return roomTypeService.update(roomType);
    }

    @Operation(summary = "删除房型")
    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return roomTypeService.delete(id);
    }
}
