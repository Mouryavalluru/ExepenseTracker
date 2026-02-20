package com.expenseguard.dao;

import com.expenseguard.db.DatabaseConnection;
import com.expenseguard.model.Budget;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BudgetDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Budget> findByMonth(String monthYear) throws SQLException {
        String sql = "SELECT b.id, b.category_id, c.name AS category_name, " +
                     "b.month_year, b.limit_amount " +
                     "FROM budgets b JOIN categories c ON b.category_id = c.id " +
                     "WHERE b.month_year = ? ORDER BY c.name";
        List<Budget> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, monthYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public Budget findByCategoryAndMonth(int categoryId, String monthYear) throws SQLException {
        String sql = "SELECT b.id, b.category_id, c.name AS category_name, " +
                     "b.month_year, b.limit_amount " +
                     "FROM budgets b JOIN categories c ON b.category_id = c.id " +
                     "WHERE b.category_id = ? AND b.month_year = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setString(2, monthYear);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Budget save(Budget b) throws SQLException {
        if (b.getId() == 0) {
            String sql = "INSERT INTO budgets (category_id, month_year, limit_amount) " +
                         "VALUES (?, ?, ?) " +
                         "ON CONFLICT (category_id, month_year) DO UPDATE SET limit_amount = EXCLUDED.limit_amount " +
                         "RETURNING id";
            try (PreparedStatement ps = conn().prepareStatement(sql)) {
                ps.setInt(1, b.getCategoryId());
                ps.setString(2, b.getMonthYear());
                ps.setBigDecimal(3, b.getLimitAmount());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) b.setId(rs.getInt(1));
                }
            }
        } else {
            String sql = "UPDATE budgets SET limit_amount = ? WHERE id = ?";
            try (PreparedStatement ps = conn().prepareStatement(sql)) {
                ps.setBigDecimal(1, b.getLimitAmount());
                ps.setInt(2, b.getId());
                ps.executeUpdate();
            }
        }
        return b;
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM budgets WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Budget map(ResultSet rs) throws SQLException {
        Budget b = new Budget();
        b.setId(rs.getInt("id"));
        b.setCategoryId(rs.getInt("category_id"));
        b.setCategoryName(rs.getString("category_name"));
        b.setMonthYear(rs.getString("month_year"));
        b.setLimitAmount(rs.getBigDecimal("limit_amount"));
        return b;
    }
}
