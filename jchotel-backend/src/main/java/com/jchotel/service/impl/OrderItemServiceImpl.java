package com.jchotel.service.impl;

// MyBatis-Plus通用服务实现基类
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
// 订单状态常量类
import com.jchotel.constants.OrderStatus;
// 订单实体类
import com.jchotel.entity.Order;
// 订单项（消费明细）实体类
import com.jchotel.entity.OrderItem;
// 订单项数据访问Mapper
import com.jchotel.mapper.OrderItemMapper;
// 订单数据访问Mapper
import com.jchotel.mapper.OrderMapper;
// 订单项服务接口
import com.jchotel.service.OrderItemService;
// 统一响应结果封装
import com.jchotel.utils.Result;
// Spring自动注入注解
import org.springframework.beans.factory.annotation.Autowired;
// Spring服务层注解
import org.springframework.stereotype.Service;
// 事务注解，保证数据一致性
import org.springframework.transaction.annotation.Transactional;

// 高精度小数类型，金额计算
import java.math.BigDecimal;
// 舍入模式，金额保留2位小数四舍五入
import java.math.RoundingMode;
// List集合
import java.util.List;

/**
 * 订单消费明细服务实现类
 * 实现入住订单附加消费项的添加、删除、查询和总额计算功能
 * 添加/删除消费项时同步更新订单的附加消费总额和订单总额，使用@Transactional保证事务一致性
 */
@Service // 标记为Spring服务组件
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements OrderItemService {

    @Autowired // 自动注入订单Mapper，用于更新订单金额
    private OrderMapper orderMapper;

    /**
     * 向订单添加一条消费明细
     * 校验订单状态（必须已入住）、价格数量合法性，计算消费金额，保存后同步更新订单总额
     * @param orderId 订单ID
     * @param item 消费项信息（名称、单价、数量）
     * @param operatorId 操作人ID
     * @return 添加结果，返回新增的消费项
     */
    @Override
    @Transactional // 开启事务，保证添加消费项和更新订单金额要么同时成功要么同时失败
    public Result addItem(Long orderId, OrderItem item, Long operatorId) {
        // 根据ID查询订单
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.error("订单不存在");
        }
        // 校验订单状态：只有已入住的订单才能添加消费项
        if (!OrderStatus.CHECKED_IN.equals(order.getStatus())) {
            return Result.error("只有已入住的订单才能添加消费项");
        }
        // 校验单价必须大于0
        if (item.getPrice() == null || item.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            return Result.error("单价必须大于0");
        }
        // 校验数量必须大于0
        if (item.getQuantity() == null || item.getQuantity() <= 0) {
            return Result.error("数量必须大于0");
        }

        // 计算消费金额：单价 * 数量
        BigDecimal amount = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        // 设置关联订单ID
        item.setOrderId(orderId);
        // 设置订单号
        item.setOrderNo(order.getOrderNo());
        // 设置计算后的消费金额
        item.setAmount(amount);
        // 设置操作人ID
        item.setOperatorId(operatorId);
        // 保存消费明细到数据库
        save(item);

        // 重新汇总该订单所有附加消费总额
        BigDecimal extraAmount = baseMapper.sumByOrderId(orderId);
        if (extraAmount == null) extraAmount = BigDecimal.ZERO;
        // 获取订单原房费金额
        BigDecimal roomAmount = order.getRoomAmount() != null ? order.getRoomAmount() : BigDecimal.ZERO;
        // 更新订单的附加消费金额（保留2位小数四舍五入）
        order.setExtraAmount(extraAmount.setScale(2, RoundingMode.HALF_UP));
        // 更新订单总金额 = 房费 + 附加消费（保留2位小数）
        order.setTotalAmount(roomAmount.add(extraAmount).setScale(2, RoundingMode.HALF_UP));
        // 更新订单到数据库
        orderMapper.updateById(order);

        return Result.success("添加消费项成功", item);
    }

    /**
     * 删除一条消费明细
     * 删除后重新汇总消费金额并更新订单总额
     * @param itemId 消费项ID
     * @return 删除结果
     */
    @Override
    @Transactional // 开启事务保证一致性
    public Result removeItem(Long itemId) {
        // 查询消费项
        OrderItem item = getById(itemId);
        if (item == null) {
            return Result.error("消费项不存在");
        }
        // 查询关联的订单
        Order order = orderMapper.selectById(item.getOrderId());
        // 删除消费项
        removeById(itemId);

        // 如果订单存在且状态为已入住，需要重新计算金额（退房后的订单不允许修改消费）
        if (order != null && OrderStatus.CHECKED_IN.equals(order.getStatus())) {
            // 重新汇总附加消费
            BigDecimal extraAmount = baseMapper.sumByOrderId(order.getId());
            if (extraAmount == null) extraAmount = BigDecimal.ZERO;
            BigDecimal roomAmount = order.getRoomAmount() != null ? order.getRoomAmount() : BigDecimal.ZERO;
            order.setExtraAmount(extraAmount.setScale(2, RoundingMode.HALF_UP));
            order.setTotalAmount(roomAmount.add(extraAmount).setScale(2, RoundingMode.HALF_UP));
            orderMapper.updateById(order);
        }

        return Result.success("删除成功", null);
    }

    /**
     * 查询指定订单的所有消费明细列表
     * @param orderId 订单ID
     * @return 消费明细列表
     */
    @Override
    public Result<List<OrderItem>> listByOrder(Long orderId) {
        // 调用Mapper查询该订单下的所有消费项
        List<OrderItem> list = baseMapper.findByOrderId(orderId);
        return Result.success(list);
    }

    /**
     * 计算指定订单的附加消费总金额
     * @param orderId 订单ID
     * @return 附加消费总金额（BigDecimal）
     */
    @Override
    public Result<BigDecimal> getTotalExtra(Long orderId) {
        // 汇总该订单所有消费项金额
        BigDecimal total = baseMapper.sumByOrderId(orderId);
        // null值处理，返回0
        return Result.success(total != null ? total : BigDecimal.ZERO);
    }
}
