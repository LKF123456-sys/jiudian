package com.jchotel.constants;

/**
 * 用户角色常量类
 * <p>
 * 定义系统中所有的用户角色，配合@RequireRole注解使用，
 * 实现基于角色的接口访问权限控制（RBAC）。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
public final class UserRole {

    /**
     * 系统管理员角色
     * 拥有系统最高权限，可管理所有模块、用户账号配置、系统设置等
     */
    public static final String ADMIN = "admin";

    /**
     * 酒店经理角色
     * 酒店管理层，可查看经营报表、业务审批、管理所有业务模块
     */
    public static final String MANAGER = "manager";

    /**
     * 前台接待角色
     * 负责办理入住/退房、订单管理、收银结账、客户信息维护等前台业务
     */
    public static final String RECEPTIONIST = "receptionist";

    /**
     * 客房保洁角色
     * 客房部保洁人员，可查看清扫任务、更新清扫状态
     */
    public static final String HOUSEKEEPING = "housekeeping";

    /**
     * 工程维修角色
     * 工程部维修人员，可查看维修工单、更新维修状态、记录维修情况
     */
    public static final String ENGINEERING = "engineering";

    /**
     * 私有构造方法，防止常量类被实例化
     */
    private UserRole() {}
}
