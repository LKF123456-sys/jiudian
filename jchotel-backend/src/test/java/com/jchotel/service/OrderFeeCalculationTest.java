// 测试类所在包
package com.jchotel.service;

// JUnit 5测试注解
import org.junit.jupiter.api.Test;

// Java反射相关类（可能用于后续扩展测试）
import java.lang.reflect.Method;
// Java高精度数值类型，用于金额计算
import java.math.BigDecimal;
// 数值舍入模式枚举
import java.math.RoundingMode;
// Java日期时间类
import java.time.LocalDateTime;
// 时间单位枚举，用于计算时间差
import java.time.temporal.ChronoUnit;

// JUnit 5断言方法
import static org.junit.jupiter.api.Assertions.*;

/**
 * 订单费用计算单元测试
 * 测试目标：验证订单费用计算中的两个核心逻辑：
 * 1. VIP折扣价格计算（不同VIP等级对应不同折扣率）
 * 2. 入住天数计算（按时间差向上取整，最少1晚）
 */
class OrderFeeCalculationTest {

    /**
     * 计算VIP折扣后价格
     * 折扣规则：
     * - null或VIP等级0：无折扣，返回原价
     * - VIP等级1（银卡）：95折
     * - VIP等级2（金卡）：9折
     * - VIP等级3（钻石卡）：85折
     * - 其他等级：无折扣
     * 计算结果保留2位小数，四舍五入
     * @param originalPrice 原价
     * @param vipLevel VIP等级
     * @return 折扣后的价格
     */
    private BigDecimal calculateVipPrice(BigDecimal originalPrice, Integer vipLevel) {
        if (vipLevel == null || vipLevel == 0) return originalPrice;
        BigDecimal discount;
        BigDecimal SILVER = new BigDecimal("0.95");
        BigDecimal GOLD = new BigDecimal("0.9");
        BigDecimal DIAMOND = new BigDecimal("0.85");
        switch (vipLevel) {
            case 1: discount = SILVER; break;
            case 2: discount = GOLD; break;
            case 3: discount = DIAMOND; break;
            default: discount = BigDecimal.ONE;
        }
        return originalPrice.multiply(discount).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算入住天数
     * 计算规则：
     * - 根据入住和退房时间差计算天数
     * - 超过24小时按2晚计算（向上取整）
     * - 最少1晚（即使入住不足24小时也按1晚计算）
     * @param checkIn 入住时间
     * @param checkOut 退房时间
     * @return 入住天数（最少1）
     */
    private int calculateNights(LocalDateTime checkIn, LocalDateTime checkOut) {
        long minutes = ChronoUnit.MINUTES.between(checkIn, checkOut);
        int nights = (int) Math.ceil(minutes / (24.0 * 60.0));
        return Math.max(nights, 1);
    }

    /**
     * 测试场景：非VIP客户（null和等级0）不享受折扣
     * 验证点：
     * 1. vipLevel为null时返回原价
     * 2. vipLevel为0时返回原价
     */
    @Test
    void calculateVipPrice_noVip_returnsOriginal() {
        BigDecimal price = new BigDecimal("200.00");
        assertEquals(0, calculateVipPrice(price, null).compareTo(price));
        assertEquals(0, calculateVipPrice(price, 0).compareTo(price));
    }

    /**
     * 测试场景：银卡VIP（等级1）享受95折优惠
     * 验证点：200元原价折扣后应为190元
     */
    @Test
    void calculateVipPrice_silverDiscount_is95Percent() {
        BigDecimal price = new BigDecimal("200.00");
        BigDecimal result = calculateVipPrice(price, 1);
        assertEquals(0, new BigDecimal("190.00").compareTo(result));
    }

    /**
     * 测试场景：金卡VIP（等级2）享受9折优惠
     * 验证点：200元原价折扣后应为180元
     */
    @Test
    void calculateVipPrice_goldDiscount_is90Percent() {
        BigDecimal price = new BigDecimal("200.00");
        BigDecimal result = calculateVipPrice(price, 2);
        assertEquals(0, new BigDecimal("180.00").compareTo(result));
    }

    /**
     * 测试场景：钻石卡VIP（等级3）享受85折优惠
     * 验证点：200元原价折扣后应为170元
     */
    @Test
    void calculateVipPrice_diamondDiscount_is85Percent() {
        BigDecimal price = new BigDecimal("200.00");
        BigDecimal result = calculateVipPrice(price, 3);
        assertEquals(0, new BigDecimal("170.00").compareTo(result));
    }

    /**
     * 测试场景：未知VIP等级不享受折扣
     * 验证点：等级99（不存在的等级）应返回原价
     */
    @Test
    void calculateVipPrice_unknownLevel_returnsOriginal() {
        BigDecimal price = new BigDecimal("200.00");
        assertEquals(0, calculateVipPrice(price, 99).compareTo(price));
    }

    /**
     * 测试场景：正好24小时入住按1晚计算
     * 验证点：7月10日14:00入住到7月11日14:00退房（正好24小时）应算1晚
     */
    @Test
    void calculateNights_exactly24Hours_is1Night() {
        LocalDateTime in = LocalDateTime.of(2026, 7, 10, 14, 0);
        LocalDateTime out = LocalDateTime.of(2026, 7, 11, 14, 0);
        assertEquals(1, calculateNights(in, out));
    }

    /**
     * 测试场景：入住不足24小时按最少1晚计算
     * 验证点：7月10日14:00入住到7月10日18:00退房（仅4小时）应算1晚（最低保障）
     */
    @Test
    void calculateNights_lessThan24Hours_is1NightMinimum() {
        LocalDateTime in = LocalDateTime.of(2026, 7, 10, 14, 0);
        LocalDateTime out = LocalDateTime.of(2026, 7, 10, 18, 0);
        assertEquals(1, calculateNights(in, out));
    }

    /**
     * 测试场景：入住超过24小时按2晚计算
     * 验证点：7月10日14:00入住到7月11日15:00退房（25小时）应算2晚（向上取整）
     */
    @Test
    void calculateNights_over24Hours_is2Nights() {
        LocalDateTime in = LocalDateTime.of(2026, 7, 10, 14, 0);
        LocalDateTime out = LocalDateTime.of(2026, 7, 11, 15, 0);
        assertEquals(2, calculateNights(in, out));
    }

    /**
     * 测试场景：入住3天按3晚计算
     * 验证点：7月10日14:00入住到7月13日12:00退房（约70小时）应算3晚
     */
    @Test
    void calculateNights_3Days_is3Nights() {
        LocalDateTime in = LocalDateTime.of(2026, 7, 10, 14, 0);
        LocalDateTime out = LocalDateTime.of(2026, 7, 13, 12, 0);
        assertEquals(3, calculateNights(in, out));
    }

    /**
     * 测试场景：超过24小时仅1分钟也按2晚计算
     * 验证点：7月10日14:00入住到7月11日14:01退房（24小时1分钟）应算2晚（边界测试）
     */
    @Test
    void calculateNights_1MinuteOver24h_is2Nights() {
        LocalDateTime in = LocalDateTime.of(2026, 7, 10, 14, 0);
        LocalDateTime out = LocalDateTime.of(2026, 7, 11, 14, 1);
        assertEquals(2, calculateNights(in, out));
    }
}
