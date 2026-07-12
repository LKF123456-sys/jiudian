package com.jchotel.service;

// MyBatis-Plus通用服务接口，提供基础CRUD能力
import com.baomidou.mybatisplus.extension.service.IService;
// 分页查询参数对象
import com.jchotel.dto.PageQuery;
// 分页结果对象
import com.jchotel.dto.PageResult;
// 房间实体类，对应数据库room表
import com.jchotel.entity.Room;
// 统一响应结果封装类
import com.jchotel.utils.Result;

// List集合，返回房间列表
import java.util.List;
// Map集合，返回统计数据
import java.util.Map;

/**
 * 房间管理服务接口
 * 负责酒店客房的基础信息维护、状态管理、房态盘展示、房间状态统计、可用房查询等功能
 * 房间状态包括：空房、入住中、待清洁、维修中、停用等
 */
public interface RoomService extends IService<Room> {

    /**
     * 分页查询房间列表
     * 支持按房间号、房型、楼层、状态等条件筛选
     * @param query 分页查询参数
     * @return 分页房间列表
     */
    Result<PageResult<Room>> list(PageQuery query);

    /**
     * 查询房间详情
     * @param id 房间ID
     * @return 房间详细信息，包含房型信息、当前入住订单等
     */
    Result<Room> detail(Long id);

    /**
     * 新增房间
     * @param room 房间信息，包含房间号、房型ID、楼层、位置等
     * @return 新增结果提示
     */
    Result<String> add(Room room);

    /**
     * 更新房间信息
     * @param room 需要更新的房间信息，必须包含房间ID
     * @return 更新结果提示
     */
    Result<String> update(Room room);

    /**
     * 删除房间
     * 需检查房间当前状态，有入住或维修中的房间不允许删除
     * @param id 待删除的房间ID
     * @return 删除结果提示
     */
    Result<String> delete(Long id);

    /**
     * 更新房间状态
     * 用于手动调整房间状态（如设置维修、停用等）
     * @param id 房间ID
     * @param status 目标状态值
     * @return 更新结果提示
     */
    Result<String> updateStatus(Long id, String status);

    /**
     * 查询房态盘数据
     * 返回所有房间的状态概览，用于前台房态图展示
     * @return 房间列表，包含每个房间的实时状态
     */
    Result<List<Room>> board();

    /**
     * 房间状态统计
     * 统计各种状态的房间数量（空房数、入住数、待清洁数、维修数等）
     * @return 状态统计Map，key为状态，value为数量
     */
    Result<Map<String, Object>> statusStats();

    /**
     * 查询指定时间范围内可用的房间
     * 用于预订或入住时选择房间，排除该时间段内已被预订/入住的房间
     * @param typeId 房型ID（可选，不指定则查询所有房型）
     * @param checkIn 预计入住时间字符串
     * @param checkOut 预计退房时间字符串
     * @return 可用房间列表
     */
    Result<List<Room>> available(Long typeId, String checkIn, String checkOut);
}
