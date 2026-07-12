package com.jchotel.constants; // 定义包名，constants包存放常量类

/**
 * 订单状态常量类
 * 定义客房预订/入住订单的状态
 * 状态流转：待入住 -> 已入住 -> 已退房 / 已取消
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
public final class OrderStatus { // final类，不可被继承
    public static final String PENDING = "pending"; // 待入住/预约：已预订但尚未办理入住
    public static final String CHECKED_IN = "checkedIn"; // 已入住：客人已办理入住手续
    public static final String CHECKED_OUT = "checkedOut"; // 已退房：客人已办理退房结账
    public static final String CANCELLED = "cancelled"; // 已取消：订单被取消（超时或主动取消）

    private OrderStatus() {} // 私有构造方法，防止实例化
} // 结束OrderStatus类
