package com.jchotel.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.jchotel.constants.*;
import com.jchotel.dto.ChangeRoomDTO;
import com.jchotel.dto.CheckinDTO;
import com.jchotel.dto.CheckoutDTO;
import com.jchotel.dto.ExtendStayDTO;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.*;
import com.jchotel.mapper.*;
import com.jchotel.service.CleaningTaskService;
import com.jchotel.service.OrderService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DISPLAY_DATE_FMT = DateTimeFormatter.ofPattern("MM-dd");
    private static final int CHECKIN_TOLERANCE_MINUTES = 15;
    private static final int IMMEDIATE_CHECKIN_WINDOW_MINUTES = 30;

    @Autowired
    private RoomMapper roomMapper;
    @Autowired
    private RoomTypeMapper roomTypeMapper;
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private RoomChangeLogMapper roomChangeLogMapper;
    @Autowired
    private CleaningTaskService cleaningTaskService;
    @Autowired
    private PaymentMapper paymentMapper;

    private void initPage(PageQuery query) {
        if (query.getPage() == null || query.getPage() < 1) query.setPage(1);
        if (query.getSize() == null || query.getSize() < 1) query.setSize(10);
        query.setOffset((query.getPage() - 1) * query.getSize());
    }

    private BigDecimal calculateVipPrice(BigDecimal originalPrice, Integer vipLevel) {
        if (vipLevel == null || vipLevel == 0) return originalPrice;
        BigDecimal discount;
        switch (vipLevel) {
            case 1: discount = VipConfig.SILVER_DISCOUNT; break;
            case 2: discount = VipConfig.GOLD_DISCOUNT; break;
            case 3: discount = VipConfig.DIAMOND_DISCOUNT; break;
            default: discount = BigDecimal.ONE;
        }
        return originalPrice.multiply(discount).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal getEffectivePrice(Room room, RoomType roomType, Customer customer, LocalDateTime date) {
        if (room == null || room.getPrice() == null) return BigDecimal.ZERO;
        BigDecimal basePrice = room.getPrice();
        DayOfWeek dow = date.getDayOfWeek();
        boolean isWeekend = (dow == DayOfWeek.FRIDAY || dow == DayOfWeek.SATURDAY);
        if (isWeekend && roomType != null && roomType.getWeekendPrice() != null
                && roomType.getWeekendPrice().compareTo(BigDecimal.ZERO) > 0) {
            basePrice = roomType.getWeekendPrice();
        }
        if (customer != null && customer.getVipLevel() != null && customer.getVipLevel() > 0) {
            basePrice = calculateVipPrice(basePrice, customer.getVipLevel());
        }
        return basePrice;
    }

    private int calculateNights(LocalDateTime checkIn, LocalDateTime checkOut) {
        long minutes = ChronoUnit.MINUTES.between(checkIn, checkOut);
        int nights = (int) Math.ceil(minutes / (24.0 * 60.0));
        return Math.max(nights, 1);
    }

    private void upgradeVipIfNeeded(Customer customer) {
        int count = customer.getCheckInCount() == null ? 0 : customer.getCheckInCount();
        int newLevel = 0;
        if (count >= VipConfig.DIAMOND_THRESHOLD) newLevel = 3;
        else if (count >= VipConfig.GOLD_THRESHOLD) newLevel = 2;
        else if (count >= VipConfig.SILVER_THRESHOLD) newLevel = 1;
        if (newLevel > (customer.getVipLevel() == null ? 0 : customer.getVipLevel())) {
            customer.setVipLevel(newLevel);
            Customer upd = new Customer();
            upd.setId(customer.getId());
            upd.setVipLevel(newLevel);
            customerMapper.updateById(upd);
        }
    }

    @Override
    public Result<PageResult<Order>> list(PageQuery query) {
        initPage(query);
        Long total = baseMapper.count(query);
        List<Order> list = baseMapper.findList(query);
        PageResult<Order> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }

    @Override
    public Result<Order> detail(Long id) {
        Order order = baseMapper.findDetailById(id);
        if (order == null) return Result.error("订单不存在");
        return Result.success(order);
    }

    @Override
    @Transactional
    public Result<Map<String, Object>> checkin(CheckinDTO checkinDTO, HttpServletRequest request) {
        LocalDateTime checkInTime = checkinDTO.getCheckInTime();
        LocalDateTime expectedCheckOutTime = checkinDTO.getExpectedCheckOutTime();
        LocalDateTime now = LocalDateTime.now();

        if (checkInTime.isBefore(now.minusMinutes(CHECKIN_TOLERANCE_MINUTES))) {
            return Result.error("入住时间不能早于当前时间（允许15分钟误差）");
        }
        if (!expectedCheckOutTime.isAfter(checkInTime)) {
            return Result.error("预计退房时间必须晚于入住时间");
        }

        Room room = roomMapper.selectById(checkinDTO.getRoomId());
        if (room == null) return Result.error("客房不存在");
        if (RoomStatus.MAINTENANCE.equals(room.getStatus())) return Result.error("维修中的房间不可预约");
        if (RoomStatus.CLEANING.equals(room.getStatus()) || RoomStatus.DIRTY.equals(room.getStatus())) {
            return Result.error("该房间正在清扫中，暂时不可入住");
        }

        Customer customer = customerMapper.selectById(checkinDTO.getCustomerId());
        if (customer == null) return Result.error("客户不存在");
        if (customer.getIsBlacklist() != null && customer.getIsBlacklist() == 1) {
            return Result.error("该客户在黑名单中：" + (customer.getBlacklistReason() != null ? customer.getBlacklistReason() : "禁止入住"));
        }

        if (checkinDTO.getDeposit() == null || checkinDTO.getDeposit().compareTo(BigDecimal.ZERO) < 0) {
            return Result.error("押金不能为负数");
        }

        String checkInStr = checkInTime.format(DATETIME_FMT);
        String expectedCheckOutStr = expectedCheckOutTime.format(DATETIME_FMT);

        int conflicts = baseMapper.countConflictOrders(checkinDTO.getRoomId(), checkInStr, expectedCheckOutStr);
        if (conflicts > 0) {
            return Result.error("该房间在所选时间段已被预约或入住，请选择其他房间或时间段");
        }

        int nights = calculateNights(checkInTime, expectedCheckOutTime);
        RoomType roomType = null;
        if (room.getTypeId() != null) {
            roomType = roomTypeMapper.selectById(room.getTypeId());
        }

        BigDecimal expectedRoomAmount = BigDecimal.ZERO;
        LocalDateTime dayCursor = checkInTime;
        for (int i = 0; i < nights; i++) {
            expectedRoomAmount = expectedRoomAmount.add(getEffectivePrice(room, roomType, customer, dayCursor));
            dayCursor = dayCursor.plusDays(1);
        }
        expectedRoomAmount = expectedRoomAmount.setScale(2, RoundingMode.HALF_UP);

        Long userId = (Long) request.getAttribute("userId");

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setCustomerId(checkinDTO.getCustomerId());
        order.setRoomId(checkinDTO.getRoomId());
        order.setUserId(userId);
        order.setCheckInTime(checkInTime);
        order.setExpectedCheckOutTime(expectedCheckOutTime);
        order.setDeposit(checkinDTO.getDeposit());
        order.setDepositRefunded(0);
        order.setRoomAmount(expectedRoomAmount);
        order.setExtraAmount(BigDecimal.ZERO);
        order.setTotalAmount(expectedRoomAmount);
        order.setRoomChanged(0);
        order.setChannel("front");
        order.setRemark(checkinDTO.getRemark());

        Map<String, Object> data = new HashMap<>();
        long minutesToCheckIn = ChronoUnit.MINUTES.between(now, checkInTime);
        if (minutesToCheckIn <= IMMEDIATE_CHECKIN_WINDOW_MINUTES) {
            order.setStatus(OrderStatus.CHECKED_IN);
            roomMapper.updateStatus(room.getId(), RoomStatus.OCCUPIED);
            customerMapper.increaseCheckInCount(customer.getId());
            save(order);
            customer.setCheckInCount((customer.getCheckInCount() == null ? 0 : customer.getCheckInCount()) + 1);
            upgradeVipIfNeeded(customer);

            if (checkinDTO.getDeposit().compareTo(BigDecimal.ZERO) > 0) {
                Payment depositPay = new Payment();
                depositPay.setOrderId(order.getId());
                depositPay.setOrderNo(order.getOrderNo());
                depositPay.setPaymentMethod("cash");
                depositPay.setAmount(checkinDTO.getDeposit());
                depositPay.setType(PaymentType.DEPOSIT);
                depositPay.setRemark("入住押金");
                depositPay.setOperatorId(userId);
                depositPay.setCreateTime(now);
                paymentMapper.insert(depositPay);
            }

            data.put("orderId", order.getId());
            data.put("orderNo", order.getOrderNo());
            data.put("status", OrderStatus.CHECKED_IN);
            return Result.success("入住办理成功", data);
        } else {
            order.setStatus(OrderStatus.PENDING);
            save(order);
            data.put("orderId", order.getId());
            data.put("orderNo", order.getOrderNo());
            data.put("status", OrderStatus.PENDING);
            return Result.success("预约成功", data);
        }
    }

    @Override
    @Transactional
    public Result<Map<String, Object>> settlePending(Long id, HttpServletRequest request) {
        Order order = getById(id);
        if (order == null) return Result.error("订单不存在");
        if (!OrderStatus.PENDING.equals(order.getStatus())) return Result.error("只有待入住订单才能办理入住");

        Room room = roomMapper.selectById(order.getRoomId());
        if (room == null) return Result.error("客房不存在");
        if (RoomStatus.MAINTENANCE.equals(room.getStatus())) return Result.error("维修中的房间无法办理入住");

        Customer customer = customerMapper.selectById(order.getCustomerId());
        if (customer == null) return Result.error("客户不存在");
        if (customer.getIsBlacklist() != null && customer.getIsBlacklist() == 1) {
            return Result.error("该客户在黑名单中，无法办理入住");
        }

        LocalDateTime now = LocalDateTime.now();
        order.setCheckInTime(now);
        order.setStatus(OrderStatus.CHECKED_IN);

        int settleNights = calculateNights(now, order.getExpectedCheckOutTime());
        RoomType settleRoomType = null;
        if (room.getTypeId() != null) {
            settleRoomType = roomTypeMapper.selectById(room.getTypeId());
        }
        BigDecimal settleRoomAmount = BigDecimal.ZERO;
        LocalDateTime settleDayCursor = now;
        for (int i = 0; i < settleNights; i++) {
            settleRoomAmount = settleRoomAmount.add(getEffectivePrice(room, settleRoomType, customer, settleDayCursor));
            settleDayCursor = settleDayCursor.plusDays(1);
        }
        settleRoomAmount = settleRoomAmount.setScale(2, RoundingMode.HALF_UP);
        order.setRoomAmount(settleRoomAmount);
        order.setTotalAmount(settleRoomAmount);

        updateById(order);

        roomMapper.updateStatus(room.getId(), RoomStatus.OCCUPIED);
        customerMapper.increaseCheckInCount(customer.getId());
        customer.setCheckInCount((customer.getCheckInCount() == null ? 0 : customer.getCheckInCount()) + 1);
        upgradeVipIfNeeded(customer);

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderNo", order.getOrderNo());
        return Result.success("办理入住成功", data);
    }

    @Override
    @Transactional
    public Result<Map<String, Object>> checkout(Long id, CheckoutDTO checkoutDTO) {
        Order order = getById(id);
        if (order == null) return Result.error("订单不存在");
        if (!OrderStatus.CHECKED_IN.equals(order.getStatus())) return Result.error("只有已入住订单才能退房");

        LocalDateTime actualCheckOutTime = checkoutDTO.getActualCheckOutTime();
        LocalDateTime now = LocalDateTime.now();
        if (actualCheckOutTime == null) actualCheckOutTime = now;
        if (actualCheckOutTime.isAfter(now)) return Result.error("实际退房时间不能晚于当前时间");
        if (!actualCheckOutTime.isAfter(order.getCheckInTime())) return Result.error("退房时间必须晚于入住时间");

        int nights = calculateNights(order.getCheckInTime(), actualCheckOutTime);
        Room room = roomMapper.selectById(order.getRoomId());
        if (room == null) return Result.error("房间信息不存在，无法退房");
        Customer customer = customerMapper.selectById(order.getCustomerId());
        RoomType roomType = null;
        if (room.getTypeId() != null) {
            roomType = roomTypeMapper.selectById(room.getTypeId());
        }

        BigDecimal roomAmount = BigDecimal.ZERO;
        LocalDateTime dayCursor = order.getCheckInTime();
        for (int i = 0; i < nights; i++) {
            roomAmount = roomAmount.add(getEffectivePrice(room, roomType, customer, dayCursor));
            dayCursor = dayCursor.plusDays(1);
        }
        roomAmount = roomAmount.setScale(2, RoundingMode.HALF_UP);

        BigDecimal extraAmount = orderItemMapper.sumByOrderId(order.getId());
        if (extraAmount == null) extraAmount = BigDecimal.ZERO;

        BigDecimal totalAmount = roomAmount.add(extraAmount);
        BigDecimal deposit = order.getDeposit() == null ? BigDecimal.ZERO : order.getDeposit();
        BigDecimal balance = totalAmount.subtract(deposit);

        order.setActualCheckOutTime(actualCheckOutTime);
        order.setRoomAmount(roomAmount);
        order.setExtraAmount(extraAmount);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.CHECKED_OUT);
        updateById(order);

        roomMapper.updateStatus(room.getId(), RoomStatus.DIRTY);
        baseMapper.updateCustomerSpending(order.getCustomerId(), totalAmount);
        cleaningTaskService.createFromCheckout(room.getId(), room.getRoomNo(), order.getId(), order.getRemark());

        BigDecimal avgPrice = nights > 0 ? roomAmount.divide(BigDecimal.valueOf(nights), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

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
        data.put("needPay", balance.compareTo(BigDecimal.ZERO) > 0);
        data.put("needRefund", balance.compareTo(BigDecimal.ZERO) < 0);
        data.put("items", orderItemMapper.findByOrderId(order.getId()));
        return Result.success("退房成功", data);
    }

    @Override
    @Transactional
    public Result<String> cancel(Long id) {
        Order order = getById(id);
        if (order == null) return Result.error("订单不存在");
        if (!OrderStatus.PENDING.equals(order.getStatus())) return Result.error("只有待入住订单才能取消");
        order.setStatus(OrderStatus.CANCELLED);
        updateById(order);
        return Result.success("取消成功", null);
    }

    @Override
    @Transactional
    public Result<Map<String, Object>> extendStay(ExtendStayDTO dto, HttpServletRequest request) {
        Order order = getById(dto.getOrderId());
        if (order == null) return Result.error("订单不存在");
        if (!OrderStatus.CHECKED_IN.equals(order.getStatus())) return Result.error("只有在住订单才能续住");

        LocalDateTime newCheckOut = dto.getNewCheckOutTime();
        if (!newCheckOut.isAfter(order.getExpectedCheckOutTime())) {
            return Result.error("新退房时间必须晚于原预计退房时间");
        }

        String newOutStr = newCheckOut.format(DATETIME_FMT);
        int conflicts = baseMapper.countConflictOrdersExcludeId(order.getRoomId(),
                order.getExpectedCheckOutTime().format(DATETIME_FMT), newOutStr, order.getId());
        if (conflicts > 0) {
            return Result.error("续住时段房间已被预约，无法续住");
        }

        int extraNights = calculateNights(order.getExpectedCheckOutTime(), newCheckOut);
        Room room = roomMapper.selectById(order.getRoomId());
        Customer customer = customerMapper.selectById(order.getCustomerId());
        RoomType roomType = null;
        if (room != null && room.getTypeId() != null) {
            roomType = roomTypeMapper.selectById(room.getTypeId());
        }

        BigDecimal extraRoomAmount = BigDecimal.ZERO;
        LocalDateTime dayCursor = order.getExpectedCheckOutTime();
        for (int i = 0; i < extraNights; i++) {
            extraRoomAmount = extraRoomAmount.add(getEffectivePrice(room, roomType, customer, dayCursor));
            dayCursor = dayCursor.plusDays(1);
        }
        extraRoomAmount = extraRoomAmount.setScale(2, RoundingMode.HALF_UP);

        BigDecimal currentRoomAmount = order.getRoomAmount() != null ? order.getRoomAmount() : BigDecimal.ZERO;
        BigDecimal currentExtraAmount = order.getExtraAmount() != null ? order.getExtraAmount() : BigDecimal.ZERO;
        order.setExpectedCheckOutTime(newCheckOut);
        order.setRoomAmount(currentRoomAmount.add(extraRoomAmount).setScale(2, RoundingMode.HALF_UP));
        order.setTotalAmount(order.getRoomAmount().add(currentExtraAmount).setScale(2, RoundingMode.HALF_UP));
        updateById(order);

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderNo", order.getOrderNo());
        data.put("newCheckOutTime", newCheckOut);
        data.put("extraNights", extraNights);
        data.put("extraRoomAmount", extraRoomAmount);
        return Result.success("续住办理成功，新增" + extraNights + "晚", data);
    }

    @Override
    @Transactional
    public Result<Map<String, Object>> changeRoom(ChangeRoomDTO dto, HttpServletRequest request) {
        Order order = getById(dto.getOrderId());
        if (order == null) return Result.error("订单不存在");
        if (!OrderStatus.CHECKED_IN.equals(order.getStatus())) return Result.error("只有在住订单才能换房");

        Room toRoom = roomMapper.selectById(dto.getToRoomId());
        if (toRoom == null) return Result.error("目标房间不存在");
        if (!RoomStatus.IDLE.equals(toRoom.getStatus())) return Result.error("目标房间当前不可用");

        Room fromRoom = roomMapper.selectById(order.getRoomId());
        if (fromRoom.getId().equals(toRoom.getId())) return Result.error("不能换入同一房间");

        String nowStr = LocalDateTime.now().format(DATETIME_FMT);
        String outStr = order.getExpectedCheckOutTime().format(DATETIME_FMT);
        int conflicts = baseMapper.countConflictOrders(toRoom.getId(), nowStr, outStr);
        if (conflicts > 0) return Result.error("目标房间在剩余时段已被占用");

        Customer changeCustomer = customerMapper.selectById(order.getCustomerId());
        RoomType fromRoomType = null;
        RoomType toRoomType = null;
        if (fromRoom.getTypeId() != null) fromRoomType = roomTypeMapper.selectById(fromRoom.getTypeId());
        if (toRoom.getTypeId() != null) toRoomType = roomTypeMapper.selectById(toRoom.getTypeId());

        int remainingDays = calculateNights(LocalDateTime.now(), order.getExpectedCheckOutTime());
        BigDecimal remainingFromAmount = BigDecimal.ZERO;
        BigDecimal remainingToAmount = BigDecimal.ZERO;
        LocalDateTime changeDayCursor = LocalDateTime.now();
        for (int i = 0; i < remainingDays; i++) {
            remainingFromAmount = remainingFromAmount.add(getEffectivePrice(fromRoom, fromRoomType, changeCustomer, changeDayCursor));
            remainingToAmount = remainingToAmount.add(getEffectivePrice(toRoom, toRoomType, changeCustomer, changeDayCursor));
            changeDayCursor = changeDayCursor.plusDays(1);
        }
        BigDecimal totalPriceDiff = remainingToAmount.subtract(remainingFromAmount).setScale(2, RoundingMode.HALF_UP);

        RoomChangeLog log = new RoomChangeLog();
        log.setOrderId(order.getId());
        log.setOrderNo(order.getOrderNo());
        log.setFromRoomId(fromRoom.getId());
        log.setFromRoomNo(fromRoom.getRoomNo());
        log.setToRoomId(toRoom.getId());
        log.setToRoomNo(toRoom.getRoomNo());
        log.setReason(dto.getReason());
        log.setPriceDiff(totalPriceDiff);
        Long userId = (Long) request.getAttribute("userId");
        log.setOperatorId(userId);
        log.setCreateTime(LocalDateTime.now());
        roomChangeLogMapper.insert(log);

        roomMapper.updateStatus(fromRoom.getId(), RoomStatus.DIRTY);
        roomMapper.updateStatus(toRoom.getId(), RoomStatus.OCCUPIED);
        cleaningTaskService.createFromCheckout(fromRoom.getId(), fromRoom.getRoomNo(), order.getId(), "换房清扫");

        order.setOriginalRoomId(fromRoom.getId());
        order.setRoomId(toRoom.getId());
        order.setRoomChanged(1);
        BigDecimal changeCurrentRoomAmount = order.getRoomAmount() != null ? order.getRoomAmount() : BigDecimal.ZERO;
        BigDecimal changeCurrentExtraAmount = order.getExtraAmount() != null ? order.getExtraAmount() : BigDecimal.ZERO;
        order.setRoomAmount(changeCurrentRoomAmount.add(totalPriceDiff).setScale(2, RoundingMode.HALF_UP));
        order.setTotalAmount(order.getRoomAmount().add(changeCurrentExtraAmount).setScale(2, RoundingMode.HALF_UP));
        updateById(order);

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("fromRoomNo", fromRoom.getRoomNo());
        data.put("toRoomNo", toRoom.getRoomNo());
        data.put("priceDiff", totalPriceDiff);
        data.put("remainingDays", remainingDays);
        String msg = "换房成功：" + fromRoom.getRoomNo() + " → " + toRoom.getRoomNo();
        if (totalPriceDiff.compareTo(BigDecimal.ZERO) > 0) msg += "，需补差价¥" + totalPriceDiff;
        else if (totalPriceDiff.compareTo(BigDecimal.ZERO) < 0) msg += "，退还差价¥" + totalPriceDiff.abs();
        return Result.success(msg, data);
    }

    @Override
    public Result<List<Order>> todayArrivals() {
        return Result.success(baseMapper.findTodayArrivals());
    }

    @Override
    public Result<List<Order>> todayDepartures() {
        return Result.success(baseMapper.findTodayDepartures());
    }

    @Override
    public Result<List<Order>> customerOrders(Long customerId) {
        return Result.success(baseMapper.findByCustomerId(customerId));
    }

    @Override
    public Result<List<Order>> lowDepositOrders() {
        return Result.success(baseMapper.findLowDepositOrders());
    }

    @Override
    public Result<List<Map<String, Object>>> recommendRooms(Long customerId, LocalDateTime checkIn, LocalDateTime checkOut) {
        String ci = checkIn.format(DATETIME_FMT);
        String co = checkOut.format(DATETIME_FMT);
        List<Room> available = baseMapper.findAvailableRooms(ci, co);
        Customer customer = customerId != null ? customerMapper.selectById(customerId) : null;

        String prefFloor = null;
        if (customer != null && customer.getTags() != null) {
            String tags = customer.getTags();
            if (tags.contains("高楼层")) prefFloor = "high";
            else if (tags.contains("低楼层")) prefFloor = "low";
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Room r : available) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", r.getId());
            item.put("roomNo", r.getRoomNo());
            item.put("floor", r.getFloor());
            item.put("price", r.getPrice());
            item.put("typeName", r.getTypeName());
            item.put("remark", r.getRemark());

            BigDecimal effectivePrice = getEffectivePrice(r, null, customer, checkIn);
            item.put("effectivePrice", effectivePrice);

            int score = 0;
            if (prefFloor != null && r.getFloor() != null) {
                if ("high".equals(prefFloor) && r.getFloor() >= 5) score += 10;
                if ("low".equals(prefFloor) && r.getFloor() <= 2) score += 10;
            }
            item.put("score", score);
            result.add(item);
        }
        result.sort((a, b) -> Integer.compare((int) b.get("score"), (int) a.get("score")));
        return Result.success(result);
    }

    @Override
    @Transactional
    public int cancelExpiredPendingOrders() {
        LocalDateTime threshold = LocalDateTime.now().minusHours(2);
        List<Order> expiredOrders = baseMapper.findExpiredPendingOrders(threshold.format(DATETIME_FMT));
        int count = 0;
        for (Order order : expiredOrders) {
            order.setStatus(OrderStatus.CANCELLED);
            updateById(order);
            count++;
        }
        return count;
    }

    @Override
    public Result<Map<String, Object>> stats(String range, String startTime, String endTime) {
        String start;
        String end;
        boolean customRange = (startTime != null && !startTime.isEmpty() && endTime != null && !endTime.isEmpty());
        if (customRange) {
            start = startTime;
            LocalDate endDateParsed = LocalDate.parse(endTime, DATE_FMT);
            end = endDateParsed.plusDays(1).format(DATE_FMT);
        } else {
            int days;
            if ("month".equals(range)) days = 30;
            else if ("week".equals(range)) days = 7;
            else {
                try { days = Integer.parseInt(range); } catch (NumberFormatException e) { return Result.error("Invalid range"); }
            }
            if (days < 1) return Result.error("Invalid range");
            LocalDate endDate = LocalDate.now();
            LocalDate startDate = endDate.minusDays(days - 1);
            start = startDate.format(DATE_FMT);
            end = endDate.plusDays(1).format(DATE_FMT);
        }

        List<Map<String, Object>> dbResult = baseMapper.statsByDateRange(start, end);
        Map<String, BigDecimal> roomAmountMap = new HashMap<>();
        Map<String, BigDecimal> extraAmountMap = new HashMap<>();
        Map<String, BigDecimal> amountMap = new HashMap<>();
        Map<String, Integer> countMap = new HashMap<>();
        for (Map<String, Object> item : dbResult) {
            String date = item.get("date").toString();
            BigDecimal amt = new BigDecimal(item.get("amount").toString());
            BigDecimal roomAmt = item.get("roomAmount") != null ? new BigDecimal(item.get("roomAmount").toString()) : BigDecimal.ZERO;
            BigDecimal extraAmt = item.get("extraAmount") != null ? new BigDecimal(item.get("extraAmount").toString()) : BigDecimal.ZERO;
            amountMap.put(date, amt);
            roomAmountMap.put(date, roomAmt);
            extraAmountMap.put(date, extraAmt);
            countMap.put(date, ((Number) item.get("count")).intValue());
        }

        List<String> dates = new ArrayList<>();
        List<BigDecimal> amounts = new ArrayList<>();
        List<BigDecimal> roomAmounts = new ArrayList<>();
        List<BigDecimal> extraAmounts = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalRoom = BigDecimal.ZERO;
        BigDecimal totalExtra = BigDecimal.ZERO;
        int totalCount = 0;

        LocalDate current = LocalDate.parse(start, DATE_FMT);
        LocalDate endDate = LocalDate.parse(end, DATE_FMT);
        while (current.isBefore(endDate)) {
            String dateKey = current.format(DATE_FMT);
            dates.add(current.format(DISPLAY_DATE_FMT));
            BigDecimal amt = amountMap.getOrDefault(dateKey, BigDecimal.ZERO);
            BigDecimal roomAmt = roomAmountMap.getOrDefault(dateKey, BigDecimal.ZERO);
            BigDecimal extraAmt = extraAmountMap.getOrDefault(dateKey, BigDecimal.ZERO);
            amounts.add(amt);
            roomAmounts.add(roomAmt);
            extraAmounts.add(extraAmt);
            totalAmount = totalAmount.add(amt);
            totalRoom = totalRoom.add(roomAmt);
            totalExtra = totalExtra.add(extraAmt);
            totalCount += countMap.getOrDefault(dateKey, 0);
            current = current.plusDays(1);
        }
        BigDecimal averageAmount = totalCount > 0 ? totalAmount.divide(BigDecimal.valueOf(totalCount), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;

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
        return Result.success(data);
    }

    private synchronized String generateOrderNo() {
        String prefix = "JC" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        int count = baseMapper.countByOrderNoPrefix(prefix);
        String orderNo = prefix + String.format("%03d", count + 1);
        while (baseMapper.findByOrderNo(orderNo) != null) {
            count++;
            orderNo = prefix + String.format("%03d", count + 1);
        }
        return orderNo;
    }
}
