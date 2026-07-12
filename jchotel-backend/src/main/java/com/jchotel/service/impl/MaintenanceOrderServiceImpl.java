package com.jchotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jchotel.constants.MaintenanceStatus;
import com.jchotel.constants.RoomStatus;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.MaintenanceOrder;
import com.jchotel.entity.Room;
import com.jchotel.mapper.MaintenanceOrderMapper;
import com.jchotel.mapper.RoomMapper;
import com.jchotel.service.MaintenanceOrderService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Service
public class MaintenanceOrderServiceImpl extends ServiceImpl<MaintenanceOrderMapper, MaintenanceOrder> implements MaintenanceOrderService {

    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    private final Random random = new Random();

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
    public Result<PageResult<MaintenanceOrder>> list(PageQuery query) {
        initPage(query);
        Long total = baseMapper.count(query);
        List<MaintenanceOrder> list = baseMapper.findList(query);
        PageResult<MaintenanceOrder> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }

    @Override
    public Result<MaintenanceOrder> detail(Long id) {
        MaintenanceOrder order = baseMapper.findDetailById(id);
        if (order == null) {
            return Result.error("维修工单不存在");
        }
        return Result.success(order);
    }

    @Override
    @Transactional
    public Result create(MaintenanceOrder order, Long reporterId, String reporterName) {
        if (order.getRoomId() == null) {
            return Result.error("请选择房间");
        }
        Room room = roomMapper.selectById(order.getRoomId());
        if (room == null) {
            return Result.error("房间不存在");
        }
        if (order.getTitle() == null || order.getTitle().trim().isEmpty()) {
            return Result.error("维修标题不能为空");
        }

        order.setOrderNo(generateOrderNo());
        order.setRoomNo(room.getRoomNo());
        order.setReporterId(reporterId);
        order.setReporterName(reporterName);
        order.setStatus(MaintenanceStatus.PENDING);
        if (order.getPriority() == null || order.getPriority().trim().isEmpty()) {
            order.setPriority("normal");
        }
        if (order.getCost() == null) {
            order.setCost(BigDecimal.ZERO);
        }
        save(order);
        roomMapper.updateStatus(room.getId(), RoomStatus.MAINTENANCE);

        return Result.success("提交维修工单成功", order);
    }

    @Override
    @Transactional
    public Result assign(Long id, Long assigneeId, String assigneeName) {
        MaintenanceOrder order = getById(id);
        if (order == null) {
            return Result.error("维修工单不存在");
        }
        if (!MaintenanceStatus.PENDING.equals(order.getStatus())) {
            return Result.error("只有待处理的工单才能分配维修员");
        }
        order.setAssigneeId(assigneeId);
        order.setAssigneeName(assigneeName);
        order.setStatus(MaintenanceStatus.ASSIGNED);
        updateById(order);
        return Result.success("分配成功", null);
    }

    @Override
    @Transactional
    public Result startProcessing(Long id) {
        MaintenanceOrder order = getById(id);
        if (order == null) {
            return Result.error("维修工单不存在");
        }
        if (!MaintenanceStatus.PENDING.equals(order.getStatus()) && !MaintenanceStatus.ASSIGNED.equals(order.getStatus())) {
            return Result.error("只有待处理或已分配的工单才能开始维修");
        }
        order.setStatus(MaintenanceStatus.PROCESSING);
        updateById(order);
        return Result.success("开始维修", null);
    }

    @Override
    @Transactional
    public Result finish(Long id, String solution, BigDecimal cost) {
        MaintenanceOrder order = getById(id);
        if (order == null) {
            return Result.error("维修工单不存在");
        }
        if (!MaintenanceStatus.PROCESSING.equals(order.getStatus())) {
            return Result.error("只有处理中的工单才能完成维修");
        }
        order.setSolution(solution);
        order.setCost(cost != null ? cost : BigDecimal.ZERO);
        order.setStatus(MaintenanceStatus.COMPLETED);
        order.setFinishTime(LocalDateTime.now());
        updateById(order);
        return Result.success("维修完成，待验收", null);
    }

    @Override
    @Transactional
    public Result verify(Long id) {
        MaintenanceOrder order = getById(id);
        if (order == null) {
            return Result.error("维修工单不存在");
        }
        if (!MaintenanceStatus.COMPLETED.equals(order.getStatus())) {
            return Result.error("只有已完成维修的工单才能验收");
        }
        order.setStatus(MaintenanceStatus.VERIFIED);
        order.setVerifyTime(LocalDateTime.now());
        updateById(order);

        int activeCount = baseMapper.countActiveByRoomIdExcludingId(order.getRoomId(), id);
        if (activeCount == 0) {
            roomMapper.updateStatus(order.getRoomId(), RoomStatus.IDLE);
        }
        return Result.success("验收通过，房间恢复可用", null);
    }

    @Override
    @Transactional
    public Result cancel(Long id) {
        MaintenanceOrder order = getById(id);
        if (order == null) {
            return Result.error("维修工单不存在");
        }
        if (MaintenanceStatus.VERIFIED.equals(order.getStatus()) || MaintenanceStatus.CANCELLED.equals(order.getStatus())) {
            return Result.error("该工单无法取消");
        }
        order.setStatus(MaintenanceStatus.CANCELLED);
        updateById(order);

        Room room = roomMapper.selectById(order.getRoomId());
        if (room != null && RoomStatus.MAINTENANCE.equals(room.getStatus())) {
            int activeCount = baseMapper.countActiveByRoomIdExcludingId(order.getRoomId(), id);
            if (activeCount == 0) {
                roomMapper.updateStatus(order.getRoomId(), RoomStatus.IDLE);
            }
        }
        return Result.success("取消成功", null);
    }

    @Override
    public int countByStatus(String status) {
        return baseMapper.countByStatus(status);
    }

    private synchronized String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(ORDER_NO_FMT);
        int rand = random.nextInt(10000);
        String orderNo = "WX" + timestamp + String.format("%04d", rand);
        while (baseMapper.findByOrderNo(orderNo) != null) {
            rand = random.nextInt(10000);
            orderNo = "WX" + LocalDateTime.now().format(ORDER_NO_FMT) + String.format("%04d", rand);
        }
        return orderNo;
    }
}
