package com.jchotel.service.impl;

// Lambda条件构造器
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
// MyBatis-Plus通用服务实现基类
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// 房间状态常量
import com.jchotel.constants.RoomStatus;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 分页结果封装
import com.jchotel.dto.PageResult;
// 房间实体类
import com.jchotel.entity.Room;
// 房间数据访问Mapper
import com.jchotel.mapper.RoomMapper;
// 房间服务接口
import com.jchotel.service.RoomService;
// 统一响应结果封装
import com.jchotel.utils.Result;
// Spring服务层注解
import org.springframework.stereotype.Service;

// HashMap集合
import java.util.HashMap;
// List集合
import java.util.List;
// Map接口
import java.util.Map;

/**
 * 房间管理服务实现类
 * 实现客房CRUD、状态管理、房态看板、可用房查询、房态统计等功能
 * 入住中的房间禁止删除、禁止直接修改状态
 */
@Service // 标记为Spring服务组件
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements RoomService {

    /**
     * 分页查询房间列表
     */
    @Override
    public Result<PageResult<Room>> list(PageQuery query) {
        // 页码默认1
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1);
        }
        // 页大小默认10
        if (query.getSize() == null || query.getSize() < 1) {
            query.setSize(10);
        }
        // 计算SQL偏移量
        query.setOffset((query.getPage() - 1) * query.getSize());

        // 查询总记录数
        Long total = baseMapper.count(query);
        // 查询分页数据（关联房型信息）
        List<Room> list = baseMapper.findList(query);

        // 封装分页结果
        PageResult<Room> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }

    /**
     * 查询房间详情（关联房型信息）
     */
    @Override
    public Result<Room> detail(Long id) {
        Room room = baseMapper.findDetailById(id);
        if (room == null) {
            return Result.error("客房不存在");
        }
        return Result.success(room);
    }

    /**
     * 新增房间
     * 校验房间号唯一，默认状态为空闲
     */
    @Override
    public Result<String> add(Room room) {
        // 校验房间号非空
        if (room.getRoomNo() == null || room.getRoomNo().trim().isEmpty()) {
            return Result.error("房间号不能为空");
        }
        // 校验房间号唯一
        if (baseMapper.findByRoomNo(room.getRoomNo()) != null) {
            return Result.error("房间号已存在");
        }
        // 默认状态为空闲
        if (room.getStatus() == null || room.getStatus().isEmpty()) {
            room.setStatus(RoomStatus.IDLE);
        }
        save(room);
        return Result.success("新增成功", null);
    }

    /**
     * 修改房间信息
     * 入住中的房间不能直接修改状态（需走退房流程）
     */
    @Override
    public Result<String> update(Room room) {
        // 校验房间号唯一（排除自身）
        Room exist = baseMapper.findByRoomNo(room.getRoomNo());
        if (exist != null && !exist.getId().equals(room.getId())) {
            return Result.error("房间号已存在");
        }
        Room oldRoom = getById(room.getId());
        if (oldRoom == null) {
            return Result.error("客房不存在");
        }
        // 入住中的房间禁止直接修改状态
        if (RoomStatus.OCCUPIED.equals(oldRoom.getStatus()) && room.getStatus() != null
                && !RoomStatus.OCCUPIED.equals(room.getStatus())) {
            return Result.error("入住中的房间不能直接修改状态，请先办理退房");
        }
        updateById(room);
        return Result.success("修改成功", null);
    }

    /**
     * 删除房间
     * 入住中的房间不能删除
     */
    @Override
    public Result<String> delete(Long id) {
        Room room = getById(id);
        if (room == null) {
            return Result.error("客房不存在");
        }
        if (RoomStatus.OCCUPIED.equals(room.getStatus())) {
            return Result.error("入住中的房间不能删除，请先办理退房");
        }
        removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 更新房间状态
     */
    @Override
    public Result<String> updateStatus(Long id, String status) {
        Room room = getById(id);
        if (room == null) {
            return Result.error("客房不存在");
        }
        if (RoomStatus.OCCUPIED.equals(room.getStatus())) {
            return Result.error("入住中的房间不能修改状态，请先办理退房");
        }
        if (RoomStatus.MAINTENANCE.equals(status) && RoomStatus.OCCUPIED.equals(room.getStatus())) {
            return Result.error("入住中的房间不能设为维修状态");
        }
        baseMapper.updateStatus(id, status);
        return Result.success("状态更新成功", null);
    }

    /**
     * 房态看板：查询所有房间带状态统计信息
     */
    @Override
    public Result<List<Room>> board() {
        List<Room> rooms = baseMapper.findRoomsWithStats();
        return Result.success(rooms);
    }

    /**
     * 房态统计：空闲、入住、维修、清洁、脏房数量及入住率
     */
    @Override
    public Result<Map<String, Object>> statusStats() {
        // 查询所有房间
        List<Room> rooms = list();
        Map<String, Object> stats = new HashMap<>();
        int total = rooms.size();
        // 初始化各状态计数
        int idle = 0, occupied = 0, maintenance = 0, cleaning = 0, dirty = 0;
        // 遍历统计各状态数量
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
        // 计算入住率（入住数/总数*100）
        stats.put("occupancyRate", total > 0 ? (double) occupied / total * 100 : 0);
        return Result.success(stats);
    }

    /**
     * 查询指定时间段可用的房间
     */
    @Override
    public Result<List<Room>> available(Long typeId, String checkIn, String checkOut) {
        List<Room> rooms = baseMapper.findAvailableRooms(typeId, checkIn, checkOut);
        return Result.success(rooms);
    }
}
