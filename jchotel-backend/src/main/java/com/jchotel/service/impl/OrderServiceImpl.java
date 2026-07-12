package com.jchotel.service.impl;

// MyBatis-Plus通用服务实现基类，提供基础CRUD实现
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// 酒店系统常量类（订单状态、房间状态、VIP配置、支付类型等）
import com.jchotel.constants.*;
// 换房请求DTO
import com.jchotel.dto.ChangeRoomDTO;
// 入住办理请求DTO
import com.jchotel.dto.CheckinDTO;
// 退房结算请求DTO
import com.jchotel.dto.CheckoutDTO;
// 续住办理请求DTO
import com.jchotel.dto.ExtendStayDTO;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 分页结果封装
import com.jchotel.dto.PageResult;
// 酒店实体类（订单、客户、房间、房型、支付、换房日志、消费明细等）
import com.jchotel.entity.*;
// MyBatis数据访问Mapper
import com.jchotel.mapper.*;
// 清扫任务服务接口
import com.jchotel.service.CleaningTaskService;
// 订单服务接口
import com.jchotel.service.OrderService;
// 统一响应结果封装
import com.jchotel.utils.Result;
// Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// Spring服务层注解
import org.springframework.stereotype.Service;
// 事务注解，保证数据库操作原子性
import org.springframework.transaction.annotation.Transactional;

// HTTP请求对象，用于获取当前登录操作员ID
import jakarta.servlet.http.HttpServletRequest;
// 高精度小数类型，金额计算
import java.math.BigDecimal;
// 小数舍入模式
import java.math.RoundingMode;
// 星期枚举类，判断周末价
import java.time.DayOfWeek;
// 日期类，统计日期范围
import java.time.LocalDate;
// 日期时间类
import java.time.LocalDateTime;
// 日期格式化类
import java.time.format.DateTimeFormatter;
// 时间单位工具类，计算分钟差
import java.time.temporal.ChronoUnit;
// 集合工具类（List、Map、ArrayList、HashMap等）
import java.util.*;

/**
 * 订单服务实现类
 * 负责酒店订单的全生命周期管理：预订/入住/退房/续住/换房/取消
 * 包含房费计算（VIP折扣/周末价/超时计费）、押金管理、消费入账等核心业务
 * 使用@Service标记为Spring服务，继承ServiceImpl<OrderMapper, Order>获得MyBatis-Plus基础CRUD能力
 */
@Service // 标记为Spring服务组件
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    // 日期时间格式化器，格式"yyyy-MM-dd HH:mm:ss"，用于数据库日期查询和存储
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    // 日期格式化器，格式"yyyy-MM-dd"，用于统计查询日期范围
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // 显示用日期格式化器，格式"MM-dd"，用于前端图表展示
    private static final DateTimeFormatter DISPLAY_DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");
    // 入住时间容差（分钟），允许比当前时间早15分钟内仍可办理入住（解决时钟误差）
    private static final int CHECKIN_TOLERANCE_MINUTES = 15;
    // 立即入住时间窗口（分钟），当入住时间在未来30分钟内视为立即入住（直接办理入住而非预约）
    private static final int IMMEDIATE_CHECKIN_WINDOW_MINUTES = 30;

    @Autowired // 自动注入房间Mapper，用于房间状态查询和更新
    private RoomMapper roomMapper;
    @Autowired // 自动注入房型Mapper，用于查询房型周末价等配置
    private RoomTypeMapper roomTypeMapper;
    @Autowired // 自动注入客户Mapper，用于客户信息查询和VIP等级更新
    private CustomerMapper customerMapper;
    @Autowired // 自动注入订单消费明细Mapper，用于查询客房消费总额
    private OrderItemMapper orderItemMapper;
    @Autowired // 自动注入换房日志Mapper，用于记录换房历史
    private RoomChangeLogMapper roomChangeLogMapper;
    @Autowired // 自动注入清扫任务服务，退房/换房时创建清扫任务
    private CleaningTaskService cleaningTaskService;
    @Autowired // 自动注入支付记录Mapper，用于记录押金等支付流水
    private PaymentMapper paymentMapper;

    /**
     * 初始化分页参数
     * 校验页码和页大小合法性，设置默认值，计算OFFSET偏移量
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
     * VIP折扣计算
     * 根据VIP等级应用对应折扣：银卡95折/金卡9折/钻石85折
     * @param originalPrice 原价
     * @param vipLevel VIP等级（0普通/1银卡/2金卡/3钻石）
     * @return 折后价，保留2位小数四舍五入
     */
    private BigDecimal calculateVipPrice(BigDecimal originalPrice, Integer vipLevel) {
        // 非VIP客户直接返回原价
        if (vipLevel == null || vipLevel == 0) return originalPrice;
        // 声明折扣率变量
        BigDecimal discount;
        // 根据VIP等级选择折扣率
        switch (vipLevel) {
            // 银卡：使用配置中的银卡折扣率
            case 1: discount = VipConfig.SILVER_DISCOUNT; break;
            // 金卡：使用配置中的金卡折扣率
            case 2: discount = VipConfig.GOLD_DISCOUNT; break;
            // 钻石：使用配置中的钻石折扣率
            case 3: discount = VipConfig.DIAMOND_DISCOUNT; break;
            // 未知等级默认不打折
            default: discount = BigDecimal.ONE;
        }
        // 应用折扣：原价 × 折扣率，保留2位小数四舍五入
        return originalPrice.multiply(discount).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 获取房间实际单价
     * 综合考虑周末价和VIP折扣计算当日房价
     * @param room 房间实体
     * @param roomType 房型实体（包含周末价配置）
     * @param customer 客户实体（包含VIP等级）
     * @param date 日期（判断是否周末）
     * @return 实际单价
     */
    private BigDecimal getEffectivePrice(Room room, RoomType roomType, Customer customer, LocalDateTime date) {
        // 房间不存在或未设置价格，返回0
        if (room == null || room.getPrice() == null) return BigDecimal.ZERO;
        // 获取房间基础价格
        BigDecimal basePrice = room.getPrice();
        // 获取当日星期几
        DayOfWeek dow = date.getDayOfWeek();
        // 判断是否周末（周五、周六）
        boolean isWeekend = (dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY);
        // 如果是周末且房型配置了周末价，使用周末价
        if (isWeekend && roomType != null && roomType.getWeekendPrice() != null
                && roomType.getWeekendPrice().compareTo(BigDecimal.ZERO) > 0) {
            basePrice = roomType.getWeekendPrice();
        }
        // 如果客户是VIP，应用VIP折扣
        if (customer != null && customer.getVipLevel() != null && customer.getVipLevel() > 0) {
            basePrice = calculateVipPrice(basePrice, customer.getVipLevel());
        }
        // 返回最终单价
        return basePrice;
    }

    /**
     * 计算入住天数
     * 不满1天按1天计，使用Math.ceil向上取整
     * @param checkIn 入住时间
     * @param checkOut 退房时间
     * @return 入住天数（至少1天）
     */
    private int calculateNights(LocalDateTime checkIn, LocalDateTime checkOut) {
        // 计算入住总分钟数
        long minutes = ChronoUnit.MINUTES.between(checkIn, checkOut);
        // 转换为天数并向上取整：分钟数 / (24*60)
        int nights = (int) Math.ceil(minutes / (24.0 * 60.0));
        // 至少返回1天（即时入住不到24小时也按1天算）
        return Math.max(nights, 1);
    }

    /**
     * 根据入住次数自动升级VIP等级
     * 阈值规则：银卡5次/金卡15次/钻石30次，只升级不降级
     * @param customer 客户实体
     */
    private void upgradeVipIfNeeded(Customer customer) {
        // 获取当前入住次数，为空默认为0
        int count = customer.getCheckInCount() == null ? 0 : customer.getCheckInCount();
        // 初始化新VIP等级为普通会员
        int newLevel = 0;
        // 达到钻石卡阈值（30次）升级为钻石
        if (count >= VipConfig.DIAMOND_THRESHOLD) newLevel = 3;
        // 达到金卡阈值（15次）升级为金卡
        else if (count >= VipConfig.GOLD_THRESHOLD) newLevel = 2;
        // 达到银卡阈值（5次）升级为银卡
        else if (count >= VipConfig.SILVER_THRESHOLD) newLevel = 1;
        // 获取当前VIP等级，为空默认为0
        // 只有新等级高于旧等级才更新（VIP只升不降）
        if (newLevel > (customer.getVipLevel() == null ? 0 : customer.getVipLevel())) {
            // 更新内存中的客户VIP等级
            customer.setVipLevel(newLevel);
            // 构建更新对象，只更新VIP等级字段
            Customer upd = new Customer();
            upd.setId(customer.getId());
            upd.setVipLevel(newLevel);
            // 执行数据库更新
            customerMapper.updateById(upd);
        }
    }

    /**
     * 分页查询订单列表
     * @param query 分页查询参数
     * @return 分页订单列表
     */
    @Override
    public Result<PageResult<Order>> list(PageQuery query) {
        // 初始化分页参数（设置默认值、计算偏移量）
        initPage(query);
        // 查询符合条件的订单总数
        Long total = baseMapper.count(query);
        // 查询当前页订单列表
        List<Order> list = baseMapper.findList(query);
        // 构建分页结果对象
        PageResult<Order> pageResult = new PageResult<>();
        // 设置总记录数
        pageResult.setTotal(total);
        // 设置当前页数据列表
        pageResult.setList(list);
        // 返回成功结果
        return Result.success(pageResult);
    }

    /**
     * 查询订单详情
     * @param id 订单ID
     * @return 订单详细信息（关联客户、房间信息）
     */
    @Override
    public Result<Order> detail(Long id) {
        // 查询订单详情（关联查询客户名、房间号等信息）
        Order order = baseMapper.findDetailById(id);
        // 订单不存在返回错误
        if (order == null) return Result.error("订单不存在");
        // 返回订单详情
        return Result.success(order);
    }

    /**
     * 办理入住/预约
     * 支持两种场景：立即入住（30分钟内）直接办理，超过则创建预约订单
     * 事务边界：订单创建、房间状态更新、客户入住次数更新、押金记录在同一事务中
     * @param checkinDTO 入住信息（房间ID、客户ID、入住/退房时间、押金、备注）
     * @param request HTTP请求获取当前操作员ID
     * @return 办理结果，包含订单ID、订单号、状态
     */
    @Override
    @Transactional // 开启事务保证数据一致性
    public Result<Map<String, Object>> checkin(CheckinDTO checkinDTO, HttpServletRequest request) {
        // 获取预计入住时间
        LocalDateTime checkInTime = checkinDTO.getCheckInTime();
        // 获取预计退房时间
        LocalDateTime expectedCheckOutTime = checkinDTO.getExpectedCheckOutTime();
        // 获取当前系统时间
        LocalDateTime now = LocalDateTime.now();

        // 校验入住时间：不能早于当前时间超过15分钟容差
        if (checkInTime.isBefore(now.minusMinutes(CHECKIN_TOLERANCE_MINUTES))) {
            return Result.error("入住时间不能早于当前时间（允许15分钟误差）");
        }
        // 校验退房时间：必须晚于入住时间
        if (!expectedCheckOutTime.isAfter(checkInTime)) {
            return Result.error("预计退房时间必须晚于入住时间");
        }

        // 查询房间信息
        Room room = roomMapper.selectById(checkinDTO.getRoomId());
        // 房间不存在返回错误
        if (room == null) return Result.error("客房不存在");
        // 房间在维修中，不可预约
        if (RoomStatus.MAINTENANCE.equals(room.getStatus())) return Result.error("维修中的房间不可预约");
        // 房间正在清扫或脏房，暂时不可入住
        if (RoomStatus.CLEANING.equals(room.getStatus()) || RoomStatus.DIRTY.equals(room.getStatus())) {
            return Result.error("该房间正在清扫中，暂时不可入住");
        }

        // 查询客户信息
        Customer customer = customerMapper.selectById(checkinDTO.getCustomerId());
        // 客户不存在返回错误
        if (customer == null) return Result.error("客户不存在");
        // 客户在黑名单中，禁止入住
        if (customer.getIsBlacklist() != null && customer.getIsBlacklist() == 1) {
            return Result.error("该客户在黑名单中：" + (customer.getBlacklistReason() != null ? customer.getBlacklistReason() : "禁止入住"));
        }

        // 校验押金：不能为负数
        if (checkinDTO.getDeposit() == null || checkinDTO.getDeposit().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("押金不能为负数");
        }

        // 格式化入住时间为字符串用于数据库查询
        String checkInStr = checkInTime.format(DATETIME_FMT);
        // 格式化预计退房时间为字符串用于数据库查询
        String expectedCheckOutStr = expectedCheckOutTime.format(DATETIME_FMT);

        // 查询该时间段内房间是否有冲突订单
        int conflicts = baseMapper.countConflictOrders(checkinDTO.getRoomId(), checkInStr, expectedCheckOutStr);
        // 存在冲突订单，返回错误
        if (conflicts > 0) {
            return Result.error("该房间在所选时间段已被预约或入住，请选择其他房间或时间段");
        }

        // 计算入住天数
        int nights = calculateNights(checkInTime, expectedCheckOutTime);
        // 声明房型变量
        RoomType roomType = null;
        // 如果房间关联了房型ID，查询房型信息
        if (room.getTypeId() != null) {
            roomType = roomTypeMapper.selectById(room.getTypeId());
        }

        // 初始化预计房费为0
        BigDecimal expectedRoomAmount = BigDecimal.ZERO;
        // 日期游标，从入住时间开始逐天计算
        LocalDateTime dayCursor = checkInTime;
        // 循环计算每天的房费（考虑周末价）
        for (int i = 0; i < nights; i++) {
            // 累加当日有效房价
            expectedRoomAmount = expectedRoomAmount.add(getEffectivePrice(room, roomType, customer, dayCursor));
            // 游标移到下一天
            dayCursor = dayCursor.plusDays(1);
        }
        // 房费保留2位小数四舍五入
        expectedRoomAmount = expectedRoomAmount.setScale(2, RoundingMode.HALF_UP);

        // 从请求属性中获取当前登录操作员ID（拦截器解析JWT后设置）
        Long userId = (Long) request.getAttribute("userId");

        // 创建订单对象
        Order order = new Order();
        // 生成唯一订单号
        order.setOrderNo(generateOrderNo());
        // 设置客户ID
        order.setCustomerId(checkinDTO.getCustomerId());
        // 设置房间ID
        order.setRoomId(checkinDTO.getRoomId());
        // 设置操作员ID
        order.setUserId(userId);
        // 设置入住时间
        order.setCheckInTime(checkInTime);
        // 设置预计退房时间
        order.setExpectedCheckOutTime(expectedCheckOutTime);
        // 设置押金
        order.setDeposit(checkinDTO.getDeposit());
        // 押金未退还标记（0=未退）
        order.setDepositRefunded(0);
        // 设置预计房费
        order.setRoomAmount(expectedRoomAmount);
        // 初始化额外消费为0
        order.setExtraAmount(BigDecimal.ZERO);
        // 订单总额=房费（暂无消费）
        order.setTotalAmount(expectedRoomAmount);
        // 未换房标记（0=未换房）
        order.setRoomChanged(0);
        // 订单渠道：前台
        order.setChannel("front");
        // 设置订单备注
        order.setRemark(checkinDTO.getRemark());

        // 构建返回数据Map
        Map<String, Object> data = new HashMap<>();
        // 计算距离入住时间还有多少分钟
        long minutesToCheckIn = ChronoUnit.MINUTES.between(now, checkInTime);
        // 30分钟内入住，直接办理入住
        if (minutesToCheckIn <= IMMEDIATE_CHECKIN_WINDOW_MINUTES) {
            // 设置订单状态为已入住
            order.setStatus(OrderStatus.CHECKED_IN);
            // 更新房间状态为入住中
            roomMapper.updateStatus(room.getId(), RoomStatus.OCCUPIED);
            // 增加客户入住次数
            customerMapper.increaseCheckInCount(customer.getId());
            // 保存订单
            save(order);
            // 更新内存中客户的入住次数（用于VIP升级判断）
            customer.setCheckInCount((customer.getCheckInCount() == null ? 0 : customer.getCheckInCount()) + 1);
            // 根据入住次数检查是否需要升级VIP
            upgradeVipIfNeeded(customer);

            // 如果押金>0，记录押金支付流水
            if (checkinDTO.getDeposit().compareTo(BigDecimal.ZERO) > 0) {
                // 创建支付记录对象
                Payment depositPay = new Payment();
                depositPay.setOrderId(order.getId());
                depositPay.setOrderNo(order.getOrderNo());
                // 支付方式默认现金
                depositPay.setPaymentMethod("cash");
                depositPay.setAmount(checkinDTO.getDeposit());
                // 支付类型：押金
                depositPay.setType(PaymentType.DEPOSIT);
                depositPay.setRemark("入住押金");
                depositPay.setOperatorId(userId);
                depositPay.setCreateTime(now);
                // 插入支付记录
                paymentMapper.insert(depositPay);
            }

            // 返回订单ID
            data.put("orderId", order.getId());
            // 返回订单号
            data.put("orderNo", order.getOrderNo());
            // 返回订单状态：已入住
            data.put("status", OrderStatus.CHECKED_IN);
            // 返回入住成功
            return Result.success("入住办理成功", data);
        } else {
            // 超过30分钟，创建预约订单
            // 设置订单状态为待入住
            order.setStatus(OrderStatus.PENDING);
            // 保存预约订单
            save(order);
            // 返回订单ID
            data.put("orderId", order.getId());
            // 返回订单号
            data.put("orderNo", order.getOrderNo());
            // 返回订单状态：待入住
            data.put("status", OrderStatus.PENDING);
            // 返回预约成功
            return Result.success("预约成功", data);
        }
    }

    /**
     * 确认预约订单办理入住
     * 将待入住状态的订单转为已入住，重新计算房费（以实际入住时间为准）
     * 事务边界：订单更新、房间状态更新、客户入住次数更新、VIP升级在同一事务中
     * @param id 订单ID
     * @param request HTTP请求获取操作员信息
     * @return 办理结果
     */
    @Override
    @Transactional // 开启事务保证数据一致性
    public Result<Map<String, Object>> settlePending(Long id, HttpServletRequest request) {
        // 查询订单信息
        Order order = getById(id);
        // 订单不存在返回错误
        if (order == null) return Result.error("订单不存在");
        // 只有待入住订单才能办理入住
        if (!OrderStatus.PENDING.equals(order.getStatus())) return Result.error("只有待入住订单才能办理入住");

        // 查询房间信息
        Room room = roomMapper.selectById(order.getRoomId());
        // 房间不存在返回错误
        if (room == null) return Result.error("客房不存在");
        // 房间在维修中无法入住
        if (RoomStatus.MAINTENANCE.equals(room.getStatus())) return Result.error("维修中的房间无法办理入住");

        // 查询客户信息
        Customer customer = customerMapper.selectById(order.getCustomerId());
        // 客户不存在返回错误
        if (customer == null) return Result.error("客户不存在");
        // 客户在黑名单中无法入住
        if (customer.getIsBlacklist() != null && customer.getIsBlacklist() == 1) {
            return Result.error("该客户在黑名单中，无法办理入住");
        }

        // 获取当前时间作为实际入住时间
        LocalDateTime now = LocalDateTime.now();
        // 更新订单实际入住时间为当前时间
        order.setCheckInTime(now);
        // 更新订单状态为已入住
        order.setStatus(OrderStatus.CHECKED_IN);

        // 重新计算实际入住天数
        int settleNights = calculateNights(now, order.getExpectedCheckOutTime());
        // 声明房型变量
        RoomType settleRoomType = null;
        // 查询房型信息
        if (room.getTypeId() != null) {
            settleRoomType = roomTypeMapper.selectById(room.getTypeId());
        }
        // 初始化房费为0
        BigDecimal settleRoomAmount = BigDecimal.ZERO;
        // 日期游标从当前实际入住时间开始
        LocalDateTime settleDayCursor = now;
        // 逐天计算房费
        for (int i = 0; i < settleNights; i++) {
            settleRoomAmount = settleRoomAmount.add(getEffectivePrice(room, settleRoomType, customer, settleDayCursor));
            settleDayCursor = settleDayCursor.plusDays(1);
        }
        // 房费保留2位小数
        settleRoomAmount = settleRoomAmount.setScale(2, RoundingMode.HALF_UP);
        // 更新订单预计房费
        order.setRoomAmount(settleRoomAmount);
        // 更新订单总额
        order.setTotalAmount(settleRoomAmount);

        // 更新订单信息
        updateById(order);

        // 更新房间状态为入住中
        roomMapper.updateStatus(room.getId(), RoomStatus.OCCUPIED);
        // 增加客户入住次数
        customerMapper.increaseCheckInCount(customer.getId());
        // 更新内存中客户入住次数
        customer.setCheckInCount((customer.getCheckInCount() == null ? 0 : customer.getCheckInCount()) + 1);
        // 检查VIP升级
        upgradeVipIfNeeded(customer);

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderNo", order.getOrderNo());
        return Result.success("办理入住成功", data);
    }

    /**
     * 办理退房结算
     * 重新计算实际房费、汇总消费、计算押金抵扣、生成结算单，更新房间为脏房并创建清扫任务
     * 事务边界：订单结算、房间状态更新、客户消费统计、清扫任务创建在同一事务中
     * @param id 订单ID
     * @param checkoutDTO 退房信息（实际退房时间）
     * @return 结算详情（房费、消费、总额、押金、应收/应退、消费明细）
     */
    @Override
    @Transactional // 开启事务保证数据一致性
    public Result<Map<String, Object>> checkout(Long id, CheckoutDTO checkoutDTO) {
        // 查询订单信息
        Order order = getById(id);
        // 订单不存在返回错误
        if (order == null) return Result.error("订单不存在");
        // 只有已入住订单才能退房
        if (!OrderStatus.CHECKED_IN.equals(order.getStatus())) return Result.error("只有已入住订单才能退房");

        // 获取实际退房时间
        LocalDateTime actualCheckOutTime = checkoutDTO.getActualCheckOutTime();
        // 获取当前时间
        LocalDateTime now = LocalDateTime.now();
        // 如果未指定退房时间，默认当前时间
        if (actualCheckOutTime == null) actualCheckOutTime = now;
        // 实际退房时间不能晚于当前时间
        if (actualCheckOutTime.isAfter(now)) return Result.error("实际退房时间不能晚于当前时间");
        // 退房时间必须晚于入住时间
        if (!actualCheckOutTime.isAfter(order.getCheckInTime())) return Result.error("退房时间必须晚于入住时间");

        // 计算实际入住天数
        int nights = calculateNights(order.getCheckInTime(), actualCheckOutTime);
        // 查询房间信息
        Room room = roomMapper.selectById(order.getRoomId());
        // 房间不存在无法退房
        if (room == null) return Result.error("房间信息不存在，无法退房");
        // 查询客户信息
        Customer customer = customerMapper.selectById(order.getCustomerId());
        // 声明房型变量
        RoomType roomType = null;
        // 查询房型信息
        if (room.getTypeId() != null) {
            roomType = roomTypeMapper.selectById(room.getTypeId());
        }

        // 初始化实际房费为0
        BigDecimal roomAmount = BigDecimal.ZERO;
        // 日期游标从入住时间开始
        LocalDateTime dayCursor = order.getCheckInTime();
        // 逐天计算实际房费
        for (int i = 0; i < nights; i++) {
            roomAmount = roomAmount.add(getEffectivePrice(room, roomType, customer, dayCursor));
            dayCursor = dayCursor.plusDays(1);
        }
        // 房费保留2位小数
        roomAmount = roomAmount.setScale(2, RoundingMode.HALF_UP);

        // 查询订单附加消费总额（小冰箱、洗衣等）
        BigDecimal extraAmount = orderItemMapper.sumByOrderId(order.getId());
        // 消费为空默认0
        if (extraAmount == null) extraAmount = BigDecimal.ZERO;

        // 计算总费用：房费 + 额外消费
        BigDecimal totalAmount = roomAmount.add(extraAmount);
        // 获取已付押金，为空默认0
        BigDecimal deposit = order.getDeposit() == null ? BigDecimal.ZERO : order.getDeposit();
        // 计算结算差额：总费用 - 押金（正数需补付，负数需退还）
        BigDecimal balance = totalAmount.subtract(deposit);

        // 更新订单实际退房时间
        order.setActualCheckOutTime(actualCheckOutTime);
        // 更新实际房费
        order.setRoomAmount(roomAmount);
        // 更新额外消费
        order.setExtraAmount(extraAmount);
        // 更新订单总额
        order.setTotalAmount(totalAmount);
        // 更新订单状态为已退房
        order.setStatus(OrderStatus.CHECKED_OUT);
        // 执行订单更新
        updateById(order);

        // 更新房间状态为脏房（待清扫）
        roomMapper.updateStatus(room.getId(), RoomStatus.DIRTY);
        // 更新客户累计消费金额
        baseMapper.updateCustomerSpending(order.getCustomerId(), totalAmount);
        // 创建退房清扫任务
        cleaningTaskService.createFromCheckout(room.getId(), room.getRoomNo(), order.getId(), order.getRemark());

        // 计算日均房价
        BigDecimal avgPrice = nights > 0 ? roomAmount.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 构建结算详情返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderNo", order.getOrderNo());
        data.put("customerName", customer != null ? customer.getName() : "");
        data.put("customerPhone", customer != null ? customer.getPhone() : "");
        data.put("roomNo", room.getRoomNo());
        data.put("roomTypeName", roomType != null ? roomType.getName() : "");
        data.put("nights", nights);
        data.put("checkInTime", order.getCheckInTime());
        data.put("actualCheckOutTime", actualCheckOutTime);
        data.put("roomAmount", roomAmount);
        data.put("extraAmount", extraAmount);
        data.put("totalAmount", totalAmount);
        data.put("avgPrice", avgPrice);
        data.put("deposit", deposit);
        data.put("balance", balance);
        // 是否需要补付（差额>0）
        data.put("needPay", balance.compareTo(BigDecimal.ZERO) > 0);
        // 是否需要退还（差额<0）
        data.put("needRefund", balance.compareTo(BigDecimal.ZERO) < 0);
        // 查询消费明细列表
        data.put("items", orderItemMapper.findByOrderId(order.getId()));
        // 返回退房成功和结算详情
        return Result.success("退房成功", data);
    }

    /**
     * 取消待入住订单
     * 只有待入住状态的订单可以取消，已入住订单不能取消
     * @param id 订单ID
     * @return 取消结果
     */
    @Override
    @Transactional // 开启事务
    public Result<String> cancel(Long id) {
        // 查询订单信息
        Order order = getById(id);
        // 订单不存在返回错误
        if (order == null) return Result.error("订单不存在");
        // 只有待入住订单才能取消
        if (!OrderStatus.PENDING.equals(order.getStatus())) return Result.error("只有待入住订单才能取消");
        // 更新订单状态为已取消
        order.setStatus(OrderStatus.CANCELLED);
        // 执行更新
        updateById(order);
        // 返回取消成功
        return Result.success("取消成功", null);
    }

    /**
     * 办理续住
     * 校验续住时段房间无冲突，计算续住房费，更新预计退房时间和订单总额
     * 事务边界：订单更新在同一事务中
     * @param dto 续住信息（订单ID、新退房时间）
     * @param request HTTP请求获取操作员ID
     * @return 续住结果（新增天数、续住房费）
     */
    @Override
    @Transactional // 开启事务保证数据一致性
    public Result<Map<String, Object>> extendStay(ExtendStayDTO dto, HttpServletRequest request) {
        // 查询订单信息
        Order order = getById(dto.getOrderId());
        // 订单不存在返回错误
        if (order == null) return Result.error("订单不存在");
        // 只有在住订单才能续住
        if (!OrderStatus.CHECKED_IN.equals(order.getStatus())) return Result.error("只有在住订单才能续住");

        // 获取新的预计退房时间
        LocalDateTime newCheckOut = dto.getNewCheckOutTime();
        // 新退房时间必须晚于原预计退房时间
        if (!newCheckOut.isAfter(order.getExpectedCheckOutTime())) {
            return Result.error("新退房时间必须晚于原预计退房时间");
        }

        // 格式化新退房时间为字符串
        String newOutStr = newCheckOut.format(DATETIME_FMT);
        // 查询原退房时间到新退房时间之间房间是否有其他预约（排除当前订单）
        int conflicts = baseMapper.countConflictOrdersExcludeId(order.getRoomId(),
                order.getExpectedCheckOutTime().format(DATETIME_FMT), newOutStr, order.getId());
        // 有冲突订单无法续住
        if (conflicts > 0) {
            return Result.error("续住时段房间已被预约，无法续住");
        }

        // 计算续住天数
        int extraNights = calculateNights(order.getExpectedCheckOutTime(), newCheckOut);
        // 查询房间信息
        Room room = roomMapper.selectById(order.getRoomId());
        // 查询客户信息
        Customer customer = customerMapper.selectById(order.getCustomerId());
        // 声明房型变量
        RoomType roomType = null;
        // 查询房型信息
        if (room != null && room.getTypeId() != null) {
            roomType = roomTypeMapper.selectById(room.getTypeId());
        }

        // 初始化续住房费为0
        BigDecimal extraRoomAmount = BigDecimal.ZERO;
        // 日期游标从原预计退房时间开始
        LocalDateTime dayCursor = order.getExpectedCheckOutTime();
        // 逐天计算续住房费
        for (int i = 0; i < extraNights; i++) {
            extraRoomAmount = extraRoomAmount.add(getEffectivePrice(room, roomType, customer, dayCursor));
            dayCursor = dayCursor.plusDays(1);
        }
        // 续住房费保留2位小数
        extraRoomAmount = extraRoomAmount.setScale(2, RoundingMode.HALF_UP);

        // 获取当前房费，为空默认0
        BigDecimal currentRoomAmount = order.getRoomAmount() != null ? order.getRoomAmount() : BigDecimal.ZERO;
        // 获取当前额外消费，为空默认0
        BigDecimal currentExtraAmount = order.getExtraAmount() != null ? order.getExtraAmount() : BigDecimal.ZERO;
        // 更新订单预计退房时间为新时间
        order.setExpectedCheckOutTime(newCheckOut);
        // 更新房费：原房费 + 续住房费
        order.setRoomAmount(currentRoomAmount.add(extraRoomAmount).setScale(2, RoundingMode.HALF_UP));
        // 更新订单总额：新房费 + 原额外消费
        order.setTotalAmount(order.getRoomAmount().add(currentExtraAmount).setScale(2, RoundingMode.HALF_UP));
        // 执行订单更新
        updateById(order);

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderNo", order.getOrderNo());
        data.put("newCheckOutTime", newCheckOut);
        data.put("extraNights", extraNights);
        data.put("extraRoomAmount", extraRoomAmount);
        // 返回续住成功
        return Result.success("续住办理成功，新增" + extraNights + "晚", data);
    }

    /**
     * 办理换房
     * 校验目标房间可用，计算剩余天数新旧房间差价，记录换房日志，更新原房间为脏房并创建清扫任务
     * 事务边界：换房日志、房间状态更新、清扫任务创建、订单更新在同一事务中
     * @param dto 换房信息（订单ID、目标房间ID、换房原因）
     * @param request HTTP请求获取操作员ID
     * @return 换房结果（房间号变更、差价信息）
     */
    @Override
    @Transactional // 开启事务保证数据一致性
    public Result<Map<String, Object>> changeRoom(ChangeRoomDTO dto, HttpServletRequest request) {
        // 查询订单信息
        Order order = getById(dto.getOrderId());
        // 订单不存在返回错误
        if (order == null) return Result.error("订单不存在");
        // 只有在住订单才能换房
        if (!OrderStatus.CHECKED_IN.equals(order.getStatus())) return Result.error("只有在住订单才能换房");

        // 查询目标房间信息
        Room toRoom = roomMapper.selectById(dto.getToRoomId());
        // 目标房间不存在返回错误
        if (toRoom == null) return Result.error("目标房间不存在");
        // 目标房间必须是空闲状态才能换入
        if (!RoomStatus.IDLE.equals(toRoom.getStatus())) return Result.error("目标房间当前不可用");

        // 查询原房间信息
        Room fromRoom = roomMapper.selectById(order.getRoomId());
        // 不能换入同一房间
        if (fromRoom.getId().equals(toRoom.getId())) return Result.error("不能换入同一房间");

        // 格式化当前时间为字符串
        String nowStr = LocalDateTime.now().format(DATETIME_FMT);
        // 格式化预计退房时间为字符串
        String outStr = order.getExpectedCheckOutTime().format(DATETIME_FMT);
        // 查询目标房间在剩余时段是否被占用
        int conflicts = baseMapper.countConflictOrders(toRoom.getId(), nowStr, outStr);
        // 目标房间在剩余时段已被占用
        if (conflicts > 0) return Result.error("目标房间在剩余时段已被占用");

        // 查询客户信息
        Customer changeCustomer = customerMapper.selectById(order.getCustomerId());
        // 声明原房间房型变量
        RoomType fromRoomType = null;
        // 声明目标房间房型变量
        RoomType toRoomType = null;
        // 查询原房间房型信息
        if (fromRoom.getTypeId() != null) fromRoomType = roomTypeMapper.selectById(fromRoom.getTypeId());
        // 查询目标房间房型信息
        if (toRoom.getTypeId() != null) toRoomType = roomTypeMapper.selectById(toRoom.getTypeId());

        // 计算剩余入住天数
        int remainingDays = calculateNights(LocalDateTime.now(), order.getExpectedCheckOutTime());
        // 初始化剩余天数原房间房费为0
        BigDecimal remainingFromAmount = BigDecimal.ZERO;
        // 初始化剩余天数目标房间房费为0
        BigDecimal remainingToAmount = BigDecimal.ZERO;
        // 日期游标从当前时间开始
        LocalDateTime changeDayCursor = LocalDateTime.now();
        // 逐天计算剩余天数新旧房间的房费差价
        for (int i = 0; i < remainingDays; i++) {
            // 累加原房间当日房价
            remainingFromAmount = remainingFromAmount.add(getEffectivePrice(fromRoom, fromRoomType, changeCustomer, changeDayCursor));
            // 累加目标房间当日房价
            remainingToAmount = remainingToAmount.add(getEffectivePrice(toRoom, toRoomType, changeCustomer, changeDayCursor));
            // 游标移到下一天
            changeDayCursor = changeDayCursor.plusDays(1);
        }
        // 计算差价：目标房间 - 原房间（正数补差价，负数退差价）
        BigDecimal totalPriceDiff = remainingToAmount.subtract(remainingFromAmount).setScale(2, RoundingMode.HALF_UP);

        // 创建换房日志记录
        RoomChangeLog log = new RoomChangeLog();
        log.setOrderId(order.getId());
        log.setOrderNo(order.getOrderNo());
        log.setFromRoomId(fromRoom.getId());
        log.setFromRoomNo(fromRoom.getRoomNo());
        log.setToRoomId(toRoom.getId());
        log.setToRoomNo(toRoom.getRoomNo());
        log.setReason(dto.getReason());
        log.setPriceDiff(totalPriceDiff);
        // 获取当前操作员ID
        Long userId = (Long) request.getAttribute("userId");
        log.setOperatorId(userId);
        log.setCreateTime(LocalDateTime.now());
        // 插入换房日志
        roomChangeLogMapper.insert(log);

        // 原房间设置为脏房（待清扫）
        roomMapper.updateStatus(fromRoom.getId(), RoomStatus.DIRTY);
        // 目标房间设置为入住中
        roomMapper.updateStatus(toRoom.getId(), RoomStatus.OCCUPIED);
        // 为原房间创建换房清扫任务
        cleaningTaskService.createFromCheckout(fromRoom.getId(), fromRoom.getRoomNo(), order.getId(), "换房清扫");

        // 记录原房间ID（用于历史追溯）
        order.setOriginalRoomId(fromRoom.getId());
        // 更新订单房间ID为新房间
        order.setRoomId(toRoom.getId());
        // 标记为已换房
        order.setRoomChanged(1);
        // 获取当前房费
        BigDecimal changeCurrentRoomAmount = order.getRoomAmount() != null ? order.getRoomAmount() : BigDecimal.ZERO;
        // 获取当前额外消费
        BigDecimal changeCurrentExtraAmount = order.getExtraAmount() != null ? order.getExtraAmount() : BigDecimal.ZERO;
        // 更新房费：原房费 + 差价
        order.setRoomAmount(changeCurrentRoomAmount.add(totalPriceDiff).setScale(2, RoundingMode.HALF_UP));
        // 更新订单总额：新房费 + 额外消费
        order.setTotalAmount(order.getRoomAmount().add(changeCurrentExtraAmount).setScale(2, RoundingMode.HALF_UP));
        // 执行订单更新
        updateById(order);

        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("fromRoomNo", fromRoom.getRoomNo());
        data.put("toRoomNo", toRoom.getRoomNo());
        data.put("priceDiff", totalPriceDiff);
        data.put("remainingDays", remainingDays);
        // 构建提示消息
        String msg = "换房成功：" + fromRoom.getRoomNo() + " → " + toRoom.getRoomNo();
        // 差价>0需补付
        if (totalPriceDiff.compareTo(BigDecimal.ZERO) > 0) msg += "，需补差价¥" + totalPriceDiff;
        // 差价<0需退还
        else if (totalPriceDiff.compareTo(BigDecimal.ZERO) < 0) msg += "，退还差价¥" + totalPriceDiff.abs();
        // 返回换房成功
        return Result.success(msg, data);
    }

    /**
     * 查询今日预计到店订单列表
     * @return 今日待入住订单列表
     */
    @Override
    public Result<List<Order>> todayArrivals() {
        // 查询今日预计到店订单并返回
        return Result.success(baseMapper.findTodayArrivals());
    }

    /**
     * 查询今日预计离店订单列表
     * @return 今日待退房订单列表
     */
    @Override
    public Result<List<Order>> todayDepartures() {
        // 查询今日预计离店订单并返回
        return Result.success(baseMapper.findTodayDepartures());
    }

    /**
     * 查询客户历史订单列表
     * @param customerId 客户ID
     * @return 客户订单列表
     */
    @Override
    public Result<List<Order>> customerOrders(Long customerId) {
        // 根据客户ID查询订单列表并返回
        return Result.success(baseMapper.findByCustomerId(customerId));
    }

    /**
     * 查询押金不足订单列表
     * @return 押金低于房费的在住订单列表
     */
    @Override
    public Result<List<Order>> lowDepositOrders() {
        // 查询押金不足订单并返回
        return Result.success(baseMapper.findLowDepositOrders());
    }

    /**
     * 推荐可用房间
     * 根据客户偏好（楼层偏好）智能推荐可用房间并按匹配度排序
     * @param customerId 客户ID（可选，用于获取偏好）
     * @param checkIn 入住时间
     * @param checkOut 退房时间
     * @return 推荐房间列表（带有效价格、匹配分数）
     */
    @Override
    public Result<List<Map<String, Object>>> recommendRooms(Long customerId, LocalDateTime checkIn, LocalDateTime checkOut) {
        // 格式化入住时间
        String ci = checkIn.format(DATETIME_FMT);
        // 格式化退房时间
        String co = checkOut.format(DATETIME_FMT);
        // 查询该时间段可用房间列表
        List<Room> available = baseMapper.findAvailableRooms(ci, co);
        // 查询客户信息（如果指定了客户ID）
        Customer customer = customerId != null ? customerMapper.selectById(customerId) : null;

        // 初始化楼层偏好
        String prefFloor = null;
        // 客户存在且有标签，解析楼层偏好
        if (customer != null && customer.getTags() != null) {
            String tags = customer.getTags();
            // 标签包含"高楼层"，偏好高楼层
            if (tags.contains("高楼层")) prefFloor = "high";
            // 标签包含"低楼层"，偏好低楼层
            else if (tags.contains("低楼层")) prefFloor = "low";
        }

        // 构建推荐结果列表
        List<Map<String, Object>> result = new ArrayList<>();
        // 遍历可用房间，计算推荐分数
        for (Room r : available) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("roomNo", r.getRoomNo());
            item.put("floor", r.getFloor());
            item.put("price", r.getPrice());
            item.put("typeName", r.getTypeName());
            item.put("remark", r.getRemark());

            // 计算客户实际房价（考虑VIP折扣）
            BigDecimal effectivePrice = getEffectivePrice(r, null, customer, checkIn);
            item.put("effectivePrice", effectivePrice);

            // 初始化推荐分数
            int score = 0;
            // 根据楼层偏好加分
            if (prefFloor != null && r.getFloor() != null) {
                // 偏好高楼层且房间在5楼及以上，加10分
                if ("high".equals(prefFloor) && r.getFloor() >= 5) score += 10;
                // 偏好低楼层且房间在2楼及以下，加10分
                if ("low".equals(prefFloor) && r.getFloor() <= 2) score += 10;
            }
            item.put("score", score);
            result.add(item);
        }
        // 按推荐分数降序排序（分数高的排前面）
        result.sort((a, b) -> Integer.compare((int) b.get("score"), (int) a.get("score")));
        // 返回推荐结果
        return Result.success(result);
    }

    /**
     * 自动取消超时未入住的预约订单
     * 定时任务调用：超过2小时未办理入住的预约订单自动取消
     * 事务边界：每个订单的取消在独立事务中
     * @return 取消的订单数量
     */
    @Override
    @Transactional // 开启事务
    public int cancelExpiredPendingOrders() {
        // 计算超时阈值：当前时间往前推2小时
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);
        // 查询超过阈值时间未入住的预约订单
        List<Order> expiredOrders = baseMapper.findExpiredPendingOrders(threshold.format(DATETIME_FMT));
        // 初始化取消计数器
        int count = 0;
        // 遍历超时订单，逐个取消
        for (Order order : expiredOrders) {
            // 设置订单状态为已取消
            order.setStatus(OrderStatus.CANCELLED);
            // 更新订单状态
            updateById(order);
            // 计数+1
            count++;
        }
        // 返回取消数量
        return count;
    }

    /**
     * 订单营收统计
     * 按日期范围统计每日订单数、房费、消费、总额，计算汇总数据
     * @param range 时间范围：week(7天)/month(30天)/数字天数
     * @param startTime 自定义开始日期（yyyy-MM-dd）
     * @param endTime 自定义结束日期（yyyy-MM-dd）
     * @return 统计数据（日期列表、金额列表、汇总数据）
     */
    @Override
    public Result<Map<String, Object>> stats(String range, String startTime, String endTime) {
        // 声明查询开始日期字符串
        String start;
        // 声明查询结束日期字符串
        String end;
        // 判断是否使用自定义日期范围
        boolean customRange = (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty());
        if (customRange) {
            // 使用自定义开始日期
            start = startTime;
            // 解析自定义结束日期
            LocalDate endDateParsed = LocalDate.parse(endTime, DATE_FMT);
            // 结束日期+1天（因为统计含结束日全天）
            end = endDateParsed.plusDays(1).format(DATE_FMT);
        } else {
            // 天数变量
            int days;
            // 根据range参数确定天数
            if ("month".equals(range)) days = 30;
            else if ("week".equals(range)) days = 7;
            else {
                try { 
                    // 尝试解析为数字天数
                    days = Integer.parseInt(range); 
                } catch (NumberFormatException e) { 
                    return Result.error("Invalid range"); 
                }
            }
            // 天数不能小于1
            if (days < 1) return Result.error("Invalid range");
            // 结束日期为今天
            LocalDate endDate = LocalDate.now();
            // 开始日期 = 结束日期 - (天数-1)
            LocalDate startDate = endDate.minusDays(days - 1);
            // 格式化开始日期
            start = startDate.format(DATE_FMT);
            // 结束日期+1天
            end = endDate.plusDays(1).format(DATE_FMT);
        }

        // 查询日期范围内的数据库统计结果
        List<Map<String, Object>> dbResult = baseMapper.statsByDateRange(start, end);
        // 构建每日总金额Map
        Map<String, BigDecimal> amountMap = new HashMap<>();
        // 构建每日房费Map
        Map<String, BigDecimal> roomAmountMap = new HashMap<>();
        // 构建每日额外消费Map
        Map<String, BigDecimal> extraAmountMap = new HashMap<>();
        // 构建每日订单数Map
        Map<String, Integer> countMap = new HashMap<>();
        // 遍历数据库结果，填充各个Map
        for (Map<String, Object> item : dbResult) {
            // 获取日期字符串
            String date = item.get("date").toString();
            // 获取总金额
            BigDecimal amt = new BigDecimal(item.get("amount").toString());
            // 获取房费（为空默认0）
            BigDecimal roomAmt = item.get("roomAmount") != null ? new BigDecimal(item.get("roomAmount").toString()) : BigDecimal.ZERO;
            // 获取额外消费（为空默认0）
            BigDecimal extraAmt = item.get("extraAmount") != null ? new BigDecimal(item.get("extraAmount").toString()) : BigDecimal.ZERO;
            amountMap.put(date, amt);
            roomAmountMap.put(date, roomAmt);
            extraAmountMap.put(date, extraAmt);
            countMap.put(date, ((Number) item.get("count")).intValue());
        }

        // 构建图表日期列表（显示格式MM-dd）
        List<String> dates = new ArrayList<>();
        // 构建每日总金额列表
        List<BigDecimal> amounts = new ArrayList<>();
        // 构建每日房费列表
        List<BigDecimal> roomAmounts = new ArrayList<>();
        // 构建每日额外消费列表
        List<BigDecimal> extraAmounts = new ArrayList<>();
        // 初始化总金额为0
        BigDecimal totalAmount = BigDecimal.ZERO;
        // 初始化总房费为0
        BigDecimal totalRoom = BigDecimal.ZERO;
        // 初始化总消费为0
        BigDecimal totalExtra = BigDecimal.ZERO;
        // 初始化总订单数为0
        int totalCount = 0;

        // 从开始日期遍历到结束日期（前闭后开）
        LocalDate current = LocalDate.parse(start, DATE_FMT);
        LocalDate endDate = LocalDate.parse(end, DATE_FMT);
        while (current.isBefore(endDate)) {
            // 获取日期key（yyyy-MM-dd格式）
            String dateKey = current.format(DATE_FMT);
            // 添加显示用日期（MM-dd格式）
            dates.add(current.format(DISPLAY_DATE_FMT));
            // 获取当日总金额，无数据则0
            BigDecimal amt = amountMap.getOrDefault(dateKey, BigDecimal.ZERO);
            // 获取当日房费，无数据则0
            BigDecimal roomAmt = roomAmountMap.getOrDefault(dateKey, BigDecimal.ZERO);
            // 获取当日消费，无数据则0
            BigDecimal extraAmt = extraAmountMap.getOrDefault(dateKey, BigDecimal.ZERO);
            // 添加到列表
            amounts.add(amt);
            roomAmounts.add(roomAmt);
            extraAmounts.add(extraAmt);
            // 累加总金额
            totalAmount = totalAmount.add(amt);
            // 累加总房费
            totalRoom = totalRoom.add(roomAmt);
            // 累加总消费
            totalExtra = totalExtra.add(extraAmt);
            // 累加总订单数
            totalCount += countMap.getOrDefault(dateKey, 0);
            // 日期移到下一天
            current = current.plusDays(1);
        }
        // 计算平均客单价（总金额/订单数）
        BigDecimal averageAmount = totalCount > 0 ? totalAmount.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

        // 构建返回统计数据
        Map<String, Object> data = new HashMap<>();
        data.put("dates", dates);
        data.put("amounts", amounts);
        data.put("roomAmounts", roomAmounts);
        data.put("extraAmounts", extraAmounts);
        data.put("totalAmount", totalAmount);
        data.put("totalRoomAmount", totalRoom);
        data.put("totalExtraAmount", totalExtra);
        data.put("totalCount", totalCount);
        data.put("averageAmount", averageAmount);
        // 返回统计结果
        return Result.success(data);
    }

    /**
     * 生成唯一订单号
     * 格式：JC + yyyyMMddHHmmss + 3位序号，synchronized保证并发安全
     * @return 唯一订单号
     */
    private synchronized String generateOrderNo() {
        // 生成前缀：JC + 当前时间戳（精确到秒）
        String prefix = "JC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        // 查询当前前缀下已有多少订单
        int count = baseMapper.countByOrderNoPrefix(prefix);
        // 生成订单号：前缀 + 3位序号（从1开始）
        String orderNo = prefix + String.format("%03d", count + 1);
        // 双重校验：如果订单号已存在（极端并发情况），序号递增直到找到唯一
        while (baseMapper.findByOrderNo(orderNo) != null) {
            count++;
            orderNo = prefix + String.format("%03d", count + 1);
        }
        // 返回唯一订单号
        return orderNo;
    }
}
