package com.jchotel.constants; // 定义包名，constants包存放常量类

/**
 * 清扫任务状态常量类
 * 定义客房清扫任务的所有可能状态
 * 状态流转：待派单 -> 已派单 -> 清扫中 -> 检查中 -> 已完成 / 已取消
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
public final class CleaningStatus { // final类，不可被继承
    public static final String PENDING = "pending"; // 待处理/待派单：客房退房后生成清扫任务，尚未分配保洁人员
    public static final String ASSIGNED = "assigned"; // 已派单：已分配保洁人员，等待开始清扫
    public static final String CLEANING = "cleaning"; // 清扫中：保洁人员正在清扫
    public static final String INSPECTING = "inspecting"; // 检查中：清扫完成，等待领班检查验收
    public static final String COMPLETED = "completed"; // 已完成：清扫完成并通过检查，客房变为空闲可售状态
    public static final String CANCELLED = "cancelled"; // 已取消：清扫任务被取消

    private CleaningStatus() {} // 私有构造方法，防止实例化
} // 结束CleaningStatus类
