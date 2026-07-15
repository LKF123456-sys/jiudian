// 订单管理控制器包
package com.jchotel.controller;

// ========== DTO导入 ==========
import com.jchotel.dto.ChangeRoomDTO; // 换房请求DTO
import com.jchotel.dto.CheckinDTO; // 入住办理请求DTO
import com.jchotel.dto.CheckoutDTO; // 退房结账请求DTO
import com.jchotel.dto.ExtendStayDTO; // 续住请求DTO
import com.jchotel.dto.PageQuery; // 分页查询参数DTO
import com.jchotel.dto.PageResult; // 分页查询结果DTO
import com.jchotel.entity.Order; // 订单实体类

// ========== 服务层导入 ==========
import com.jchotel.service.OrderService; // 订单服务
import com.jchotel.utils.Result; // 统一响应结果封装类

// ========== Swagger/OpenAPI 文档注解导入 ==========
import io.swagger.v3.oas.annotations.Operation; // API操作描述注解
import io.swagger.v3.oas.annotations.tags.Tag; // API分组标签注解
import io.swagger.v3.oas.annotations.security.SecurityRequirement; // 安全要求注解

// ========== Spring框架相关导入 ==========
import org.springframework.beans.factory.annotation.Autowired; // 自动注入注解
import org.springframework.format.annotation.DateTimeFormat; // 日期时间格式化注解
import org.springframework.validation.annotation.Validated; // 类级别参数校验注解
import org.springframework.web.bind.annotation.*; // Spring MVC注解（包含GetMapping/PostMapping/PutMapping/RequestBody/PathVariable/RequestParam/Valid等）

// ========== Java EE/Servlet/Validation 相关导入 ==========
import jakarta.servlet.http.HttpServletRequest; // HTTP请求对象
import jakarta.validation.Valid; // 参数校验注解

// ========== Java 标准库导入 ==========
import java.time.LocalDateTime; // 本地日期时间类
import java.util.List; // 列表集合类
import java.util.Map; // 键值对映射类

/**
 * 订单管理控制器
 * 负责处理酒店订单的全生命周期管理，包括入住、结算、退房、取消、续住、换房等核心业务流程
 * URL前缀: /api/orders
 */
@RestController // 标记为REST风格控制器，返回值自动序列化为JSON
@RequestMapping("/api/orders") // 设置该控制器的基础请求路径
@Validated // 启用类级别的参数校验
@Tag(name = "订单管理") // Swagger文档分组标签
@SecurityRequirement(name = "Bearer Authentication") // 需要Bearer Token认证
public class OrderController {

    @Autowired // 自动注入订单服务
    private OrderService orderService; // 订单业务服务

    /**
     * 分页查询订单列表接口
     * 功能：按分页参数和条件查询订单列表
     * HTTP方法: GET /api/orders
     * @param query 分页查询参数（PageQuery对象，包含页码、页大小等）
     * @return Result包含分页订单数据的PageResult
     */
    @Operation(summary = "分页查询订单列表") // Swagger接口摘要描述
    @GetMapping // 映射GET请求到基础路径
    public Result<PageResult<Order>> list(PageQuery query) {
        return orderService.list(query); // 调用服务层分页查询订单列表
    }

    /**
     * 查询订单详情接口
     * 功能：根据ID查询单个订单的详细信息
     * HTTP方法: GET /api/orders/{id}
     * @param id 订单ID（PathVariable，路径参数）
     * @return Result包含订单详情的Order对象
     */
    @Operation(summary = "查询订单详情") // Swagger接口摘要描述
    @GetMapping("/{id}") // 映射GET请求到/{id}路径
    public Result<Order> detail(@PathVariable Long id) { // 从路径中获取订单ID
        return orderService.detail(id); // 调用服务层查询订单详情
    }

    /**
     * 办理入住接口
     * 功能：为预订的客户办理入住手续，分配客房，登记入住信息
     * HTTP方法: POST /api/orders/checkin
     * @param checkinDTO 入住信息DTO（RequestBody，包含客户信息、客房选择、押金等，@Valid启用参数校验）
     * @param request HTTP请求对象，用于获取当前操作人信息
     * @return Result包含入住结果数据的Map集合
     */
    @Operation(summary = "办理入住") // Swagger接口摘要描述
    @PostMapping("/checkin") // 映射POST请求到/checkin路径
    public Result<Map<String, Object>> checkin(@Valid @RequestBody CheckinDTO checkinDTO, HttpServletRequest request) { // 校验并接收入住DTO，获取请求对象
        return orderService.checkin(checkinDTO, request); // 调用服务层办理入住
    }

    /**
     * 结算待结订单接口
     * 功能：对待结账的订单进行费用结算（计算房费、消费品等总金额）
     * HTTP方法: POST /api/orders/{id}/settle
     * @param id 订单ID（PathVariable，路径参数）
     * @param request HTTP请求对象，用于获取当前操作人信息
     * @return Result包含结算结果数据的Map集合
     */
    @Operation(summary = "结算待结订单") // Swagger接口摘要描述
    @PostMapping("/{id}/settle") // 映射POST请求到/{id}/settle路径
    public Result<Map<String, Object>> settlePending(@PathVariable Long id, HttpServletRequest request) { // 从路径获取订单ID，获取请求对象
        return orderService.settlePending(id, request); // 调用服务层结算待结订单
    }

    /**
     * 办理退房接口
     * 功能：完成退房结账流程，释放客房，更新订单状态为已完成
     * HTTP方法: POST /api/orders/{id}/checkout
     * @param id 订单ID（PathVariable，路径参数）
     * @param checkoutDTO 退房信息DTO（RequestBody，包含支付方式、退款金额等，@Valid启用参数校验）
     * @return Result包含退房结果数据的Map集合
     */
    @Operation(summary = "办理退房") // Swagger接口摘要描述
    @PostMapping("/{id}/checkout") // 映射POST请求到/{id}/checkout路径
    public Result<Map<String, Object>> checkout(@PathVariable Long id, @Valid @RequestBody CheckoutDTO checkoutDTO) { // 获取路径订单ID，校验并接收退房DTO
        return orderService.checkout(id, checkoutDTO); // 调用服务层办理退房
    }

    /**
     * 取消订单接口
     * 功能：取消待入住或已预订的订单
     * HTTP方法: POST /api/orders/{id}/cancel
     * @param id 订单ID（PathVariable，路径参数）
     * @return Result包含取消结果提示信息的String
     */
    @Operation(summary = "取消订单") // Swagger接口摘要描述
    @PostMapping("/{id}/cancel") // 映射POST请求到/{id}/cancel路径
    public Result<String> cancel(@PathVariable Long id) { // 从路径中获取订单ID
        return orderService.cancel(id); // 调用服务层取消订单
    }

    /**
     * 续住接口
     * 功能：为在住订单办理续住手续，延长离店日期
     * HTTP方法: POST /api/orders/{id}/extend
     * @param id 订单ID（PathVariable，路径参数）
     * @param dto 续住信息DTO（RequestBody，包含续住天数、新离店日期等，@Valid启用参数校验）
     * @param request HTTP请求对象，用于获取当前操作人信息
     * @return Result包含续住结果数据的Map集合
     */
    @Operation(summary = "续住") // Swagger接口摘要描述
    @PostMapping("/{id}/extend") // 映射POST请求到/{id}/extend路径
    public Result<Map<String, Object>> extendStay(@PathVariable Long id, @Valid @RequestBody ExtendStayDTO dto, HttpServletRequest request) { // 获取路径订单ID，校验并接收续住DTO，获取请求对象
        dto.setOrderId(id); // 设置DTO中的订单ID为路径参数中的ID
        return orderService.extendStay(dto, request); // 调用服务层办理续住
    }

    /**
     * 换房接口
     * 功能：为在住订单更换客房
     * HTTP方法: POST /api/orders/{id}/change-room
     * @param id 订单ID（PathVariable，路径参数）
     * @param dto 换房信息DTO（RequestBody，包含目标客房ID、换房原因等，@Valid启用参数校验）
     * @param request HTTP请求对象，用于获取当前操作人信息
     * @return Result包含换房结果数据的Map集合
     */
    @Operation(summary = "换房") // Swagger接口摘要描述
    @PostMapping("/{id}/change-room") // 映射POST请求到/{id}/change-room路径
    public Result<Map<String, Object>> changeRoom(@PathVariable Long id, @Valid @RequestBody ChangeRoomDTO dto, HttpServletRequest request) { // 获取路径订单ID，校验并接收换房DTO，获取请求对象
        dto.setOrderId(id); // 设置DTO中的订单ID为路径参数中的ID
        return orderService.changeRoom(dto, request); // 调用服务层办理换房
    }

    /**
     * 查询今日入住订单接口
     * 功能：获取今日预计入住的订单列表
     * HTTP方法: GET /api/orders/today-arrivals
     * @return Result包含今日入住订单列表的List集合
     */
    @Operation(summary = "查询今日入住订单") // Swagger接口摘要描述
    @GetMapping("/today-arrivals") // 映射GET请求到/today-arrivals路径
    public Result<List<Order>> todayArrivals() {
        return orderService.todayArrivals(); // 调用服务层查询今日入住订单
    }

    /**
     * 查询今日离店订单接口
     * 功能：获取今日预计离店的订单列表
     * HTTP方法: GET /api/orders/today-departures
     * @return Result包含今日离店订单列表的List集合
     */
    @Operation(summary = "查询今日离店订单") // Swagger接口摘要描述
    @GetMapping("/today-departures") // 映射GET请求到/today-departures路径
    public Result<List<Order>> todayDepartures() {
        return orderService.todayDepartures(); // 调用服务层查询今日离店订单
    }

    /**
     * 查询客户订单列表接口
     * 功能：查询指定客户的所有历史订单
     * HTTP方法: GET /api/orders/customer/{customerId}
     * @param customerId 客户ID（PathVariable，路径参数）
     * @return Result包含客户订单列表的List集合
     */
    @Operation(summary = "查询客户订单列表") // Swagger接口摘要描述
    @GetMapping("/customer/{customerId}") // 映射GET请求到/customer/{customerId}路径
    public Result<List<Order>> customerOrders(@PathVariable Long customerId) { // 从路径中获取客户ID
        return orderService.customerOrders(customerId); // 调用服务层查询客户订单列表
    }

    /**
     * 查询低押金订单接口
     * 功能：获取押金余额不足的订单列表
     * HTTP方法: GET /api/orders/low-deposit
     * @return Result包含低押金订单列表的List集合
     */
    @Operation(summary = "查询低押金订单") // Swagger接口摘要描述
    @GetMapping("/low-deposit") // 映射GET请求到/low-deposit路径
    public Result<List<Order>> lowDepositOrders() {
        return orderService.lowDepositOrders(); // 调用服务层查询低押金订单
    }

    /**
     * 推荐客房接口
     * 功能：根据入住时间和离店时间推荐可用客房
     * HTTP方法: GET /api/orders/recommend-rooms
     * @param customerId 客户ID（可选，RequestParam，用于会员推荐）
     * @param checkIn 入住时间（必填，RequestParam，格式yyyy-MM-dd HH:mm:ss）
     * @param checkOut 离店时间（必填，RequestParam，格式yyyy-MM-dd HH:mm:ss）
     * @return Result包含推荐客房列表的List集合
     */
    @Operation(summary = "推荐客房") // Swagger接口摘要描述
    @GetMapping("/recommend-rooms") // 映射GET请求到/recommend-rooms路径
    public Result<List<Map<String, Object>>> recommendRooms(
            @RequestParam(required = false) Long customerId, // 可选的客户ID参数
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkIn, // 必填的入住时间参数，指定日期格式
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime checkOut) { // 必填的离店时间参数，指定日期格式
        return orderService.recommendRooms(customerId, checkIn, checkOut); // 调用服务层推荐可用客房
    }

    /**
     * 查询订单统计接口
     * 功能：按时间范围查询订单统计数据
     * HTTP方法: GET /api/orders/stats
     * @param range 统计范围（RequestParam，默认值week，可选day/week/month/year）
     * @param startTime 开始时间（可选，RequestParam）
     * @param endTime 结束时间（可选，RequestParam）
     * @return Result包含订单统计数据的Map集合
     */
    @Operation(summary = "查询订单统计") // Swagger接口摘要描述
    @GetMapping("/stats") // 映射GET请求到/stats路径
    public Result<Map<String, Object>> stats(
            @RequestParam(defaultValue = "week") String range, // 统计范围参数，默认为周
            @RequestParam(required = false) String startTime, // 可选的开始时间参数
            @RequestParam(required = false) String endTime) { // 可选的结束时间参数
        return orderService.stats(range, startTime, endTime); // 调用服务层查询订单统计数据
    }
}