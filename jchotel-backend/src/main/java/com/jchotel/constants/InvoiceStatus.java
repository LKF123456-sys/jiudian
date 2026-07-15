package com.jchotel.constants;

/**
 * 发票状态常量类
 * <p>
 * 定义发票在整个生命周期中的开具状态。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
public final class InvoiceStatus {

    /**
     * 已开具状态
     * 正常开具的蓝字发票，可用于报销
     */
    public static final String ISSUED = "issued";

    /**
     * 已红冲状态
     * 发票开具后发生退款、错开等情况，开具红字发票进行冲销
     */
    public static final String RED = "red";

    /**
     * 已作废状态
     * 发票因开具错误等原因被作废处理
     */
    public static final String CANCELLED = "cancelled";

    /**
     * 私有构造方法，防止常量类被实例化
     */
    private InvoiceStatus() {}
}
