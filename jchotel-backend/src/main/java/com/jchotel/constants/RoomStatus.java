package com.jchotel.constants;

/**
 * 客房状态常量类
 * <p>
 * 定义客房在运营过程中的所有可能状态，用于房态管理和前台房态盘展示。
 * 房态直接影响客房是否可售、是否可安排入住等业务逻辑。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
public final class RoomStatus {

    /**
     * 空闲/可售状态
     * 客房已清扫完毕并通过检查，可以安排客人入住
     */
    public static final String IDLE = "idle";

    /**
     * 占用/在住状态
     * 客人已办理入住手续，客房正在使用中
     */
    public static final String OCCUPIED = "occupied";

    /**
     * 维修中状态
     * 客房因设施故障、损坏等原因正在维修，暂不可售
     */
    public static final String MAINTENANCE = "maintenance";

    /**
     * 清扫中状态
     * 客人退房后保洁人员正在进行清扫作业，暂不可售
     */
    public static final String CLEANING = "cleaning";

    /**
     * 脏房/待清扫状态
     * 客人刚退房，尚未安排保洁人员进行清扫
     */
    public static final String DIRTY = "dirty";

    /**
     * 私有构造方法，防止常量类被实例化
     */
    private RoomStatus() {}
}
