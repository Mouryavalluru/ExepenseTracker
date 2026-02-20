package com.expenseguard.dao;

import com.expenseguard.db.DatabaseConnection;
import com.expenseguard.model.Expense;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class ExpenseDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Expense> findAll() throws SQLException {
        return query("SELECT e.*, c.name AS category_name FROM expenses e " +
                     "LEFT JOIN categories c ON e.category_id = c.id " +
                     "ORDER BY e.expense_date DESC, e.id DESC");
    }

    public List<Expense> findByMonth(YearMonth ym) throws SQLException {
        String sql = "SELECT e.*, c.name AS category_name FROM expenses e " +
                     "LEFT JOIN categories c ON e.category_id = c.id " +
                     "WHERE TO_CHAR(e.expense_date,'YYYY-MM') = ? " +
                     "ORDER BY e.expense_date DESC, e.id DESC";
        List<Expense> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, ym.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Expense> findByCategoryAndMonth(int categoryId, String monthYear) throws SQLException {
        String sql = "SELECT e.*, c.name AS category_name FROM expenses e " +
                     "LEFT JOIN categories c ON e.category_id = c.id " +
                     "WHERE e.category_id = ? AND TO_CHAR(e.expense_date,'YYYY-MM') = ? " +
                     "ORDER BY e.expense_date DESC";
        List<Expense> list = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setString(2, monthYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public BigDecimal sumByCategoryAndMonth(int categoryId, String monthYear) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM expenses " +
                     "WHERE category_id = ? AND TO_CHAR(expense_date,'YYYY-MM') = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, categoryId);
            ps.setString(2, monthYear);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    /** Returns total spent per category for a given month: [categoryId, categoryName, total] */
    public List<Object[]> monthlyCategorySummary(String monthYear) throws SQLException {
        String sql = "SELECT c.id, c.name, COALESCE(SUM(e.amount),0) AS total " +
                     "FROM categories c LEFT JOIN expenses e " +
                     "ON e.category_id = c.id AND TO_CHAR(e.expense_date,'YYYY-MM') = ? " +
                     "GROUP BY c.id, c.name ORDER BY total DESC";
        List<Object[]> rows = new ArrayList<>();
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setString(1, monthYear);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rows.add(new Object[]{rs.getInt(1), rs.getString(2), rs.getBigDecimal(3)});
                }
            }
        }
        return rows;
    }

    public Expense save(Expense e) throws SQLException {
        if (e.getId() == 0) {
            String sql = "INSERT INTO expenses (category_id, description, amount, expense_date, notes) " +
                         "VALUES (?, ?, ?, ?, ?) RETURNING id";
            try (PreparedStatement ps = conn().prepareStatement(sql)) {
                bind(ps, e);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) e.setId(rs.getInt(1));
                }
            }
        } else {
            String sql = "UPDATE expenses SET category_id=?, description=?, amount=?, " +
                         "expense_date=?, notes=?, updated_at=NOW() WHERE id=?";
            try (PreparedStatement ps = conn().prepareStatement(sql)) {
                bind(ps, e);
                ps.setInt(6, e.getId());
                ps.executeUpdate();
            }
        }
        return e;
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM expenses WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private List<Expense> query(String sql) throws SQLException {
        List<Expense> list = new ArrayList<>();
        try (Statement st = conn().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    private Expense map(ResultSet rs) throws SQLException {
        Expense e = new Expense();
        e.setId(rs.getInt("id"));
        e.setCategoryId(rs.getInt("category_id"));
        e.setCategoryName(rs.getString("category_name"));
        e.setDescription(rs.getString("description"));
        e.setAmount(rs.getBigDecimal("amount"));
        Date d = rs.getDate("expense_date");
        if (d != null) e.setExpenseDate(d.toLocalDate());
        e.setNotes(rs.getString("notes"));
        return e;
    }

    private void bind(PreparedStatement ps, Expense e) throws SQLException {
        ps.setInt(1, e.getCategoryId());
        ps.setString(2, e.getDescription());
        ps.setBigDecimal(3, e.getAmount());
        ps.setDate(4, Date.valueOf(e.getExpenseDate()));
        ps.setString(5, e.getNotes());
    }
}
