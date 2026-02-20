package com.expenseguard.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

public final class CurrencyFormatter {

    private static final NumberFormat NF = NumberFormat.getCurrencyInstance(Locale.US);

    private CurrencyFormatter() {}

    public static String format(BigDecimal amount) {
        if (amount == null) return NF.format(BigDecimal.ZERO);
        return NF.format(amount);
    }

    public static String format(double amount) {
        return NF.format(amount);
    }
}
