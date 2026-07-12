package com.jchotel.constants; // 定义包名，constants包存放常量类

/**
 * 维修工单状态常量类
 * 定义维修工单的所有可能状态
 * 状态流转：待派单 -> 已派单 -> 维修中 -> 已完成 -> 已验收 / 已取消
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
public final class MaintenanceStatus { // final类，不可被继承
    public static final String PENDING = "pending"; // 待处理/待派单：新建工单尚未分配维修人员
    public static final String ASSIGNED = "assigned"; // 已派单：已分配维修人员，等待开始维修
    public static final String PROCESSING = "processing"; // 维修中：维修人员正在处理
    public static final String COMPLETED = "completed"; // 已完成：维修完成，等待验收
    public static final String VERIFIED = "verified"; // 已验收：维修完成并通过验收
    public static final String CANCELLED = "cancelled"; // 已取消：工单被取消

    private MaintenanceStatus() {} // 私有构造方法，防止实例化（工具类/常量类规范）
} // 结束MaintenanceStatus类
