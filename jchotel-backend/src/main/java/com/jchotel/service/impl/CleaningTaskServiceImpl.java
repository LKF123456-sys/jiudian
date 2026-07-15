package com.jchotel.service.impl;

// MyBatis-Plus通用服务实现基类，提供基础CRUD实现
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// 保洁任务状态常量类
import com.jchotel.constants.CleaningStatus;
// 房间状态常量类
import com.jchotel.constants.RoomStatus;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 分页结果封装类
import com.jchotel.dto.PageResult;
// 保洁任务实体类，对应数据库cleaning_task表
import com.jchotel.entity.CleaningTask;
// 保洁任务数据访问Mapper
import com.jchotel.mapper.CleaningTaskMapper;
// 房间数据访问Mapper，用于更新房间状态
import com.jchotel.mapper.RoomMapper;
// 保洁任务服务接口
import com.jchotel.service.CleaningTaskService;
// 统一响应结果封装类
import com.jchotel.utils.Result;
// Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// Spring服务层注解，标记为Spring管理的Bean
import org.springframework.stereotype.Service;
// 事务注解，保证数据库操作原子性
import org.springframework.transaction.annotation.Transactional;

// 日期时间类，记录完成时间、查房时间
import java.time.LocalDateTime;
// List集合，返回任务列表
import java.util.List;

/**
 * 保洁任务管理服务实现类
 * 实现客房清洁任务的全流程管理：退房自动创建任务、任务分配、开始清洁、完成清洁、查房验收、任务取消
 * 状态流转：待分配(PENDING) → 已分配(ASSIGNED) → 清洁中(CLEANING) → 待检查(INSPECTING) → 已完成(COMPLETED) / 已取消(CANCELLED)
 * 查房通过后自动将房间状态恢复为空闲(IDLE)可售
 */
@Service // 标记为Spring服务组件
public class CleaningTaskServiceImpl extends ServiceImpl<CleaningTaskMapper, CleaningTask> implements CleaningTaskService {

    @Autowired // 自动注入房间Mapper，用于更新房间状态（脏房→清洁中→空闲）
    private RoomMapper roomMapper;

    /**
     * 初始化分页参数默认值
     * 校验页码和页大小合法性，设置默认值，计算SQL查询OFFSET偏移量
     * @param query 分页查询参数对象
     */
    private void initPage(PageQuery query) {
        // 如果页码为空或小于1，默认第1页
        if (query.getPage() == null || query.getPage() < 1) query.setPage(1);
        // 如果页大小为空或小于1，默认每页10条
        if (query.getSize() == null || query.getSize() < 1) query.setSize(10);
        // 计算SQL查询偏移量：(页码-1)*页大小
        query.setOffset((query.getPage() - 1) * query.getSize());
    }

    /**
     * 分页查询保洁任务列表
     * 支持按任务状态、保洁人员、任务时间范围、房间号等条件筛选
     * @param query 分页查询参数
     * @return 分页保洁任务列表
     */
    @Override
    public Result<PageResult<CleaningTask>> list(PageQuery query) {
        // 初始化分页参数，设置默认值和偏移量
        initPage(query);
        // 查询符合条件的任务总记录数
        Long total = baseMapper.count(query);
        // 查询当前页任务列表数据
        List<CleaningTask> list = baseMapper.findList(query);
        // 构建分页结果对象
        PageResult<CleaningTask> pageResult = new PageResult<>();
        // 设置总记录数
        pageResult.setTotal(total);
        // 设置当前页数据列表
        pageResult.setList(list);
        // 返回成功结果
        return Result.success(pageResult);
    }

    /**
     * 查询保洁任务详情
     * @param id 任务ID
     * @return 任务详细信息
     */
    @Override
    public Result<CleaningTask> detail(Long id) {
        // 根据ID查询任务详情（关联查询房间号、保洁人员等信息）
        CleaningTask task = baseMapper.findDetailById(id);
        // 任务不存在返回错误
        if (task == null) return Result.error("清扫任务不存在");
        // 返回任务详情
        return Result.success(task);
    }

    /**
     * 退房后自动创建保洁任务
     * 退房结算时系统自动调用，将房间标记为脏房并生成清洁任务
     * @param roomId 房间ID
     * @param roomNo 房间号
     * @param orderId 关联订单ID
     * @param remark 备注（如客人遗留物品、特殊清洁要求）
     */
    @Override
    @Transactional // 开启事务保证任务创建和房间状态更新原子性
    public void createFromCheckout(Long roomId, String roomNo, Long orderId, String remark) {
        // 创建新的保洁任务对象
        CleaningTask task = new CleaningTask();
        // 设置关联房间ID
        task.setRoomId(roomId);
        // 设置房间号
        task.setRoomNo(roomNo);
        // 设置关联订单ID
        task.setOrderId(orderId);
        // 设置初始状态为待分配
        task.setStatus(CleaningStatus.PENDING);
        // 设置任务优先级为普通
        task.setPriority("normal");
        // 设置任务备注
        task.setRemark(remark);
        // 保存任务到数据库
        save(task);
        // 将房间状态更新为脏房（待清洁）
        roomMapper.updateStatus(roomId, RoomStatus.DIRTY);
    }

    /**
     * 分配保洁任务（派单）
     * 将待分配任务指定给保洁人员，状态更新为已分配
     * @param id 任务ID
     * @param assigneeId 保洁人员用户ID
     * @param assigneeName 保洁人员姓名
     * @return 派单结果
     */
    @Override
    @Transactional
    public Result assign(Long id, Long assigneeId, String assigneeName) {
        // 根据ID查询任务
        CleaningTask task = getById(id);
        // 任务不存在返回错误
        if (task == null) return Result.error("清扫任务不存在");
        // 只有待分配状态的任务才能派单
        if (!CleaningStatus.PENDING.equals(task.getStatus())) return Result.error("只有待分配的任务才能分配保洁员");
        // 设置保洁人员ID
        task.setAssigneeId(assigneeId);
        // 设置保洁人员姓名
        task.setAssigneeName(assigneeName);
        // 更新任务状态为已分配
        task.setStatus(CleaningStatus.ASSIGNED);
        // 更新任务到数据库
        updateById(task);
        // 返回派单成功
        return Result.success("分配成功", null);
    }

    /**
     * 开始清洁
     * 保洁人员到达房间后开始清洁，状态更新为清洁中，同时更新房间状态为清洁中
     * @param id 任务ID
     * @return 处理结果
     */
    @Override
    @Transactional
    public Result startCleaning(Long id) {
        // 查询任务信息
        CleaningTask task = getById(id);
        // 任务不存在返回错误
        if (task == null) return Result.error("清扫任务不存在");
        // 只有待分配或已分配状态的任务才能开始清洁
        if (!CleaningStatus.PENDING.equals(task.getStatus()) && !CleaningStatus.ASSIGNED.equals(task.getStatus()))
            return Result.error("只有待分配或已分配的任务才能开始清扫");
        // 更新任务状态为清洁中
        task.setStatus(CleaningStatus.CLEANING);
        // 更新任务到数据库
        updateById(task);
        // 将房间状态更新为清洁中
        roomMapper.updateStatus(task.getRoomId(), RoomStatus.CLEANING);
        // 返回开始清洁成功
        return Result.success("开始清扫", null);
    }

    /**
     * 完成清洁
     * 保洁人员清洁完毕提交，状态更新为待检查（等待管理人员查房）
     * @param id 任务ID
     * @return 提交结果
     */
    @Override
    @Transactional
    public Result finishCleaning(Long id) {
        // 查询任务信息
        CleaningTask task = getById(id);
        // 任务不存在返回错误
        if (task == null) return Result.error("清扫任务不存在");
        // 只有清洁中的任务才能标记完成
        if (!CleaningStatus.CLEANING.equals(task.getStatus())) return Result.error("只有清扫中的任务才能完成");
        // 更新任务状态为待检查
        task.setStatus(CleaningStatus.INSPECTING);
        // 记录清洁完成时间
        task.setFinishTime(LocalDateTime.now());
        // 更新任务到数据库
        updateById(task);
        // 返回清洁完成待查房
        return Result.success("清扫完成，待查房", null);
    }

    /**
     * 查房验收
     * 管理人员检查清洁质量合格后确认，状态更新为已完成，房间恢复空闲可售状态
     * @param id 任务ID
     * @return 验收结果
     */
    @Override
    @Transactional
    public Result inspect(Long id) {
        // 查询任务信息
        CleaningTask task = getById(id);
        // 任务不存在返回错误
        if (task == null) return Result.error("清扫任务不存在");
        // 只有待检查状态的任务才能查房
        if (!CleaningStatus.INSPECTING.equals(task.getStatus())) return Result.error("只有待查房的任务才能查房");
        // 更新任务状态为已完成
        task.setStatus(CleaningStatus.COMPLETED);
        // 记录查房验收时间
        task.setInspectTime(LocalDateTime.now());
        // 更新任务到数据库
        updateById(task);
        // 将房间状态恢复为空闲（可售）
        roomMapper.updateStatus(task.getRoomId(), RoomStatus.IDLE);
        // 返回查房通过结果
        return Result.success("查房通过，房间已就绪", null);
    }

    /**
     * 取消保洁任务
     * 仅未完成的任务可以取消
     * @param id 任务ID
     * @return 取消结果
     */
    @Override
    @Transactional
    public Result cancel(Long id) {
        // 查询任务信息
        CleaningTask task = getById(id);
        // 任务不存在返回错误
        if (task == null) return Result.error("清扫任务不存在");
        // 已完成或已取消的任务不能再次取消
        if (CleaningStatus.COMPLETED.equals(task.getStatus()) || CleaningStatus.CANCELLED.equals(task.getStatus()))
            return Result.error("该任务无法取消");
        // 更新任务状态为已取消
        task.setStatus(CleaningStatus.CANCELLED);
        // 更新任务到数据库
        updateById(task);
        // 返回取消成功
        return Result.success("取消成功", null);
    }

    /**
     * 按状态统计保洁任务数量
     * @param status 任务状态
     * @return 该状态的任务数量
     */
    @Override
    public int countByStatus(String status) {
        // 调用Mapper统计指定状态的任务数量
        return baseMapper.countByStatus(status);
    }

    /**
     * 查询待分配和已分配的保洁任务列表
     * 用于保洁人员工作台查看待处理任务
     * @return 待处理保洁任务列表
     */
    @Override
    public Result<List<CleaningTask>> findPendingAndAssigned() {
        // 查询待分配和已分配状态的任务列表
        List<CleaningTask> list = baseMapper.findPendingAndAssigned();
        // 返回查询结果
        return Result.success(list);
    }
}
