package com.jchotel.constants; // 定义包名，constants包存放常量类

/**
 * 用户角色常量类
 * 定义系统中所有用户角色，用于权限控制
 * 配合@RequireRole注解使用，控制接口访问权限
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
public final class UserRole { // final类，不可被继承
    public static final String ADMIN = "admin"; // 系统管理员：拥有最高权限，可管理所有模块、用户配置
    public static final String MANAGER = "manager"; // 经理：酒店管理层，可查看报表、审批、管理所有业务
    public static final String RECEPTIONIST = "receptionist"; // 前台接待：负责入住、退房、订单管理、收银等前台业务
    public static final String HOUSEKEEPING = "housekeeping"; // 客房保洁：负责查看清扫任务、更新清扫状态
    public static final String ENGINEERING = "engineering"; // 工程维修：负责查看维修工单、更新维修状态

    private UserRole() {} // 私有构造方法，防止实例化
} // 结束UserRole类
