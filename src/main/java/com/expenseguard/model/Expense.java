package com.expenseguard.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Represents a single expense record.
 */
public class Expense {

    private int           id;
    private int           categoryId;
    private String        categoryName;   // joined from categories table
    private String        description;
    private BigDecimal    amount;
    private LocalDate     expenseDate;
    private String        notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Expense() {}

    public Expense(int categoryId, String description, BigDecimal amount,
                   LocalDate expenseDate, String notes) {
        this.categoryId  = categoryId;
        this.description = description;
        this.amount      = amount;
        this.expenseDate = expenseDate;
        this.notes       = notes;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────
    public int           getId()                       { return id; }
    public void          setId(int id)                 { this.id = id; }

    public int           getCategoryId()               { return categoryId; }
    public void          setCategoryId(int v)          { this.categoryId = v; }

    public String        getCategoryName()             { return categoryName; }
    public void          setCategoryName(String v)     { this.categoryName = v; }

    public String        getDescription()              { return description; }
    public void          setDescription(String v)      { this.description = v; }

    public BigDecimal    getAmount()                   { return amount; }
    public void          setAmount(BigDecimal v)       { this.amount = v; }

    public LocalDate     getExpenseDate()              { return expenseDate; }
    public void          setExpenseDate(LocalDate v)   { this.expenseDate = v; }

    public String        getNotes()                    { return notes; }
    public void          setNotes(String v)            { this.notes = v; }

    public LocalDateTime getCreatedAt()                { return createdAt; }
    public void          setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    public LocalDateTime getUpdatedAt()                { return updatedAt; }
    public void          setUpdatedAt(LocalDateTime v) { this.updatedAt = v; }
}
