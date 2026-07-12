package com.jchotel.service.impl;

// MyBatis-Plus通用服务实现基类
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// 维修工单状态常量
import com.jchotel.constants.MaintenanceStatus;
// 房间状态常量
import com.jchotel.constants.RoomStatus;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 分页结果封装
import com.jchotel.dto.PageResult;
// 维修工单实体类
import com.jchotel.entity.MaintenanceOrder;
// 房间实体类
import com.jchotel.entity.Room;
// 维修工单数据访问Mapper
import com.jchotel.mapper.MaintenanceOrderMapper;
// 房间数据访问Mapper
import com.jchotel.mapper.RoomMapper;
// 维修工单服务接口
import com.jchotel.service.MaintenanceOrderService;
// 统一响应结果封装
import com.jchotel.utils.Result;
// Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// Spring服务层注解
import org.springframework.stereotype.Service;
// 事务注解
import org.springframework.transaction.annotation.Transactional;

// 高精度小数类型，维修费用
import java.math.BigDecimal;
// 日期时间类
import java.time.LocalDateTime;
// 日期格式化类，生成工单编号
import java.time.format.DateTimeFormatter;
// List集合
import java.util.List;
// 随机数类，生成工单编号后缀
import java.util.Random;

/**
 * 维修工单管理服务实现类
 * 实现维修工单的创建、派单、处理、完工、验收、取消全流程
 * 工单创建时房间自动设为维修中，验收通过或全部取消时房间恢复空闲
 */
@Service
public class MaintenanceOrderServiceImpl extends ServiceImpl<MaintenanceOrderMapper, MaintenanceOrder> implements MaintenanceOrderService {

    // 工单编号日期格式化常量：yyyyMMddHHmmss
    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    // 随机数生成器，用于工单编号后缀（避免并发重复）
    private final Random random = new Random();

    @Autowired // 自动注入房间Mapper，用于更新房间状态
    private RoomMapper roomMapper;

    /**
     * 初始化分页参数
     */
    private void initPage(PageQuery query) {
        if (query.getPage() == null || query.getPage() < 1) query.setPage(1);
        if (query.getSize() == null || query.getSize() < 1) query.setSize(10);
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
        if (order == null) return Result.error("维修工单不存在");
        return Result.success(order);
    }

    /**
     * 创建维修工单（报修）
     * 房间状态设为维修中，生成工单号
     */
    @Override
    @Transactional
    public Result create(MaintenanceOrder order, Long reporterId, String reporterName) {
        if (order.getRoomId() == null) return Result.error("请选择房间");
        Room room = roomMapper.selectById(order.getRoomId());
        if (room == null) return Result.error("房间不存在");
        if (order.getTitle() == null || order.getTitle().trim().isEmpty()) return Result.error("维修标题不能为空");

        // 生成唯一工单号
        order.setOrderNo(generateOrderNo());
        // 设置房间号
        order.setRoomNo(room.getRoomNo());
        // 设置报修人
        order.setReporterId(reporterId);
        order.setReporterName(reporterName);
        // 初始状态：待处理
        order.setStatus(MaintenanceStatus.PENDING);
        // 默认优先级普通
        if (order.getPriority() == null || order.getPriority().trim().isEmpty()) order.setPriority("normal");
        // 默认维修费用0
        if (order.getCost() == null) order.setCost(BigDecimal.ZERO);
        save(order);
        // 将房间状态更新为维修中
        roomMapper.updateStatus(room.getId(), RoomStatus.MAINTENANCE);

        return Result.success("提交维修工单成功", order);
    }

    /**
     * 派单：分配维修人员
     */
    @Override
    @Transactional
    public Result assign(Long id, Long assigneeId, String assigneeName) {
        MaintenanceOrder order = getById(id);
        if (order == null) return Result.error("维修工单不存在");
        if (!MaintenanceStatus.PENDING.equals(order.getStatus())) return Result.error("只有待处理的工单才能分配维修员");
        order.setAssigneeId(assigneeId);
        order.setAssigneeName(assigneeName);
        order.setStatus(MaintenanceStatus.ASSIGNED);
        updateById(order);
        return Result.success("分配成功", null);
    }

    /**
     * 开始维修
     */
    @Override
    @Transactional
    public Result startProcessing(Long id) {
        MaintenanceOrder order = getById(id);
        if (order == null) return Result.error("维修工单不存在");
        if (!MaintenanceStatus.PENDING.equals(order.getStatus()) && !MaintenanceStatus.ASSIGNED.equals(order.getStatus()))
            return Result.error("只有待处理或已分配的工单才能开始维修");
        order.setStatus(MaintenanceStatus.PROCESSING);
        updateById(order);
        return Result.success("开始维修", null);
    }

    /**
     * 完工：维修完成，等待验收
     */
    @Override
    @Transactional
    public Result finish(Long id, String solution, BigDecimal cost) {
        MaintenanceOrder order = getById(id);
        if (order == null) return Result.error("维修工单不存在");
        if (!MaintenanceStatus.PROCESSING.equals(order.getStatus())) return Result.error("只有处理中的工单才能完成维修");
        order.setSolution(solution);
        order.setCost(cost != null ? cost : BigDecimal.ZERO);
        order.setStatus(MaintenanceStatus.COMPLETED);
        order.setFinishTime(LocalDateTime.now());
        updateById(order);
        return Result.success("维修完成，待验收", null);
    }

    /**
     * 验收：验收通过，检查该房间是否还有其他未完成维修单，没有则恢复房间空闲
     */
    @Override
    @Transactional
    public Result verify(Long id) {
        MaintenanceOrder order = getById(id);
        if (order == null) return Result.error("维修工单不存在");
        if (!MaintenanceStatus.COMPLETED.equals(order.getStatus())) return Result.error("只有已完成维修的工单才能验收");
        order.setStatus(MaintenanceStatus.VERIFIED);
        order.setVerifyTime(LocalDateTime.now());
        updateById(order);

        // 统计该房间除当前工单外是否还有其他进行中的维修单
        int activeCount = baseMapper.countActiveByRoomIdExcludingId(order.getRoomId(), id);
        if (activeCount == 0) {
            // 没有其他维修单了，房间恢复空闲
            roomMapper.updateStatus(order.getRoomId(), RoomStatus.IDLE);
        }
        return Result.success("验收通过，房间恢复可用", null);
    }

    /**
     * 取消工单
     * 取消后检查该房间是否还有其他进行中的维修单，没有则恢复空闲
     */
    @Override
    @Transactional
    public Result cancel(Long id) {
        MaintenanceOrder order = getById(id);
        if (order == null) return Result.error("维修工单不存在");
        if (MaintenanceStatus.VERIFIED.equals(order.getStatus()) || MaintenanceStatus.CANCELLED.equals(order.getStatus()))
            return Result.error("该工单无法取消");
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

    /**
     * 生成唯一工单号：WX+时间戳+4位随机数
     */
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
