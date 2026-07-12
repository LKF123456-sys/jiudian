package com.jchotel.service;

// MyBatis-Plus通用服务接口，提供基础CRUD能力
import com.baomidou.mybatisplus.extension.service.IService;
// 分页查询参数对象
import com.jchotel.dto.PageQuery;
// 分页结果对象
import com.jchotel.dto.PageResult;
// 客户实体类，对应数据库customer表
import com.jchotel.entity.Customer;
// 房间实体类
import com.jchotel.entity.Room;
// 统一响应结果封装类
import com.jchotel.utils.Result;

// 高精度小数类型，用于消费金额
import java.math.BigDecimal;
// 日期时间类，处理入住时间
import java.time.LocalDateTime;
// List集合，返回列表数据
import java.util.List;
// Map集合，返回统计数据
import java.util.Map;

/**
 * 客户管理服务接口
 * 负责酒店客户（住客）信息管理，包括客户增删改查、黑名单管理、生日提醒、
 * VIP会员等级升级、消费记录累计、房间推荐、客户统计等功能
 */
public interface CustomerService extends IService<Customer> {

    /**
     * 分页查询客户列表
     * 支持按姓名、手机号、会员等级、是否黑名单等条件筛选
     * @param query 分页查询参数
     * @return 分页客户列表
     */
    Result<PageResult<Customer>> list(PageQuery query);

    /**
     * 查询客户详情
     * @param id 客户ID
     * @return 客户详细信息，包含会员信息、消费历史等
     */
    Result<Customer> detail(Long id);

    /**
     * 新增客户
     * 入住时如客户不存在则自动创建，也可手动录入
     * @param customer 客户信息，包含姓名、手机号、证件号等
     * @return 新增结果提示
     */
    Result<String> add(Customer customer);

    /**
     * 更新客户信息
     * @param customer 需要更新的客户信息，必须包含客户ID
     * @return 更新结果提示
     */
    Result<String> update(Customer customer);

    /**
     * 删除客户
     * @param id 待删除的客户ID
     * @return 删除结果提示
     */
    Result<String> delete(Long id);

    /**
     * 将客户加入黑名单
     * 黑名单客户将无法办理入住
     * @param id 客户ID
     * @param reason 拉黑原因
     * @return 操作结果提示
     */
    Result<String> addToBlacklist(Long id, String reason);

    /**
     * 将客户从黑名单移除
     * @param id 客户ID
     * @return 操作结果提示
     */
    Result<String> removeFromBlacklist(Long id);

    /**
     * 查询生日客户列表
     * 用于客户关怀，在客户生日当天发送祝福或提供优惠
     * @return 当天或近期过生日的客户列表
     */
    Result<List<Customer>> birthdayList();

    /**
     * 查询黑名单客户列表
     * @return 所有黑名单客户列表
     */
    Result<List<Customer>> blacklist();

    /**
     * 根据累计消费自动升级VIP会员等级
     * 客户消费达到升级阈值后自动升级，享受相应折扣
     * @param customerId 客户ID
     * @return 升级后的会员等级
     */
    Result<Integer> upgradeVipLevel(Long customerId);

    /**
     * 累计客户消费金额并记录入住时间
     * 退房结算后调用，更新客户累计消费、入住次数、最后入住时间
     * @param customerId 客户ID
     * @param amount 本次消费金额
     * @param stayTime 本次入住时间
     * @return 操作结果提示
     */
    Result<String> addSpent(Long customerId, BigDecimal amount, LocalDateTime stayTime);

    /**
     * 为客户推荐可用房间
     * 根据房型和时间查询可售房间，并结合客户偏好排序
     * @param typeId 房型ID（可选，不指定则推荐所有房型）
     * @param checkIn 预计入住时间
     * @param checkOut 预计退房时间
     * @return 推荐房间列表
     */
    Result<List<Room>> recommendRooms(Long typeId, LocalDateTime checkIn, LocalDateTime checkOut);

    /**
     * 获取客户统计数据
     * 包含该客户的累计入住次数、累计消费、平均消费、最爱房型等统计
     * @param customerId 客户ID
     * @return 客户统计数据Map
     */
    Result<Map<String, Object>> getCustomerStats(Long customerId);
}
