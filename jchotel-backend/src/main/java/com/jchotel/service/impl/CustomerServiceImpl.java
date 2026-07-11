package com.jchotel.service.impl;

import com.jchotel.constants.VipConfig;
import com.jchotel.dto.PageQuery;
import com.jchotel.dto.PageResult;
import com.jchotel.entity.Customer;
import com.jchotel.entity.Order;
import com.jchotel.entity.Room;
import com.jchotel.mapper.CustomerMapper;
import com.jchotel.mapper.OrderMapper;
import com.jchotel.mapper.RoomMapper;
import com.jchotel.service.CustomerService;
import com.jchotel.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CustomerServiceImpl implements CustomerService {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private CustomerMapper customerMapper;

    @Autowired
    private OrderMapper orderMapper;

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
    public Result<PageResult<Customer>> list(PageQuery query) {
        initPage(query);
        Long total = customerMapper.count(query);
        List<Customer> list = customerMapper.findList(query);
        PageResult<Customer> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }

    @Override
    public Result<Customer> detail(Long id) {
        Customer customer = customerMapper.findById(id);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        return Result.success(customer);
    }

    @Override
    public Result<String> add(Customer customer) {
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            return Result.error("客户姓名不能为空");
        }
        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        Customer byPhone = customerMapper.findByPhone(customer.getPhone());
        if (byPhone != null) {
            return Result.error("该手机号已存在");
        }
        if (customer.getCheckInCount() == null) {
            customer.setCheckInCount(0);
        }
        if (customer.getVipLevel() == null) {
            customer.setVipLevel(0);
        }
        if (customer.getIsBlacklist() == null) {
            customer.setIsBlacklist(0);
        }
        if (customer.getTotalSpent() == null) {
            customer.setTotalSpent(BigDecimal.ZERO);
        }
        customerMapper.insert(customer);
        return Result.success("新增成功", null);
    }

    @Override
    public Result<String> update(Customer customer) {
        Customer exist = customerMapper.findById(customer.getId());
        if (exist == null) {
            return Result.error("客户不存在");
        }
        if (customer.getPhone() != null && !customer.getPhone().equals(exist.getPhone())) {
            Customer byPhone = customerMapper.findByPhone(customer.getPhone());
            if (byPhone != null) {
                return Result.error("该手机号已被其他客户使用");
            }
        }
        customerMapper.update(customer);
        return Result.success("修改成功", null);
    }

    @Override
    public Result<String> delete(Long id) {
        Customer customer = customerMapper.findById(id);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        int activeOrders = orderMapper.countActiveByCustomerId(id);
        if (activeOrders > 0) {
            return Result.error("该客户存在未完成订单，无法删除");
        }
        customerMapper.deleteById(id);
        return Result.success("删除成功", null);
    }

    @Override
    public Result<String> addToBlacklist(Long id, String reason) {
        Customer customer = customerMapper.findById(id);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        if (customer.getIsBlacklist() != null && customer.getIsBlacklist() == 1) {
            return Result.error("该客户已在黑名单中");
        }
        int activeOrders = orderMapper.countActiveByCustomerId(id);
        if (activeOrders > 0) {
            return Result.error("该客户存在未完成订单，无法加入黑名单");
        }
        customerMapper.updateBlacklist(id, 1, reason);
        return Result.success("已加入黑名单", null);
    }

    @Override
    public Result<String> removeFromBlacklist(Long id) {
        Customer customer = customerMapper.findById(id);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        if (customer.getIsBlacklist() == null || customer.getIsBlacklist() == 0) {
            return Result.error("该客户不在黑名单中");
        }
        customerMapper.updateBlacklist(id, 0, null);
        return Result.success("已移出黑名单", null);
    }

    @Override
    public Result<List<Customer>> birthdayList() {
        List<Customer> list = customerMapper.findBirthdayCustomers(LocalDate.now());
        return Result.success(list);
    }

    @Override
    public Result<List<Customer>> blacklist() {
        List<Customer> list = customerMapper.findBlacklistCustomers();
        return Result.success(list);
    }

    @Override
    @Transactional
    public Result<Integer> upgradeVipLevel(Long customerId) {
        Customer customer = customerMapper.findById(customerId);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        int completedOrders = orderMapper.countCompletedByCustomerId(customerId);
        int newLevel = calculateVipLevel(completedOrders);
        int oldLevel = customer.getVipLevel() != null ? customer.getVipLevel() : 0;
        if (newLevel > oldLevel) {
            customerMapper.updateVipLevel(customerId, newLevel);
            return Result.success("会员等级已升级", newLevel);
        }
        return Result.success("无需升级", oldLevel);
    }

    @Override
    @Transactional
    public Result<String> addSpent(Long customerId, BigDecimal amount, LocalDateTime stayTime) {
        Customer customer = customerMapper.findById(customerId);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        customerMapper.addSpent(customerId, amount);
        customerMapper.increaseCheckInCount(customerId);
        upgradeVipLevel(customerId);
        return Result.success("消费记录已更新", null);
    }

    @Override
    public Result<List<Room>> recommendRooms(Long typeId, LocalDateTime checkIn, LocalDateTime checkOut) {
        String startStr = checkIn.format(DATETIME_FMT);
        String endStr = checkOut.format(DATETIME_FMT);
        List<Room> rooms = roomMapper.findAvailableRooms(typeId, startStr, endStr);
        return Result.success(rooms);
    }

    @Override
    public Result<Map<String, Object>> getCustomerStats(Long customerId) {
        Customer customer = customerMapper.findById(customerId);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("customer", customer);
        int completedOrders = orderMapper.countCompletedByCustomerId(customerId);
        BigDecimal totalSpent = orderMapper.totalSpentByCustomerId(customerId);
        int activeOrders = orderMapper.countActiveByCustomerId(customerId);
        stats.put("completedOrders", completedOrders);
        stats.put("totalSpent", totalSpent);
        stats.put("activeOrders", activeOrders);
        stats.put("vipLevel", customer.getVipLevel() != null ? customer.getVipLevel() : 0);
        stats.put("vipName", getVipName(customer.getVipLevel() != null ? customer.getVipLevel() : 0));
        stats.put("nextLevelInfo", getNextLevelInfo(completedOrders));
        List<Order> orders = orderMapper.findByCustomerId(customerId);
        stats.put("orders", orders);
        return Result.success(stats);
    }

    private int calculateVipLevel(int checkInCount) {
        if (checkInCount >= VipConfig.DIAMOND_THRESHOLD) {
            return 3;
        } else if (checkInCount >= VipConfig.GOLD_THRESHOLD) {
            return 2;
        } else if (checkInCount >= VipConfig.SILVER_THRESHOLD) {
            return 1;
        }
        return 0;
    }

    private String getVipName(int level) {
        switch (level) {
            case 1: return "银卡会员";
            case 2: return "金卡会员";
            case 3: return "钻石会员";
            default: return "普通会员";
        }
    }

    private Map<String, Object> getNextLevelInfo(int completedCount) {
        Map<String, Object> info = new HashMap<>();
        if (completedCount < VipConfig.SILVER_THRESHOLD) {
            info.put("nextLevel", "银卡会员");
            info.put("needCount", VipConfig.SILVER_THRESHOLD - completedCount);
        } else if (completedCount < VipConfig.GOLD_THRESHOLD) {
            info.put("nextLevel", "金卡会员");
            info.put("needCount", VipConfig.GOLD_THRESHOLD - completedCount);
        } else if (completedCount < VipConfig.DIAMOND_THRESHOLD) {
            info.put("nextLevel", "钻石会员");
            info.put("needCount", VipConfig.DIAMOND_THRESHOLD - completedCount);
        } else {
            info.put("nextLevel", "已是最高等级");
            info.put("needCount", 0);
        }
        return info;
    }
}
