package com.jchotel.constants; // 定义包名，constants包存放常量类

/**
 * 客房状态常量类
 * 定义客房的当前状态，用于房态管理
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
public final class RoomStatus { // final类，不可被继承
    public static final String IDLE = "idle"; // 空闲/可售：客房已清扫完毕，可办理入住
    public static final String OCCUPIED = "occupied"; // 占用/在住：客人已入住
    public static final String MAINTENANCE = "maintenance"; // 维修中：客房因设施故障等原因维修中，不可售
    public static final String CLEANING = "cleaning"; // 清扫中：客人退房后正在清扫，暂不可售
    public static final String DIRTY = "dirty"; // 脏房/待清扫：客人刚退房，等待安排清扫

    private RoomStatus() {} // 私有构造方法，防止实例化
} // 结束RoomStatus类
