package com.example.bankcards.util;

import java.time.YearMonth;

public final class CardExpiryUtils {

    private CardExpiryUtils() {
    }

    public static boolean isExpired(int expiryMonth, int expiryYear, YearMonth now) {
        YearMonth exp = YearMonth.of(expiryYear, expiryMonth);
        return now.isAfter(exp);
    }
}
