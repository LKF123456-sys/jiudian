package com.jchotel.constants; // 定义包名，constants包存放常量类

/**
 * 支付/交易类型常量类
 * 定义支付流水的交易类型，包括支付、退款、押金收付
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
public final class PaymentType { // final类，不可被继承
    public static final String PAY = "pay"; // 消费支付：客房费、消费项目的正常支付
    public static final String REFUND = "refund"; // 退款：订单取消、多收金额的退款
    public static final String DEPOSIT = "deposit"; // 押金收取：入住时收取的押金
    public static final String DEPOSIT_REFUND = "deposit_refund"; // 押金退还：退房时扣除消费后退还押金

    private PaymentType() {} // 私有构造方法，防止实例化
} // 结束PaymentType类
