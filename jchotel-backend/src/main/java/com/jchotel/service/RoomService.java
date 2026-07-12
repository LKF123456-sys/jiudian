package com.jchotel.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.Room;
import com.jchotel.utils.Result;

import java.util.List;
import java.util.Map;

public interface RoomService extends IService<Room> {
    Result<PageResult<Room>> list(PageQuery query);
    Result<Room> detail(Long id);
    Result<String> add(Room room);
    Result<String> update(Room room);
    Result<String> delete(Long id);
    Result<String> updateStatus(Long id, String status);
    Result<List<Room>> board();
    Result<Map<String, Object>> statusStats();
    Result<List<Room>> available(Long typeId, String checkIn, String checkOut);
}
