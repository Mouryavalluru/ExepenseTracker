package com.expenseguard.ui;

import com.expenseguard.dao.CategoryDAO;
import com.expenseguard.model.Category;
import com.expenseguard.model.Expense;
import com.expenseguard.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Modal dialog for adding or editing an expense.
 */
public class ExpenseFormDialog extends JDialog {

    private final Expense        expense;
    private boolean              saved = false;

    private JComboBox<Category>  cbCategory;
    private JTextField           tfDescription;
    private JTextField           tfAmount;
    private JTextField           tfDate;
    private JTextArea            taaNotes;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ExpenseFormDialog(Frame owner, Expense expense) {
        super(owner, expense.getId() == 0 ? "Add Expense" : "Edit Expense", true);
        this.expense = expense;
        buildUI();
        populateCategories();
        if (expense.getId() != 0) prefill();
        pack();
        setLocationRelativeTo(owner);
        setResizable(false);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UITheme.CARD);
        root.setBorder(BorderFactory.createEmptyBorder(20, 24, 16, 24));

        JLabel title = UITheme.titleLabel(expense.getId() == 0 ? "➕  New Expense" : "✏  Edit Expense");
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        root.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UITheme.CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 0, 5, 10);
        gbc.fill   = GridBagConstraints.HORIZONTAL;

        cbCategory    = new JComboBox<>();
        tfDescription = UITheme.styledField();
        tfAmount      = UITheme.styledField();
        tfDate        = UITheme.styledField();
        tfDate.setText(LocalDate.now().format(DATE_FMT));
        taaNotes      = new JTextArea(3, 20);
        taaNotes.setFont(UITheme.FONT_BODY);
        taaNotes.setLineWrap(true);
        taaNotes.setWrapStyleWord(true);
        JScrollPane spNotes = new JScrollPane(taaNotes);

        addRow(form, gbc, 0, "Category *",    cbCategory);
        addRow(form, gbc, 1, "Description *", tfDescription);
        addRow(form, gbc, 2, "Amount ($) *",  tfAmount);
        addRow(form, gbc, 3, "Date (YYYY-MM-DD) *", tfDate);
        addRow(form, gbc, 4, "Notes",          spNotes);

        root.add(form, BorderLayout.CENTER);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setBackground(UITheme.CARD);
        btns.setBorder(BorderFactory.createEmptyBorder(16, 0, 0, 0));

        JButton btnCancel = UITheme.secondaryButton("Cancel");
        JButton btnSave   = UITheme.primaryButton("Save Expense");

        btnCancel.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> onSave());

        btns.add(btnCancel);
        btns.add(btnSave);
        root.add(btns, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void addRow(JPanel p, GridBagConstraints gbc, int row, String label, Component field) {
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BODY);
        lbl.setForeground(UITheme.TEXT_MUTED);
        p.add(lbl, gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        p.add(field, gbc);
    }

    private void populateCategories() {
        try {
            List<Category> cats = new CategoryDAO().findAll();
            for (Category c : cats) cbCategory.addItem(c);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load categories: " + ex.getMessage(),
                                          "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void prefill() {
        tfDescription.setText(expense.getDescription());
        tfAmount.setText(expense.getAmount().toPlainString());
        tfDate.setText(expense.getExpenseDate().format(DATE_FMT));
        if (expense.getNotes() != null) taaNotes.setText(expense.getNotes());
        for (int i = 0; i < cbCategory.getItemCount(); i++) {
            if (cbCategory.getItemAt(i).getId() == expense.getCategoryId()) {
                cbCategory.setSelectedIndex(i);
                break;
            }
        }
    }

    private void onSave() {
        String desc   = tfDescription.getText().trim();
        String amtStr = tfAmount.getText().trim();
        String dateStr = tfDate.getText().trim();

        if (desc.isEmpty() || amtStr.isEmpty() || dateStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.",
                                          "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        BigDecimal amount;
        try {
            amount = new BigDecimal(amtStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Amount must be a positive number.",
                                          "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        LocalDate date;
        try {
            date = LocalDate.parse(dateStr, DATE_FMT);
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Date must be in YYYY-MM-DD format.",
                                          "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Category cat = (Category) cbCategory.getSelectedItem();
        if (cat == null) {
            JOptionPane.showMessageDialog(this, "Please select a category.",
                                          "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        expense.setCategoryId(cat.getId());
        expense.setCategoryName(cat.getName());
        expense.setDescription(desc);
        expense.setAmount(amount);
        expense.setExpenseDate(date);
        expense.setNotes(taaNotes.getText().trim());

        saved = true;
        dispose();
    }

    public boolean isSaved() { return saved; }
    public Expense getExpense() { return expense; }
}
