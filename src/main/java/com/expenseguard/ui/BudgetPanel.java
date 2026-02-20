package com.expenseguard.ui;

import com.expenseguard.dao.CategoryDAO;
import com.expenseguard.model.Budget;
import com.expenseguard.model.Category;
import com.expenseguard.service.ExpenseService;
import com.expenseguard.util.CurrencyFormatter;
import com.expenseguard.util.UITheme;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for viewing and managing monthly budget limits.
 */
public class BudgetPanel extends JPanel {

    private final ExpenseService service     = new ExpenseService();
    private final CategoryDAO    categoryDAO = new CategoryDAO();

    private JComboBox<String> cbMonth;
    private JTable            table;
    private DefaultTableModel tableModel;
    private List<Budget>      budgets = new ArrayList<>();

    private static final String[] COLS = {
        "ID", "Category", "Month", "Limit", "Spent", "Remaining", "Usage %", "Status"
    };

    public BudgetPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.SURFACE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        buildUI();
        refreshData();
    }

    private void buildUI() {
        // ── Header ───────────────────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UITheme.titleLabel("🛡  Budget Guard"), BorderLayout.WEST);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        ctrl.setOpaque(false);

        cbMonth = new JComboBox<>();
        YearMonth cur = YearMonth.now();
        for (int i = 0; i < 12; i++)
            cbMonth.addItem(cur.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        cbMonth.addActionListener(e -> refreshData());

        JButton btnSet    = UITheme.primaryButton("+ Set Budget");
        JButton btnDelete = UITheme.dangerButton("🗑 Remove");

        btnSet.addActionListener(e -> onSetBudget());
        btnDelete.addActionListener(e -> onDeleteBudget());

        ctrl.add(new JLabel("Month:"));
        ctrl.add(cbMonth);
        ctrl.add(btnSet);
        ctrl.add(btnDelete);
        top.add(ctrl, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // ── Table ────────────────────────────────────────────────────────────
        tableModel = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UITheme.FONT_BODY);
        table.getTableHeader().setBackground(UITheme.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setGridColor(UITheme.BORDER_COLOR);

        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);

        // Custom row renderer for status colouring
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, value, isSelected, hasFocus, row, col);
                if (!isSelected && row < budgets.size()) {
                    Budget b = budgets.get(row);
                    if (b.isExceeded())       c.setBackground(new Color(254, 226, 226));
                    else if (b.isNearLimit()) c.setBackground(new Color(254, 243, 199));
                    else                      c.setBackground(Color.WHITE);
                }
                return c;
            }
        });

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        add(sp, BorderLayout.CENTER);

        // Legend
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 4));
        legend.setOpaque(false);
        legend.add(legendItem(new Color(254, 226, 226), "Exceeded"));
        legend.add(legendItem(new Color(254, 243, 199), "Near limit (≥80%)"));
        legend.add(legendItem(Color.WHITE, "OK"));
        add(legend, BorderLayout.SOUTH);
    }

    private JPanel legendItem(Color color, String label) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        p.setOpaque(false);
        JPanel swatch = new JPanel();
        swatch.setPreferredSize(new Dimension(14, 14));
        swatch.setBackground(color);
        swatch.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_SMALL);
        p.add(swatch);
        p.add(lbl);
        return p;
    }

    public void refreshData() {
        try {
            String month = (String) cbMonth.getSelectedItem();
            budgets = service.getBudgetsForMonth(month);
            tableModel.setRowCount(0);
            for (Budget b : budgets) {
                String status = b.isExceeded() ? "❌ Exceeded"
                              : b.isNearLimit() ? "⚡ Near Limit"
                              : "✅ OK";
                tableModel.addRow(new Object[]{
                    b.getId(),
                    b.getCategoryName(),
                    b.getMonthYear(),
                    CurrencyFormatter.format(b.getLimitAmount()),
                    CurrencyFormatter.format(b.getSpentAmount() != null ? b.getSpentAmount() : BigDecimal.ZERO),
                    CurrencyFormatter.format(b.getRemainingAmount()),
                    String.format("%.1f%%", b.getUsagePercent()),
                    status
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading budgets: " + ex.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onSetBudget() {
        try {
            List<Category> cats = categoryDAO.findAll();
            Category[] catArr = cats.toArray(new Category[0]);
            Category selected = (Category) JOptionPane.showInputDialog(
                this, "Select category:", "Set Budget",
                JOptionPane.PLAIN_MESSAGE, null, catArr, catArr[0]);
            if (selected == null) return;

            String amtStr = JOptionPane.showInputDialog(this,
                "Monthly budget limit for \"" + selected.getName() + "\" ($):");
            if (amtStr == null || amtStr.isBlank()) return;

            BigDecimal limit;
            try {
                limit = new BigDecimal(amtStr.trim());
                if (limit.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid positive amount.");
                return;
            }

            String month = (String) cbMonth.getSelectedItem();
            Budget b = new Budget(selected.getId(), month, limit);
            service.saveBudget(b);
            refreshData();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDeleteBudget() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a budget to remove."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Remove this budget limit?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            service.deleteBudget(budgets.get(row).getId());
            refreshData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
