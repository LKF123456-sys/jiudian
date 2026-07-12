package com.jchotel.service;

// MyBatis-Plus通用服务接口，提供基础CRUD能力
import com.baomidou.mybatisplus.extension.service.IService;
// 换房请求数据传输对象
import com.jchotel.dto.ChangeRoomDTO;
// 入住登记请求数据传输对象
import com.jchotel.dto.CheckinDTO;
// 退房结算请求数据传输对象
import com.jchotel.dto.CheckoutDTO;
// 续住请求数据传输对象
import com.jchotel.dto.ExtendStayDTO;
// 分页查询参数对象
import com.jchotel.dto.PageQuery;
// 分页结果对象
import com.jchotel.dto.PageResult;
// 订单实体类，对应数据库order表
import com.jchotel.entity.Order;
// 统一响应结果封装类
import com.jchotel.utils.Result;

// HTTP请求对象，用于获取当前操作人信息
import jakarta.servlet.http.HttpServletRequest;
// 日期时间类，处理入住退房时间
import java.time.LocalDateTime;
// List集合，返回列表数据
import java.util.List;
// Map集合，返回统计数据等键值对结构
import java.util.Map;

/**
 * 订单管理服务接口
 * 酒店核心业务模块，负责客房预订、入住登记、退房结算、订单取消、续住换房、
 * 超时取消、今日到店/离店查询、低押金预警、房间推荐等全流程订单业务
 */
public interface OrderService extends IService<Order> {

    /**
     * 分页查询订单列表
     * 支持按订单号、客户姓名、房间号、订单状态、时间范围等多条件筛选
     * @param query 分页查询参数，包含筛选条件
     * @return 分页订单列表
     */
    Result<PageResult<Order>> list(PageQuery query);

    /**
     * 查询订单详情
     * @param id 订单ID
     * @return 订单详细信息，关联客户、房间、消费明细等
     */
    Result<Order> detail(Long id);

    /**
     * 办理入住登记
     * 选择房间、登记客户信息、收取押金、生成入住订单、更新房间状态为入住中
     * @param checkinDTO 入住信息，包含客户信息、房间ID、入住天数、押金等
     * @param request HTTP请求，获取当前操作的前台人员信息
     * @return 入住结果，包含生成的订单信息
     */
    Result<Map<String, Object>> checkin(CheckinDTO checkinDTO, HttpServletRequest request);

    /**
     * 办理退房结算
     * 计算房费、消费明细、总金额，扣除押金后多退少补，更新房间状态为待清洁
     * @param id 订单ID
     * @param checkoutDTO 退房信息，包含支付方式、备注等
     * @return 结算结果，包含费用明细、应收应付金额
     */
    Result<Map<String, Object>> checkout(Long id, CheckoutDTO checkoutDTO);

    /**
     * 取消订单
     * 仅待确认或已确认未入住状态的订单可取消，释放房间资源
     * @param id 订单ID
     * @return 取消结果提示
     */
    Result<String> cancel(Long id);

    /**
     * 订单统计数据
     * 按日/周/月/年或自定义时间范围统计订单数量、收入、入住率等指标
     * @param range 统计范围（day/week/month/year/custom）
     * @param startTime 自定义开始时间（range为custom时使用）
     * @param endTime 自定义结束时间（range为custom时使用）
     * @return 统计数据Map，包含各类统计指标
     */
    Result<Map<String, Object>> stats(String range, String startTime, String endTime);

    /**
     * 结算待支付订单
     * 处理押金不足或有欠费的订单结算
     * @param id 订单ID
     * @param request HTTP请求，获取操作人信息
     * @return 结算结果
     */
    Result<Map<String, Object>> settlePending(Long id, HttpServletRequest request);

    /**
     * 自动取消超时未支付的待确认订单
     * 定时任务调用，超过规定时间未支付的预订订单自动取消，释放房间库存
     * @return 取消的订单数量
     */
    int cancelExpiredPendingOrders();

    /**
     * 办理续住
     * 延长客人入住时间，重新计算房费，加收续住押金
     * @param dto 续住信息，包含订单ID、续住天数等
     * @param request HTTP请求，获取操作人信息
     * @return 续住结果，包含新的退房日期和费用
     */
    Result<Map<String, Object>> extendStay(ExtendStayDTO dto, HttpServletRequest request);

    /**
     * 办理换房
     * 将客人从当前房间调换到其他房间，记录换房日志，更新房间状态
     * @param dto 换房信息，包含原订单ID、新房间ID、换房原因等
     * @param request HTTP请求，获取操作人信息
     * @return 换房结果，包含新房间信息
     */
    Result<Map<String, Object>> changeRoom(ChangeRoomDTO dto, HttpServletRequest request);

    /**
     * 查询今日预计到店订单列表
     * @return 今日应到店的订单列表
     */
    Result<List<Order>> todayArrivals();

    /**
     * 查询今日预计离店订单列表
     * @return 今日应退房的订单列表
     */
    Result<List<Order>> todayDepartures();

    /**
     * 查询指定客户的历史订单列表
     * @param customerId 客户ID
     * @return 该客户的所有入住订单
     */
    Result<List<Order>> customerOrders(Long customerId);

    /**
     * 查询押金不足的订单列表
     * 用于押金预警，提醒前台催缴押金
     * @return 押金低于阈值的在住订单列表
     */
    Result<List<Order>> lowDepositOrders();

    /**
     * 根据客户偏好推荐可用房间
     * 结合客户历史入住记录、会员等级、当前空房情况智能推荐房型房间
     * @param customerId 客户ID，用于分析入住偏好
     * @param checkIn 预计入住时间
     * @param checkOut 预计退房时间
     * @return 推荐房间列表，包含推荐理由
     */
    Result<List<Map<String, Object>>> recommendRooms(Long customerId, LocalDateTime checkIn, LocalDateTime checkOut);
}
