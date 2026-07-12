package com.jchotel.constants; // 定义包名，constants包存放常量类

// Java数学类
import java.math.BigDecimal; // 高精度十进制数，用于金额计算避免精度丢失

/**
 * VIP会员配置常量类
 * 定义会员等级升级阈值、折扣率、账户安全相关配置
 * 会员等级根据入住次数自动升级
 *
 * @author 锦程酒店
 * @since 1.0.0
 */
public final class VipConfig { // final类，不可被继承
    // 会员等级入住次数阈值
    public static final int SILVER_THRESHOLD = 5; // 银卡升级阈值：累计入住5次升级为银卡会员
    public static final int GOLD_THRESHOLD = 15; // 金卡升级阈值：累计入住15次升级为金卡会员
    public static final int DIAMOND_THRESHOLD = 30; // 钻石卡升级阈值：累计入住30次升级为钻石卡会员

    // 会员折扣率（使用BigDecimal避免浮点精度问题）
    public static final BigDecimal SILVER_DISCOUNT = new BigDecimal("0.95"); // 银卡折扣：95折
    public static final BigDecimal GOLD_DISCOUNT = new BigDecimal("0.90"); // 金卡折扣：9折
    public static final BigDecimal DIAMOND_DISCOUNT = new BigDecimal("0.85"); // 钻石卡折扣：85折

    // 账户安全配置
    public static final int MAX_LOGIN_FAILS = 5; // 最大连续登录失败次数：超过5次锁定账户
    public static final int LOCK_MINUTES = 30; // 账户锁定时长：锁定30分钟后自动解锁

    private VipConfig() {} // 私有构造方法，防止实例化
} // 结束VipConfig类
