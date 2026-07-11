package com.jchotel.constants;

import java.math.BigDecimal;

public final class VipConfig {
    public static final int SILVER_THRESHOLD = 5;
    public static final int GOLD_THRESHOLD = 15;
    public static final int DIAMOND_THRESHOLD = 30;

    public static final BigDecimal SILVER_DISCOUNT = new BigDecimal("0.95");
    public static final BigDecimal GOLD_DISCOUNT = new BigDecimal("0.90");
    public static final BigDecimal DIAMOND_DISCOUNT = new BigDecimal("0.85");

    public static final int MAX_LOGIN_FAILS = 5;
    public static final int LOCK_MINUTES = 30;

    private VipConfig() {}
}
