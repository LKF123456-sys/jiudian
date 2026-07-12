package com.jchotel.constants; // 定义包名，constants包存放常量类

/**
 * 收费项目分类常量类
 * 定义客房消费/附加收费的类别
 * 用于记账、账单明细分类统计
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
public final class ChargeCategory { // final类，不可被继承
    public static final String MINI_BAR = "mini_bar"; // 迷你吧：客房内小冰箱饮料、零食消费
    public static final String LAUNDRY = "laundry"; // 洗衣服务：客衣洗涤收费
    public static final String FOOD = "food"; // 餐饮：客房送餐、餐厅消费记账
    public static final String DAMAGE = "damage"; // 物品损坏赔偿：客房设施/物品损坏赔偿
    public static final String OTHER = "other"; // 其他收费：未归类的其他消费项目

    private ChargeCategory() {} // 私有构造方法，防止实例化
} // 结束ChargeCategory类
