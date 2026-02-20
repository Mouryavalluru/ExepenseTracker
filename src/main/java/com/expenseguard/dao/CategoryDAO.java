package com.expenseguard.dao;

import com.expenseguard.db.DatabaseConnection;
import com.expenseguard.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {

    private Connection conn() {
        return DatabaseConnection.getInstance().getConnection();
    }

    public List<Category> findAll() throws SQLException {
        List<Category> list = new ArrayList<>();
        String sql = "SELECT id, name, description FROM categories ORDER BY name";
        try (PreparedStatement ps = conn().prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(map(rs));
            }
        }
        return list;
    }

    public Category findById(int id) throws SQLException {
        String sql = "SELECT id, name, description FROM categories WHERE id = ?";
        try (PreparedStatement ps = conn().prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Category save(Category c) throws SQLException {
        if (c.getId() == 0) {
            String sql = "INSERT INTO categories (name, description) VALUES (?, ?) RETURNING id";
            try (PreparedStatement ps = conn().prepareStatement(sql)) {
                ps.setString(1, c.getName());
                ps.setString(2, c.getDescription());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) c.setId(rs.getInt(1));
                }
            }
        } else {
            String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
            try (PreparedStatement ps = conn().prepareStatement(sql)) {
                ps.setString(1, c.getName());
                ps.setString(2, c.getDescription());
                ps.setInt(3, c.getId());
                ps.executeUpdate();
            }
        }
        return c;
    }

    public void delete(int id) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement("DELETE FROM categories WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Category map(ResultSet rs) throws SQLException {
        return new Category(rs.getInt("id"), rs.getString("name"), rs.getString("description"));
    }
}
