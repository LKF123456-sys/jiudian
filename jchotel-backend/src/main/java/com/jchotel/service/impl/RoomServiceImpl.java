package com.jchotel.service.impl;

import com.jchotel.constants.RoomStatus;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.Room;
import com.jchotel.mapper.RoomMapper;
import com.jchotel.service.RoomService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RoomServiceImpl implements RoomService {

    @Autowired
    private RoomMapper roomMapper;

    @Override
    public Result<PageResult<Room>> list(PageQuery query) {
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1);
        }
        if (query.getSize() == null || query.getSize() < 1) {
            query.setSize(10);
        }
        query.setOffset((query.getPage() - 1) * query.getSize());

        Long total = roomMapper.count(query);
        List<Room> list = roomMapper.findList(query);

        PageResult<Room> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }

    @Override
    public Result<Room> detail(Long id) {
        Room room = roomMapper.findById(id);
        if (room == null) {
            return Result.error("客房不存在");
        }
        return Result.success(room);
    }

    @Override
    public Result<String> add(Room room) {
        if (room.getRoomNo() == null || room.getRoomNo().trim().isEmpty()) {
            return Result.error("房间号不能为空");
        }
        if (roomMapper.findByRoomNo(room.getRoomNo()) != null) {
            return Result.error("房间号已存在");
        }
        if (room.getStatus() == null || room.getStatus().isEmpty()) {
            room.setStatus(RoomStatus.IDLE);
        }
        roomMapper.insert(room);
        return Result.success("新增成功", null);
    }

    @Override
    public Result<String> update(Room room) {
        Room exist = roomMapper.findByRoomNo(room.getRoomNo());
        if (exist != null && !exist.getId().equals(room.getId())) {
            return Result.error("房间号已存在");
        }
        Room oldRoom = roomMapper.findById(room.getId());
        if (oldRoom == null) {
            return Result.error("客房不存在");
        }
        if (RoomStatus.OCCUPIED.equals(oldRoom.getStatus()) && room.getStatus() != null 
                && !RoomStatus.OCCUPIED.equals(room.getStatus())) {
            return Result.error("入住中的房间不能直接修改状态，请先办理退房");
        }
        roomMapper.update(room);
        return Result.success("修改成功", null);
    }

    @Override
    public Result<String> delete(Long id) {
        Room room = roomMapper.findById(id);
        if (room == null) {
            return Result.error("客房不存在");
        }
        if (RoomStatus.OCCUPIED.equals(room.getStatus())) {
            return Result.error("入住中的房间不能删除，请先办理退房");
        }
        roomMapper.deleteById(id);
        return Result.success("删除成功", null);
    }

    @Override
    public Result<String> updateStatus(Long id, String status) {
        Room room = roomMapper.findById(id);
        if (room == null) {
            return Result.error("客房不存在");
        }
        if (RoomStatus.OCCUPIED.equals(room.getStatus())) {
            return Result.error("入住中的房间不能修改状态，请先办理退房");
        }
        if (RoomStatus.MAINTENANCE.equals(status) && RoomStatus.OCCUPIED.equals(room.getStatus())) {
            return Result.error("入住中的房间不能设为维修状态");
        }
        roomMapper.updateStatus(id, status);
        return Result.success("状态更新成功", null);
    }

    @Override
    public Result<List<Room>> board() {
        List<Room> rooms = roomMapper.findRoomsWithStats();
        return Result.success(rooms);
    }

    @Override
    public Result<Map<String, Object>> statusStats() {
        List<Room> rooms = roomMapper.findAll();
        Map<String, Object> stats = new HashMap<>();
        int total = rooms.size();
        int idle = 0, occupied = 0, maintenance = 0, cleaning = 0, dirty = 0;
        for (Room r : rooms) {
            String status = r.getStatus();
            if (RoomStatus.IDLE.equals(status)) idle++;
            else if (RoomStatus.OCCUPIED.equals(status)) occupied++;
            else if (RoomStatus.MAINTENANCE.equals(status)) maintenance++;
            else if (RoomStatus.CLEANING.equals(status)) cleaning++;
            else if (RoomStatus.DIRTY.equals(status)) dirty++;
        }
        stats.put("total", total);
        stats.put("idle", idle);
        stats.put("occupied", occupied);
        stats.put("maintenance", maintenance);
        stats.put("cleaning", cleaning);
        stats.put("dirty", dirty);
        stats.put("occupancyRate", total > 0 ? (double) occupied / total * 100 : 0);
        return Result.success(stats);
    }

    @Override
    public Result<List<Room>> available(Long typeId, String checkIn, String checkOut) {
        List<Room> rooms = roomMapper.findAvailableRooms(typeId, checkIn, checkOut);
        return Result.success(rooms);
    }
}
