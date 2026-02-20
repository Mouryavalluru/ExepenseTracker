package com.expenseguard.ui;

import com.expenseguard.dao.CategoryDAO;
import com.expenseguard.model.Category;
import com.expenseguard.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for managing expense categories.
 */
public class CategoriesPanel extends JPanel {

    private final CategoryDAO   dao = new CategoryDAO();
    private List<Category>      cats = new ArrayList<>();

    private JTable              table;
    private DefaultTableModel   model;

    private static final String[] COLS = {"ID", "Name", "Description"};

    public CategoriesPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.SURFACE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        buildUI();
        refreshData();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UITheme.titleLabel("🏷  Categories"), BorderLayout.WEST);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        ctrl.setOpaque(false);

        JButton btnAdd    = UITheme.primaryButton("+ Add");
        JButton btnEdit   = UITheme.secondaryButton("✏ Edit");
        JButton btnDelete = UITheme.dangerButton("🗑 Delete");

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());

        ctrl.add(btnAdd); ctrl.add(btnEdit); ctrl.add(btnDelete);
        top.add(ctrl, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UITheme.FONT_BODY);
        table.getTableHeader().setBackground(UITheme.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(UITheme.BORDER_COLOR);

        // Hide ID
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        add(sp, BorderLayout.CENTER);
    }

    public void refreshData() {
        try {
            cats = dao.findAll();
            model.setRowCount(0);
            for (Category c : cats)
                model.addRow(new Object[]{c.getId(), c.getName(), c.getDescription()});
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    private void onAdd() {
        JTextField tfName = UITheme.styledField();
        JTextField tfDesc = UITheme.styledField();
        JPanel p = buildFormPanel(tfName, tfDesc);
        int res = JOptionPane.showConfirmDialog(this, p, "Add Category", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        if (tfName.getText().isBlank()) { JOptionPane.showMessageDialog(this, "Name required."); return; }
        try {
            dao.save(new Category(0, tfName.getText().trim(), tfDesc.getText().trim()));
            refreshData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a category."); return; }
        Category c = cats.get(row);
        JTextField tfName = UITheme.styledField(); tfName.setText(c.getName());
        JTextField tfDesc = UITheme.styledField(); tfDesc.setText(c.getDescription());
        JPanel p = buildFormPanel(tfName, tfDesc);
        int res = JOptionPane.showConfirmDialog(this, p, "Edit Category", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;
        c.setName(tfName.getText().trim());
        c.setDescription(tfDesc.getText().trim());
        try {
            dao.save(c);
            refreshData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage());
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a category."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete category \"" + cats.get(row).getName() + "\"?\nExpenses in this category will lose their category.",
            "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            dao.delete(cats.get(row).getId());
            refreshData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage());
        }
    }

    private JPanel buildFormPanel(JTextField name, JTextField desc) {
        JPanel p = new JPanel(new GridLayout(4, 1, 0, 4));
        p.add(new JLabel("Category Name *:"));
        p.add(name);
        p.add(new JLabel("Description:"));
        p.add(desc);
        return p;
    }
}
