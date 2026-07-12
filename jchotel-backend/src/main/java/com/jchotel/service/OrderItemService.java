package com.jchotel.service;

// MyBatis-Plus通用服务接口，提供基础CRUD能力
import com.baomidou.mybatisplus.extension.service.IService;
// 订单项（消费明细）实体类，对应数据库order_item表
import com.jchotel.entity.OrderItem;
// 统一响应结果封装类
import com.jchotel.utils.Result;

// 高精度小数类型，用于金额计算
import java.math.BigDecimal;
// List集合，返回消费明细列表
import java.util.List;

/**
 * 订单消费明细服务接口
 * 负责入住订单的附加消费管理，包括商品/服务的添加、删除、查询，以及消费总额计算
 * 附加消费包括迷你吧商品、餐饮、洗衣、加床等额外收费项目
 */
public interface OrderItemService extends IService<OrderItem> {

    /**
     * 向订单添加一条消费明细
     * @param orderId 订单ID，关联到具体入住订单
     * @param item 消费项信息，包含商品名称、单价、数量、金额等
     * @param operatorId 操作人ID（记录谁添加的消费）
     * @return 添加结果
     */
    Result addItem(Long orderId, OrderItem item, Long operatorId);

    /**
     * 从订单中删除一条消费明细
     * @param itemId 消费项ID
     * @return 删除结果
     */
    Result removeItem(Long itemId);

    /**
     * 查询指定订单的所有消费明细列表
     * @param orderId 订单ID
     * @return 该订单的消费明细列表
     */
    Result<List<OrderItem>> listByOrder(Long orderId);

    /**
     * 计算指定订单的附加消费总金额
     * 汇总该订单下所有消费项的金额总和（不含房费）
     * @param orderId 订单ID
     * @return 附加消费总金额
     */
    Result<BigDecimal> getTotalExtra(Long orderId);
}
