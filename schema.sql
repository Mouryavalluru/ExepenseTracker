-- ============================================================
-- Personal Expense & Budget Guard System – Database Schema
-- PostgreSQL 12+
-- Run this manually OR let SchemaInitializer.java handle it.
-- ============================================================

-- Create the database (run as superuser once)
-- CREATE DATABASE expense_guard;
-- \c expense_guard

-- ------------------------------------------------------------
-- 1. Categories
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS categories (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Seed defaults
INSERT INTO categories (name, description) VALUES
    ('Food & Dining',   'Restaurants, groceries, and food delivery'),
    ('Transportation',  'Fuel, public transit, ride-shares'),
    ('Housing',         'Rent, utilities, maintenance'),
    ('Healthcare',      'Medical, dental, pharmacy'),
    ('Entertainment',   'Movies, games, subscriptions'),
    ('Shopping',        'Clothing, electronics, general retail'),
    ('Education',       'Tuition, books, courses'),
    ('Miscellaneous',   'Other uncategorised expenses')
ON CONFLICT (name) DO NOTHING;

-- ------------------------------------------------------------
-- 2. Budgets
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS budgets (
    id            SERIAL PRIMARY KEY,
    category_id   INTEGER REFERENCES categories(id) ON DELETE CASCADE,
    month_year    VARCHAR(7) NOT NULL,      -- format: YYYY-MM  e.g. 2025-02
    limit_amount  DECIMAL(12,2) NOT NULL,
    UNIQUE(category_id, month_year)
);

-- ------------------------------------------------------------
-- 3. Expenses
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS expenses (
    id            SERIAL PRIMARY KEY,
    category_id   INTEGER REFERENCES categories(id) ON DELETE SET NULL,
    description   VARCHAR(255) NOT NULL,
    amount        DECIMAL(12,2) NOT NULL,
    expense_date  DATE NOT NULL,
    notes         TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Useful indexes
CREATE INDEX IF NOT EXISTS idx_expenses_date     ON expenses(expense_date);
CREATE INDEX IF NOT EXISTS idx_expenses_category ON expenses(category_id);
CREATE INDEX IF NOT EXISTS idx_budgets_month     ON budgets(month_year);
