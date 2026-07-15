package com.jchotel.constants;

/**
 * 维修工单状态常量类
 * <p>
 * 定义维修工单在整个生命周期中的所有可能状态。
 * 状态流转路径：待派单 → 已派单 → 维修中 → 已完成 → 已验收 / 已取消
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
public final class MaintenanceStatus {

    /**
     * 待处理/待派单状态
     * 新建的维修工单尚未分配维修人员
     */
    public static final String PENDING = "pending";

    /**
     * 已派单状态
     * 已分配维修人员，等待维修人员到场处理
     */
    public static final String ASSIGNED = "assigned";

    /**
     * 维修中状态
     * 维修人员正在进行维修作业
     */
    public static final String PROCESSING = "processing";

    /**
     * 已完成状态
     * 维修人员完成维修作业，等待管理人员验收
     */
    public static final String COMPLETED = "completed";

    /**
     * 已验收状态
     * 维修完成并通过管理人员验收，客房可恢复正常使用
     */
    public static final String VERIFIED = "verified";

    /**
     * 已取消状态
     * 维修工单因问题已解决或误报等原因被取消
     */
    public static final String CANCELLED = "cancelled";

    /**
     * 私有构造方法，防止常量类被实例化
     */
    private MaintenanceStatus() {}
}
