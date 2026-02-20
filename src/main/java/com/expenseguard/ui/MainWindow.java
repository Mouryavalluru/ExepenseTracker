package com.expenseguard.ui;

import com.expenseguard.util.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * Main JFrame – tabbed shell containing all panels.
 */
public class MainWindow extends JFrame {

    private ExpensesPanel   expensesPanel;
    private BudgetPanel     budgetPanel;
    private ChartsPanel     chartsPanel;
    private CategoriesPanel categoriesPanel;

    public MainWindow() {
        setTitle("💰 Personal Expense & Budget Guard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1100, 700));
        setMinimumSize(new Dimension(900, 580));
        setLocationRelativeTo(null);
        buildUI();
        pack();
    }

    private void buildUI() {
        UITheme.applyDefaults();
        getContentPane().setBackground(UITheme.SURFACE);

        // ── Sidebar / tab strip ──────────────────────────────────────────────
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(UITheme.FONT_BODY);
        tabs.setBackground(UITheme.SURFACE);

        expensesPanel   = new ExpensesPanel();
        budgetPanel     = new BudgetPanel();
        chartsPanel     = new ChartsPanel();
        categoriesPanel = new CategoriesPanel();

        tabs.addTab("💸  Expenses",   expensesPanel);
        tabs.addTab("🛡  Budgets",    budgetPanel);
        tabs.addTab("📊  Reports",    chartsPanel);
        tabs.addTab("🏷  Categories", categoriesPanel);

        // Refresh charts/budgets when switching to those tabs
        tabs.addChangeListener(e -> {
            int idx = tabs.getSelectedIndex();
            if (idx == 1) budgetPanel.refreshData();
            if (idx == 2) chartsPanel.refreshData();
            if (idx == 3) categoriesPanel.refreshData();
        });

        setContentPane(tabs);
    }
}
