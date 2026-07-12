package com.jchotel.service.impl;

// MyBatis-Plus通用服务实现基类
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// VIP等级阈值配置常量类
import com.jchotel.constants.VipConfig;
// 分页查询参数DTO
import com.jchotel.dto.PageQuery;
// 分页结果封装
import com.jchotel.dto.PageResult;
// 客户实体类
import com.jchotel.entity.Customer;
// 订单实体类
import com.jchotel.entity.Order;
// 房间实体类
import com.jchotel.entity.Room;
// 客户数据访问Mapper
import com.jchotel.mapper.CustomerMapper;
// 订单数据访问Mapper
import com.jchotel.mapper.OrderMapper;
// 房间数据访问Mapper
import com.jchotel.mapper.RoomMapper;
// 客户服务接口
import com.jchotel.service.CustomerService;
// 统一响应结果封装
import com.jchotel.utils.Result;
// Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// Spring服务层注解
import org.springframework.stereotype.Service;
// 事务注解
import org.springframework.transaction.annotation.Transactional;

// 高精度小数类型，金额计算
import java.math.BigDecimal;
// 日期类，生日校验
import java.time.LocalDate;
// 日期时间类
import java.time.LocalDateTime;
// 日期格式化类
import java.time.format.DateTimeFormatter;
// HashMap集合
import java.util.HashMap;
// List集合
import java.util.List;
// Map接口
import java.util.Map;

/**
 * 客户管理服务实现类
 * 实现客户CRUD、黑名单管理、VIP等级升级、消费记录统计、生日查询等功能
 * VIP等级根据入住次数自动升级：银卡(5次)、金卡(15次)、钻石卡(30次)
 */
@Service // 标记为Spring服务组件
public class CustomerServiceImpl extends ServiceImpl<CustomerMapper, Customer> implements CustomerService {

    // 日期时间格式化常量：yyyy-MM-dd HH:mm:ss
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired // 自动注入订单Mapper，用于查询客户订单统计
    private OrderMapper orderMapper;

    @Autowired // 自动注入房间Mapper，用于查询可用房间推荐
    private RoomMapper roomMapper;

    /**
     * 初始化分页参数默认值
     * @param query 分页查询参数
     */
    private void initPage(PageQuery query) {
        // 页码默认1
        if (query.getPage() == null || query.getPage() < 1) {
            query.setPage(1);
        }
        // 页大小默认10
        if (query.getSize() == null || query.getSize() < 1) {
            query.setSize(10);
        }
        // 计算SQL查询偏移量
        query.setOffset((query.getPage() - 1) * query.getSize());
    }

    /**
     * 分页查询客户列表
     * @param query 分页查询参数
     * @return 分页客户列表
     */
    @Override
    public Result<PageResult<Customer>> list(PageQuery query) {
        // 初始化分页参数
        initPage(query);
        // 查询总记录数
        Long total = baseMapper.count(query);
        // 查询分页数据
        List<Customer> list = baseMapper.findList(query);
        // 封装分页结果
        PageResult<Customer> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setList(list);
        return Result.success(pageResult);
    }

    /**
     * 查询客户详情
     * @param id 客户ID
     * @return 客户信息
     */
    @Override
    public Result<Customer> detail(Long id) {
        // 根据ID查询客户
        Customer customer = getById(id);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        return Result.success(customer);
    }

    /**
     * 新增客户
     * 校验姓名、手机号必填，手机号唯一，初始化默认值
     * @param customer 客户信息
     * @return 新增结果
     */
    @Override
    public Result<String> add(Customer customer) {
        // 校验姓名非空
        if (customer.getName() == null || customer.getName().trim().isEmpty()) {
            return Result.error("客户姓名不能为空");
        }
        // 校验手机号非空
        if (customer.getPhone() == null || customer.getPhone().trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        // 校验手机号唯一
        Customer byPhone = baseMapper.findByPhone(customer.getPhone());
        if (byPhone != null) {
            return Result.error("该手机号已存在");
        }
        // 初始化入住次数为0
        if (customer.getCheckInCount() == null) {
            customer.setCheckInCount(0);
        }
        // 初始化VIP等级为普通会员(0)
        if (customer.getVipLevel() == null) {
            customer.setVipLevel(0);
        }
        // 初始化黑名单状态为非黑名单
        if (customer.getIsBlacklist() == null) {
            customer.setIsBlacklist(0);
        }
        // 初始化累计消费为0
        if (customer.getTotalSpent() == null) {
            customer.setTotalSpent(BigDecimal.ZERO);
        }
        // 保存客户
        save(customer);
        return Result.success("新增成功", null);
    }

    /**
     * 修改客户信息
     * 校验手机号唯一（排除自身）
     * @param customer 客户信息
     * @return 修改结果
     */
    @Override
    public Result<String> update(Customer customer) {
        // 校验客户存在
        Customer exist = getById(customer.getId());
        if (exist == null) {
            return Result.error("客户不存在");
        }
        // 如果修改了手机号，校验新手机号未被其他客户使用
        if (customer.getPhone() != null && !customer.getPhone().equals(exist.getPhone())) {
            Customer byPhone = baseMapper.findByPhone(customer.getPhone());
            if (byPhone != null) {
                return Result.error("该手机号已被其他客户使用");
            }
        }
        // 更新客户信息
        updateById(customer);
        return Result.success("修改成功", null);
    }

    /**
     * 删除客户
     * 校验客户无未完成订单才能删除
     * @param id 客户ID
     * @return 删除结果
     */
    @Override
    public Result<String> delete(Long id) {
        // 校验客户存在
        Customer customer = getById(id);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        // 校验客户无进行中的订单
        int activeOrders = orderMapper.countActiveByCustomerId(id);
        if (activeOrders > 0) {
            return Result.error("该客户存在未完成订单，无法删除");
        }
        // 删除客户
        removeById(id);
        return Result.success("删除成功", null);
    }

    /**
     * 将客户加入黑名单
     * 校验客户不在黑名单且无未完成订单
     * @param id 客户ID
     * @param reason 拉黑原因
     * @return 操作结果
     */
    @Override
    public Result<String> addToBlacklist(Long id, String reason) {
        // 校验客户存在
        Customer customer = getById(id);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        // 校验客户不在黑名单中
        if (customer.getIsBlacklist() != null && customer.getIsBlacklist() == 1) {
            return Result.error("该客户已在黑名单中");
        }
        // 校验客户无进行中订单（有在住订单不能拉黑）
        int activeOrders = orderMapper.countActiveByCustomerId(id);
        if (activeOrders > 0) {
            return Result.error("该客户存在未完成订单，无法加入黑名单");
        }
        // 更新黑名单状态为1（黑名单）
        baseMapper.updateBlacklist(id, 1, reason);
        return Result.success("已加入黑名单", null);
    }

    /**
     * 将客户移出黑名单
     * @param id 客户ID
     * @return 操作结果
     */
    @Override
    public Result<String> removeFromBlacklist(Long id) {
        // 校验客户存在
        Customer customer = getById(id);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        // 校验客户在黑名单中
        if (customer.getIsBlacklist() == null || customer.getIsBlacklist() == 0) {
            return Result.error("该客户不在黑名单中");
        }
        // 更新黑名单状态为0（正常）
        baseMapper.updateBlacklist(id, 0, null);
        return Result.success("已移出黑名单", null);
    }

    /**
     * 查询今日生日客户列表
     * @return 今日生日客户
     */
    @Override
    public Result<List<Customer>> birthdayList() {
        // 查询今天生日的客户（月日匹配）
        List<Customer> list = baseMapper.findBirthdayCustomers(LocalDate.now());
        return Result.success(list);
    }

    /**
     * 查询黑名单客户列表
     * @return 黑名单客户
     */
    @Override
    public Result<List<Customer>> blacklist() {
        // 查询所有黑名单客户
        List<Customer> list = baseMapper.findBlacklistCustomers();
        return Result.success(list);
    }

    /**
     * 根据入住次数升级客户VIP等级
     * 等级规则：银卡(1级，5次)、金卡(2级，15次)、钻石卡(3级，30次)
     * @param customerId 客户ID
     * @return 新的VIP等级
     */
    @Override
    @Transactional // 开启事务保证数据一致性
    public Result<Integer> upgradeVipLevel(Long customerId) {
        // 校验客户存在
        Customer customer = getById(customerId);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        // 统计客户已完成入住次数
        int completedOrders = orderMapper.countCompletedByCustomerId(customerId);
        // 根据入住次数计算新的VIP等级
        int newLevel = calculateVipLevel(completedOrders);
        // 获取当前VIP等级
        int oldLevel = customer.getVipLevel() != null ? customer.getVipLevel() : 0;
        // 只有升级才更新（不降级）
        if (newLevel > oldLevel) {
            Customer upd = new Customer();
            upd.setId(customerId);
            upd.setVipLevel(newLevel);
            updateById(upd);
            return Result.success("会员等级已升级", newLevel);
        }
        return Result.success("无需升级", oldLevel);
    }

    /**
     * 增加客户消费记录
     * 退房时调用：累加消费金额、增加入住次数、检查是否需要升级VIP
     * @param customerId 客户ID
     * @param amount 本次消费金额
     * @param stayTime 入住时间（暂时未用）
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result<String> addSpent(Long customerId, BigDecimal amount, LocalDateTime stayTime) {
        // 校验客户存在
        Customer customer = getById(customerId);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        // 累加消费金额
        baseMapper.addSpent(customerId, amount);
        // 增加入住次数
        baseMapper.increaseCheckInCount(customerId);
        // 检查并升级VIP等级
        upgradeVipLevel(customerId);
        return Result.success("消费记录已更新", null);
    }

    /**
     * 推荐可用房间
     * 根据房型和时间查询可用房间列表
     * @param typeId 房型ID（可选）
     * @param checkIn 入住时间
     * @param checkOut 退房时间
     * @return 可用房间列表
     */
    @Override
    public Result<List<Room>> recommendRooms(Long typeId, LocalDateTime checkIn, LocalDateTime checkOut) {
        // 格式化入住时间为字符串
        String startStr = checkIn.format(DATETIME_FMT);
        // 格式化退房时间为字符串
        String endStr = checkOut.format(DATETIME_FMT);
        // 查询指定时间段可用的房间
        List<Room> rooms = roomMapper.findAvailableRooms(typeId, startStr, endStr);
        return Result.success(rooms);
    }

    /**
     * 获取客户统计信息（包含订单、消费、VIP等级等）
     * @param customerId 客户ID
     * @return 客户统计数据
     */
    @Override
    public Result<Map<String, Object>> getCustomerStats(Long customerId) {
        // 校验客户存在
        Customer customer = getById(customerId);
        if (customer == null) {
            return Result.error("客户不存在");
        }
        // 构建统计结果Map
        Map<String, Object> stats = new HashMap<>();
        // 放入客户基本信息
        stats.put("customer", customer);
        // 查询已完成订单数
        int completedOrders = orderMapper.countCompletedByCustomerId(customerId);
        // 查询累计消费金额
        BigDecimal totalSpent = orderMapper.totalSpentByCustomerId(customerId);
        // 查询进行中订单数
        int activeOrders = orderMapper.countActiveByCustomerId(customerId);
        stats.put("completedOrders", completedOrders);
        stats.put("totalSpent", totalSpent);
        stats.put("activeOrders", activeOrders);
        // 当前VIP等级
        int vipLevel = customer.getVipLevel() != null ? customer.getVipLevel() : 0;
        stats.put("vipLevel", vipLevel);
        stats.put("vipName", getVipName(vipLevel));
        // 计算下一等级所需信息
        stats.put("nextLevelInfo", getNextLevelInfo(completedOrders));
        // 查询客户历史订单列表
        List<Order> orders = orderMapper.findByCustomerId(customerId);
        stats.put("orders", orders);
        return Result.success(stats);
    }

    /**
     * 根据入住次数计算VIP等级
     * 阈值从VipConfig常量类获取：银卡5次、金卡15次、钻石30次
     * @param checkInCount 累计入住次数
     * @return VIP等级（0普通、1银卡、2金卡、3钻石）
     */
    private int calculateVipLevel(int checkInCount) {
        // 达到钻石卡阈值（30次）返回3
        if (checkInCount >= VipConfig.DIAMOND_THRESHOLD) {
            return 3;
        } else if (checkInCount >= VipConfig.GOLD_THRESHOLD) {
            // 达到金卡阈值（15次）返回2
            return 2;
        } else if (checkInCount >= VipConfig.SILVER_THRESHOLD) {
            // 达到银卡阈值（5次）返回1
            return 1;
        }
        // 否则返回普通会员0
        return 0;
    }

    /**
     * 根据VIP等级获取等级名称
     * @param level VIP等级数值
     * @return 等级名称字符串
     */
    private String getVipName(int level) {
        switch (level) {
            case 1: return "银卡会员";
            case 2: return "金卡会员";
            case 3: return "钻石会员";
            default: return "普通会员";
        }
    }

    /**
     * 获取下一等级信息（距离下一等级还需多少次入住）
     * @param completedCount 当前已完成入住次数
     * @return 包含nextLevel（下一等级名称）和needCount（还需次数）的Map
     */
    private Map<String, Object> getNextLevelInfo(int completedCount) {
        Map<String, Object> info = new HashMap<>();
        if (completedCount < VipConfig.SILVER_THRESHOLD) {
            // 未达银卡：显示到银卡还需多少次
            info.put("nextLevel", "银卡会员");
            info.put("needCount", VipConfig.SILVER_THRESHOLD - completedCount);
        } else if (completedCount < VipConfig.GOLD_THRESHOLD) {
            // 已达银卡未达金卡：显示到金卡还需多少次
            info.put("nextLevel", "金卡会员");
            info.put("needCount", VipConfig.GOLD_THRESHOLD - completedCount);
        } else if (completedCount < VipConfig.DIAMOND_THRESHOLD) {
            // 已达金卡未达钻石：显示到钻石还需多少次
            info.put("nextLevel", "钻石会员");
            info.put("needCount", VipConfig.DIAMOND_THRESHOLD - completedCount);
        } else {
            // 已达最高等级（钻石）
            info.put("nextLevel", "已是最高等级");
            info.put("needCount", 0);
        }
        return info;
    }
}
