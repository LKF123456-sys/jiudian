package com.jchotel.controller;

import com.jchotel.entity.RoomType;
import com.jchotel.service.RoomTypeService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    @GetMapping
    public Result<List<RoomType>> list() {
        return roomTypeService.listAll();
    }

    @PostMapping
    public Result<String> add(@RequestBody RoomType roomType) {
        return roomTypeService.add(roomType);
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody RoomType roomType) {
        roomType.setId(id);
        return roomTypeService.update(roomType);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return roomTypeService.delete(id);
    }
}
