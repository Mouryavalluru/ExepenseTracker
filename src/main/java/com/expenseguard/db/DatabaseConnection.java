package com.expenseguard.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Singleton class for managing PostgreSQL database connections.
 */
public class DatabaseConnection {

    private static final Logger LOGGER = Logger.getLogger(DatabaseConnection.class.getName());

    // ── Change these to match your PostgreSQL setup ──────────────────────────
    private static final String URL      = "jdbc:postgresql://localhost:5433/expense_guard";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "Mourya11";
    // ─────────────────────────────────────────────────────────────────────────

    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            LOGGER.info("Database connection established successfully.");
        } catch (ClassNotFoundException e) {
            LOGGER.severe("PostgreSQL JDBC Driver not found: " + e.getMessage());
            throw new RuntimeException("JDBC Driver not found.", e);
        } catch (SQLException e) {
            LOGGER.severe("Failed to connect to database: " + e.getMessage());
            throw new RuntimeException("Database connection failed.", e);
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null || isConnectionClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    private static boolean isConnectionClosed() {
        try {
            return instance.connection == null || instance.connection.isClosed();
        } catch (SQLException e) {
            return true;
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                LOGGER.info("Database connection closed.");
            }
        } catch (SQLException e) {
            LOGGER.warning("Error closing connection: " + e.getMessage());
        }
    }
}
