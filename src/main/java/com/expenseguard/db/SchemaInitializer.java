package com.expenseguard.db;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Creates all required database tables on first run.
 */
public class SchemaInitializer {

    private static final Logger LOGGER = Logger.getLogger(SchemaInitializer.class.getName());

    public static void initialize() {
        Connection conn = DatabaseConnection.getInstance().getConnection();
        try (Statement stmt = conn.createStatement()) {

            // Categories table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS categories (
                    id          SERIAL PRIMARY KEY,
                    name        VARCHAR(100) NOT NULL UNIQUE,
                    description TEXT,
                    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Budgets table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id          SERIAL PRIMARY KEY,
                    category_id INTEGER REFERENCES categories(id) ON DELETE CASCADE,
                    month_year  VARCHAR(7) NOT NULL,   -- format: YYYY-MM
                    limit_amount DECIMAL(12,2) NOT NULL,
                    UNIQUE(category_id, month_year)
                )
            """);

            // Expenses table
            stmt.executeUpdate("""
                CREATE TABLE IF NOT EXISTS expenses (
                    id          SERIAL PRIMARY KEY,
                    category_id INTEGER REFERENCES categories(id) ON DELETE SET NULL,
                    description VARCHAR(255) NOT NULL,
                    amount      DECIMAL(12,2) NOT NULL,
                    expense_date DATE NOT NULL,
                    notes       TEXT,
                    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
            """);

            // Seed default categories if none exist
            stmt.executeUpdate("""
                INSERT INTO categories (name, description)
                VALUES
                    ('Food & Dining',   'Restaurants, groceries, and food delivery'),
                    ('Transportation',  'Fuel, public transit, ride-shares'),
                    ('Housing',         'Rent, utilities, maintenance'),
                    ('Healthcare',      'Medical, dental, pharmacy'),
                    ('Entertainment',   'Movies, games, subscriptions'),
                    ('Shopping',        'Clothing, electronics, general retail'),
                    ('Education',       'Tuition, books, courses'),
                    ('Miscellaneous',   'Other uncategorised expenses')
                ON CONFLICT (name) DO NOTHING
            """);

            LOGGER.info("Schema initialised successfully.");

        } catch (SQLException e) {
            LOGGER.severe("Schema initialisation failed: " + e.getMessage());
            throw new RuntimeException("Schema init failed.", e);
        }
    }
}
