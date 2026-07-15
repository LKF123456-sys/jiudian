package com.jchotel.constants;

/**
 * 订单状态常量类
 * <p>
 * 定义客房预订/入住订单在整个生命周期中的所有可能状态。
 * 状态流转路径：待入住 → 已入住 → 已退房 / 已取消
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
public final class OrderStatus {

    /**
     * 待入住/预约状态
     * 客人已完成预订但尚未办理入住手续
     */
    public static final String PENDING = "pending";

    /**
     * 已入住状态
     * 客人已办理入住手续，正在客房入住中
     */
    public static final String CHECKED_IN = "checkedIn";

    /**
     * 已退房状态
     * 客人已办理退房结账手续，订单完成
     */
    public static final String CHECKED_OUT = "checkedOut";

    /**
     * 已取消状态
     * 订单被取消（超时未入住自动取消或客人主动取消）
     */
    public static final String CANCELLED = "cancelled";

    /**
     * 私有构造方法，防止常量类被实例化
     */
    private OrderStatus() {}
}
