package com.expenseguard.model;

import java.math.BigDecimal;

/**
 * Represents a monthly budget limit for a category.
 */
public class Budget {

    private int        id;
    private int        categoryId;
    private String     categoryName;
    private String     monthYear;      // YYYY-MM
    private BigDecimal limitAmount;
    private BigDecimal spentAmount;    // populated by service layer

    public Budget() {}

    public Budget(int categoryId, String monthYear, BigDecimal limitAmount) {
        this.categoryId  = categoryId;
        this.monthYear   = monthYear;
        this.limitAmount = limitAmount;
    }

    // ── Computed helpers ─────────────────────────────────────────────────────
    public BigDecimal getRemainingAmount() {
        if (spentAmount == null) return limitAmount;
        return limitAmount.subtract(spentAmount);
    }

    public double getUsagePercent() {
        if (spentAmount == null || limitAmount.compareTo(BigDecimal.ZERO) == 0) return 0;
        return spentAmount.divide(limitAmount, 4, java.math.RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100))
                          .doubleValue();
    }

    public boolean isExceeded()      { return getUsagePercent() >= 100; }
    public boolean isNearLimit()     { return getUsagePercent() >= 80 && !isExceeded(); }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public int        getId()                        { return id; }
    public void       setId(int id)                  { this.id = id; }

    public int        getCategoryId()                { return categoryId; }
    public void       setCategoryId(int v)           { this.categoryId = v; }

    public String     getCategoryName()              { return categoryName; }
    public void       setCategoryName(String v)      { this.categoryName = v; }

    public String     getMonthYear()                 { return monthYear; }
    public void       setMonthYear(String v)         { this.monthYear = v; }

    public BigDecimal getLimitAmount()               { return limitAmount; }
    public void       setLimitAmount(BigDecimal v)   { this.limitAmount = v; }

    public BigDecimal getSpentAmount()               { return spentAmount; }
    public void       setSpentAmount(BigDecimal v)   { this.spentAmount = v; }
}
