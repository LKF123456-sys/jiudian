package com.jchotel.constants;

/**
 * 收费项目分类常量类
 * <p>
 * 定义客房消费/附加收费的类别，用于记账、账单明细分类统计和报表分析。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
public final class ChargeCategory {

    /**
     * 迷你吧消费
     * 客房内小冰箱饮料、零食等消费
     */
    public static final String MINI_BAR = "mini_bar";

    /**
     * 洗衣服务收费
     * 客人衣物洗涤、熨烫等服务收费
     */
    public static final String LAUNDRY = "laundry";

    /**
     * 餐饮消费
     * 客房送餐、餐厅消费挂账等
     */
    public static final String FOOD = "food";

    /**
     * 物品损坏赔偿
     * 客房设施、物品损坏的赔偿费用
     */
    public static final String DAMAGE = "damage";

    /**
     * 其他收费
     * 未归类的其他杂项消费项目
     */
    public static final String OTHER = "other";

    /**
     * 私有构造方法，防止常量类被实例化
     */
    private ChargeCategory() {}
}
