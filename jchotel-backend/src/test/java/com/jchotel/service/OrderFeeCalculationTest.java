package com.jchotel.service;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

class OrderFeeCalculationTest {

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

    private int calculateNights(LocalDateTime checkIn, LocalDateTime checkOut) {
        long minutes = ChronoUnit.MINUTES.between(checkIn, checkOut);
        int nights = (int) Math.ceil(minutes / (24.0 * 60.0));
        return Math.max(nights, 1);
    }

    @Test
    void calculateVipPrice_noVip_returnsOriginal() {
        BigDecimal price = new BigDecimal("200.00");
        assertEquals(0, calculateVipPrice(price, null).compareTo(price));
        assertEquals(0, calculateVipPrice(price, 0).compareTo(price));
    }

    @Test
    void calculateVipPrice_silverDiscount_is95Percent() {
        BigDecimal price = new BigDecimal("200.00");
        BigDecimal result = calculateVipPrice(price, 1);
        assertEquals(0, new BigDecimal("190.00").compareTo(result));
    }

    @Test
    void calculateVipPrice_goldDiscount_is90Percent() {
        BigDecimal price = new BigDecimal("200.00");
        BigDecimal result = calculateVipPrice(price, 2);
        assertEquals(0, new BigDecimal("180.00").compareTo(result));
    }

    @Test
    void calculateVipPrice_diamondDiscount_is85Percent() {
        BigDecimal price = new BigDecimal("200.00");
        BigDecimal result = calculateVipPrice(price, 3);
        assertEquals(0, new BigDecimal("170.00").compareTo(result));
    }

    @Test
    void calculateVipPrice_unknownLevel_returnsOriginal() {
        BigDecimal price = new BigDecimal("200.00");
        assertEquals(0, calculateVipPrice(price, 99).compareTo(price));
    }

    @Test
    void calculateNights_exactly24Hours_is1Night() {
        LocalDateTime in = LocalDateTime.of(2026, 7, 10, 14, 0);
        LocalDateTime out = LocalDateTime.of(2026, 7, 11, 14, 0);
        assertEquals(1, calculateNights(in, out));
    }

    @Test
    void calculateNights_lessThan24Hours_is1NightMinimum() {
        LocalDateTime in = LocalDateTime.of(2026, 7, 10, 14, 0);
        LocalDateTime out = LocalDateTime.of(2026, 7, 10, 18, 0);
        assertEquals(1, calculateNights(in, out));
    }

    @Test
    void calculateNights_over24Hours_is2Nights() {
        LocalDateTime in = LocalDateTime.of(2026, 7, 10, 14, 0);
        LocalDateTime out = LocalDateTime.of(2026, 7, 11, 15, 0);
        assertEquals(2, calculateNights(in, out));
    }

    @Test
    void calculateNights_3Days_is3Nights() {
        LocalDateTime in = LocalDateTime.of(2026, 7, 10, 14, 0);
        LocalDateTime out = LocalDateTime.of(2026, 7, 13, 12, 0);
        assertEquals(3, calculateNights(in, out));
    }

    @Test
    void calculateNights_1MinuteOver24h_is2Nights() {
        LocalDateTime in = LocalDateTime.of(2026, 7, 10, 14, 0);
        LocalDateTime out = LocalDateTime.of(2026, 7, 11, 14, 1);
        assertEquals(2, calculateNights(in, out));
    }
}
