package com.expenseguard.ui;

import com.expenseguard.model.Expense;
import com.expenseguard.service.ExpenseService;
import com.expenseguard.service.ExpenseService.BudgetAlert;
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
 * Panel that lists expenses and provides add/edit/delete actions.
 */
public class ExpensesPanel extends JPanel {

    private final ExpenseService service = new ExpenseService();

    private JTable          table;
    private DefaultTableModel model;
    private List<Expense>   expenses = new ArrayList<>();

    private JComboBox<String> cbMonthFilter;
    private JLabel            lblTotal;

    private static final String[] COLUMNS = {
        "ID", "Date", "Category", "Description", "Amount", "Notes"
    };

    public ExpensesPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.SURFACE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        buildUI();
        refreshData();
    }

    private void buildUI() {
        // ── Top bar ──────────────────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);

        JLabel title = UITheme.titleLabel("💸  Expenses");
        top.add(title, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);

        cbMonthFilter = new JComboBox<>();
        populateMonthFilter();
        cbMonthFilter.addActionListener(e -> refreshData());
        controls.add(new JLabel("Month:"));
        controls.add(cbMonthFilter);

        JButton btnAdd    = UITheme.primaryButton("+ Add");
        JButton btnEdit   = UITheme.secondaryButton("✏ Edit");
        JButton btnDelete = UITheme.dangerButton("🗑 Delete");

        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());

        controls.add(btnAdd);
        controls.add(btnEdit);
        controls.add(btnDelete);
        top.add(controls, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);

        // ── Table ────────────────────────────────────────────────────────────
        model = new DefaultTableModel(COLUMNS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setFont(UITheme.FONT_BODY);
        table.setRowHeight(28);
        table.getTableHeader().setFont(UITheme.FONT_BODY);
        table.getTableHeader().setBackground(UITheme.PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setGridColor(UITheme.BORDER_COLOR);
        table.setShowVerticalLines(false);

        // Hide ID column
        table.getColumnModel().getColumn(0).setMinWidth(0);
        table.getColumnModel().getColumn(0).setMaxWidth(0);
        table.getColumnModel().getColumn(0).setWidth(0);

        // Amount column right-aligned
        DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
        rightAlign.setHorizontalAlignment(SwingConstants.RIGHT);
        table.getColumnModel().getColumn(4).setCellRenderer(rightAlign);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));
        add(sp, BorderLayout.CENTER);

        // ── Footer ───────────────────────────────────────────────────────────
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.setOpaque(false);
        lblTotal = new JLabel("Total: $0.00");
        lblTotal.setFont(UITheme.FONT_H2);
        lblTotal.setForeground(UITheme.PRIMARY);
        footer.add(lblTotal);
        add(footer, BorderLayout.SOUTH);
    }

    private void populateMonthFilter() {
        cbMonthFilter.addItem("All");
        YearMonth ym = YearMonth.now();
        for (int i = 0; i < 12; i++) {
            cbMonthFilter.addItem(ym.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        }
    }

    public void refreshData() {
        try {
            String selected = (String) cbMonthFilter.getSelectedItem();
            if ("All".equals(selected)) {
                expenses = service.getAllExpenses();
            } else {
                expenses = service.getExpensesByMonth(YearMonth.parse(selected));
            }
            populateTable();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading expenses: " + ex.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void populateTable() {
        model.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (Expense e : expenses) {
            model.addRow(new Object[]{
                e.getId(),
                e.getExpenseDate(),
                e.getCategoryName(),
                e.getDescription(),
                CurrencyFormatter.format(e.getAmount()),
                e.getNotes() != null ? e.getNotes() : ""
            });
            if (e.getAmount() != null) total = total.add(e.getAmount());
        }
        lblTotal.setText("Total: " + CurrencyFormatter.format(total));
    }

    private void onAdd() {
        Expense e = new Expense();
        ExpenseFormDialog dlg = new ExpenseFormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), e);
        dlg.setVisible(true);
        if (!dlg.isSaved()) return;
        try {
            BudgetAlert alert = service.saveExpense(dlg.getExpense());
            refreshData();
            showAlert(alert);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an expense to edit."); return; }
        Expense e = expenses.get(row);
        ExpenseFormDialog dlg = new ExpenseFormDialog(
            (Frame) SwingUtilities.getWindowAncestor(this), e);
        dlg.setVisible(true);
        if (!dlg.isSaved()) return;
        try {
            service.updateExpense(dlg.getExpense());
            refreshData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Update failed: " + ex.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an expense to delete."); return; }
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete this expense?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;
        try {
            service.deleteExpense(expenses.get(row).getId());
            refreshData();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Delete failed: " + ex.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showAlert(BudgetAlert alert) {
        if (alert == null) return;
        int msgType = alert.getType() == BudgetAlert.Type.EXCEEDED
                      ? JOptionPane.ERROR_MESSAGE
                      : JOptionPane.WARNING_MESSAGE;
        JOptionPane.showMessageDialog(this, alert.getMessage(), "Budget Alert", msgType);
    }
}
