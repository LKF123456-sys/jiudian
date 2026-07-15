package com.jchotel.constants;

import java.math.BigDecimal;

/**
 * VIP会员配置常量类
 * <p>
 * 定义会员等级升级阈值、各等级折扣率、账户安全相关配置参数。
 * 会员等级根据客人累计入住次数自动升级，不同等级享受不同的房费折扣优惠。
 * </p>
 *
 * @author 锦程酒店开发团队
 * @since 1.0.0
 */
public final class VipConfig {

    /**
     * 银卡会员升级阈值（累计入住次数）
     * 累计入住达到5次自动升级为银卡会员
     */
    public static final int SILVER_THRESHOLD = 5;

    /**
     * 金卡会员升级阈值（累计入住次数）
     * 累计入住达到15次自动升级为金卡会员
     */
    public static final int GOLD_THRESHOLD = 15;

    /**
     * 钻石卡会员升级阈值（累计入住次数）
     * 累计入住达到30次自动升级为钻石卡会员
     */
    public static final int DIAMOND_THRESHOLD = 30;

    /**
     * 银卡会员折扣率（95折）
     * 使用BigDecimal避免浮点数计算精度问题
     */
    public static final BigDecimal SILVER_DISCOUNT = new BigDecimal("0.95");

    /**
     * 金卡会员折扣率（9折）
     */
    public static final BigDecimal GOLD_DISCOUNT = new BigDecimal("0.90");

    /**
     * 钻石卡会员折扣率（85折）
     */
    public static final BigDecimal DIAMOND_DISCOUNT = new BigDecimal("0.85");

    /**
     * 最大连续登录失败次数
     * 连续登录失败超过5次将锁定账户
     */
    public static final int MAX_LOGIN_FAILS = 5;

    /**
     * 账户锁定时长（分钟）
     * 账户锁定30分钟后自动解锁
     */
    public static final int LOCK_MINUTES = 30;

    /**
     * 私有构造方法，防止常量类被实例化
     */
    private VipConfig() {}
}
