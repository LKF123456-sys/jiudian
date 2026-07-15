package com.jchotel.constants;

/**
 * 支付/交易类型常量类
 * <p>
 * 定义支付流水的交易类型，包括正常支付、退款、押金收付等不同场景。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
public final class PaymentType {

    /**
     * 消费支付类型
     * 客房房费、消费项目的正常支付
     */
    public static final String PAY = "pay";

    /**
     * 退款类型
     * 订单取消、多收金额退还等场景的退款
     */
    public static final String REFUND = "refund";

    /**
     * 押金收取类型
     * 客人入住时收取的住宿押金
     */
    public static final String DEPOSIT = "deposit";

    /**
     * 押金退还类型
     * 客人退房时扣除消费后退还剩余押金
     */
    public static final String DEPOSIT_REFUND = "deposit_refund";

    /**
     * 私有构造方法，防止常量类被实例化
     */
    private PaymentType() {}
}
