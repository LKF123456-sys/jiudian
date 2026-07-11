package com.jchotel.controller;

import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.Room;
import com.jchotel.mapper.OrderMapper;
import com.jchotel.service.RoomService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private RoomService roomService;

    @Autowired
    private OrderMapper orderMapper;

    @GetMapping
    public Result<PageResult<Room>> list(PageQuery query) {
        return roomService.list(query);
    }

    @GetMapping("/board")
    public Result<List<Room>> board() {
        return roomService.board();
    }

    @GetMapping("/status-stats")
    public Result<Map<String, Object>> statusStats() {
        return roomService.statusStats();
    }

    @GetMapping("/available")
    public Result<List<Room>> availableRooms(
            @RequestParam(required = false) Long typeId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkInTime,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime expectedCheckOutTime) {
        String checkIn = checkInTime.format(FMT);
        String checkOut = expectedCheckOutTime.format(FMT);
        return roomService.available(typeId, checkIn, checkOut);
    }

    @GetMapping("/{id}")
    public Result<Room> detail(@PathVariable Long id) {
        return roomService.detail(id);
    }

    @PostMapping
    public Result<String> add(@RequestBody Room room) {
        return roomService.add(room);
    }

    @PutMapping("/{id}")
    public Result<String> update(@PathVariable Long id, @RequestBody Room room) {
        room.setId(id);
        return roomService.update(room);
    }

    @DeleteMapping("/{id}")
    public Result<String> delete(@PathVariable Long id) {
        return roomService.delete(id);
    }

    @PutMapping("/{id}/status")
    public Result<String> updateStatus(@PathVariable Long id, @RequestBody Room room) {
        return roomService.updateStatus(id, room.getStatus());
    }
}
