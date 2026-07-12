package com.jchotel.constants; // 定义包名，constants包存放常量类

/**
 * 发票状态常量类
 * 定义发票的开具状态
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
public final class InvoiceStatus { // final类，不可被继承
    public static final String ISSUED = "issued"; // 已开具：正常开具的蓝字发票
    public static final String RED = "red"; // 已红冲：发票开具后发生退款等情况，开具红字发票冲销
    public static final String CANCELLED = "cancelled"; // 已作废：发票作废处理

    private InvoiceStatus() {} // 私有构造方法，防止实例化
} // 结束InvoiceStatus类
