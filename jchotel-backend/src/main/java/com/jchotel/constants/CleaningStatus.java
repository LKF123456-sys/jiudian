package com.jchotel.constants;

/**
 * 清扫任务状态常量类
 * <p>
 * 定义客房清扫任务在整个生命周期中的所有可能状态。
 * 状态流转路径：待派单 → 已派单 → 清扫中 → 检查中 → 已完成 / 已取消
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
public final class CleaningStatus {

    /**
     * 待处理/待派单状态
     * 客房退房后自动生成清扫任务，尚未分配保洁人员
     */
    public static final String PENDING = "pending";

    /**
     * 已派单状态
     * 已分配保洁人员，等待保洁人员开始清扫作业
     */
    public static final String ASSIGNED = "assigned";

    /**
     * 清扫中状态
     * 保洁人员正在进行客房清扫作业
     */
    public static final String CLEANING = "cleaning";

    /**
     * 检查中状态
     * 保洁人员清扫完成，等待领班或主管检查验收
     */
    public static final String INSPECTING = "inspecting";

    /**
     * 已完成状态
     * 清扫完成并通过检查验收，客房状态自动变为空闲可售
     */
    public static final String COMPLETED = "completed";

    /**
     * 已取消状态
     * 清扫任务因特殊原因被取消
     */
    public static final String CANCELLED = "cancelled";

    /**
     * 私有构造方法，防止常量类被实例化
     */
    private CleaningStatus() {}
}
