package com.jchotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jchotel.constants.CleaningStatus;
import com.jchotel.constants.RoomStatus;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.CleaningTask;
import com.jchotel.mapper.CleaningTaskMapper;
import com.jchotel.mapper.RoomMapper;
import com.jchotel.service.CleaningTaskService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CleaningTaskServiceImpl extends ServiceImpl<CleaningTaskMapper, CleaningTask> implements CleaningTaskService {

    @Autowired
    private RoomMapper roomMapper;

    private void initPage(PageQuery query) {
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1);
        }
        if (query.getSize() == null || query.getSize() < 1) {
            query.setSize(10);
        }
        query.setOffset((query.getPage() - 1) * query.getSize());
    }

    @Override
    public Result<PageResult<CleaningTask>> list(PageQuery query) {
        initPage(query);
        Long total = baseMapper.count(query);
        List<CleaningTask> list = baseMapper.findList(query);
        PageResult<CleaningTask> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }

    @Override
    public Result<CleaningTask> detail(Long id) {
        CleaningTask task = baseMapper.findDetailById(id);
        if (task == null) {
            return Result.error("清扫任务不存在");
        }
        return Result.success(task);
    }

    @Override
    @Transactional
    public void createFromCheckout(Long roomId, String roomNo, Long orderId, String remark) {
        CleaningTask task = new CleaningTask();
        task.setRoomId(roomId);
        task.setRoomNo(roomNo);
        task.setOrderId(orderId);
        task.setStatus(CleaningStatus.PENDING);
        task.setPriority("normal");
        task.setRemark(remark);
        save(task);
        roomMapper.updateStatus(roomId, RoomStatus.DIRTY);
    }

    @Override
    @Transactional
    public Result assign(Long id, Long assigneeId, String assigneeName) {
        CleaningTask task = getById(id);
        if (task == null) {
            return Result.error("清扫任务不存在");
        }
        if (!CleaningStatus.PENDING.equals(task.getStatus())) {
            return Result.error("只有待分配的任务才能分配保洁员");
        }
        task.setAssigneeId(assigneeId);
        task.setAssigneeName(assigneeName);
        task.setStatus(CleaningStatus.ASSIGNED);
        updateById(task);
        return Result.success("分配成功", null);
    }

    @Override
    @Transactional
    public Result startCleaning(Long id) {
        CleaningTask task = getById(id);
        if (task == null) {
            return Result.error("清扫任务不存在");
        }
        if (!CleaningStatus.PENDING.equals(task.getStatus()) && !CleaningStatus.ASSIGNED.equals(task.getStatus())) {
            return Result.error("只有待分配或已分配的任务才能开始清扫");
        }
        task.setStatus(CleaningStatus.CLEANING);
        updateById(task);
        roomMapper.updateStatus(task.getRoomId(), RoomStatus.CLEANING);
        return Result.success("开始清扫", null);
    }

    @Override
    @Transactional
    public Result finishCleaning(Long id) {
        CleaningTask task = getById(id);
        if (task == null) {
            return Result.error("清扫任务不存在");
        }
        if (!CleaningStatus.CLEANING.equals(task.getStatus())) {
            return Result.error("只有清扫中的任务才能完成");
        }
        task.setStatus(CleaningStatus.INSPECTING);
        task.setFinishTime(LocalDateTime.now());
        updateById(task);
        return Result.success("清扫完成，待查房", null);
    }

    @Override
    @Transactional
    public Result inspect(Long id) {
        CleaningTask task = getById(id);
        if (task == null) {
            return Result.error("清扫任务不存在");
        }
        if (!CleaningStatus.INSPECTING.equals(task.getStatus())) {
            return Result.error("只有待查房的任务才能查房");
        }
        task.setStatus(CleaningStatus.COMPLETED);
        task.setInspectTime(LocalDateTime.now());
        updateById(task);
        roomMapper.updateStatus(task.getRoomId(), RoomStatus.IDLE);
        return Result.success("查房通过，房间已就绪", null);
    }

    @Override
    @Transactional
    public Result cancel(Long id) {
        CleaningTask task = getById(id);
        if (task == null) {
            return Result.error("清扫任务不存在");
        }
        if (CleaningStatus.COMPLETED.equals(task.getStatus()) || CleaningStatus.CANCELLED.equals(task.getStatus())) {
            return Result.error("该任务无法取消");
        }
        task.setStatus(CleaningStatus.CANCELLED);
        updateById(task);
        return Result.success("取消成功", null);
    }

    @Override
    public int countByStatus(String status) {
        return baseMapper.countByStatus(status);
    }

    @Override
    public Result<List<CleaningTask>> findPendingAndAssigned() {
        List<CleaningTask> list = baseMapper.findPendingAndAssigned();
        return Result.success(list);
    }
}
