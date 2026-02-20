package com.expenseguard.service;

import com.expenseguard.dao.BudgetDAO;
import com.expenseguard.dao.ExpenseDAO;
import com.expenseguard.model.Budget;
import com.expenseguard.model.Expense;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Business logic layer for expenses + budget alerts.
 */
public class ExpenseService {

    private final ExpenseDAO expenseDAO = new ExpenseDAO();
    private final BudgetDAO  budgetDAO  = new BudgetDAO();

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM");

    /**
     * Saves an expense and returns a BudgetAlert if the category budget is
     * at or near its limit, or null if no alert is needed.
     */
    public BudgetAlert saveExpense(Expense expense) throws SQLException {
        expenseDAO.save(expense);
        return checkBudget(expense.getCategoryId(),
                           YearMonth.from(expense.getExpenseDate()).format(FMT));
    }

    public void updateExpense(Expense expense) throws SQLException {
        expenseDAO.save(expense);
    }

    public void deleteExpense(int id) throws SQLException {
        expenseDAO.delete(id);
    }

    public List<Expense> getAllExpenses() throws SQLException {
        return expenseDAO.findAll();
    }

    public List<Expense> getExpensesByMonth(YearMonth ym) throws SQLException {
        return expenseDAO.findByMonth(ym);
    }

    public List<Object[]> getMonthlyCategorySummary(String monthYear) throws SQLException {
        return expenseDAO.monthlyCategorySummary(monthYear);
    }

    public BudgetAlert checkBudget(int categoryId, String monthYear) throws SQLException {
        Budget budget = budgetDAO.findByCategoryAndMonth(categoryId, monthYear);
        if (budget == null) return null;

        BigDecimal spent = expenseDAO.sumByCategoryAndMonth(categoryId, monthYear);
        budget.setSpentAmount(spent);

        if (budget.isExceeded()) {
            return new BudgetAlert(budget, BudgetAlert.Type.EXCEEDED);
        } else if (budget.isNearLimit()) {
            return new BudgetAlert(budget, BudgetAlert.Type.NEAR_LIMIT);
        }
        return null;
    }

    // ── Budget CRUD ──────────────────────────────────────────────────────────

    public List<Budget> getBudgetsForMonth(String monthYear) throws SQLException {
        List<Budget> budgets = budgetDAO.findByMonth(monthYear);
        for (Budget b : budgets) {
            BigDecimal spent = expenseDAO.sumByCategoryAndMonth(b.getCategoryId(), monthYear);
            b.setSpentAmount(spent);
        }
        return budgets;
    }

    public void saveBudget(Budget budget) throws SQLException {
        budgetDAO.save(budget);
    }

    public void deleteBudget(int id) throws SQLException {
        budgetDAO.delete(id);
    }

    // ── Inner class for alerts ───────────────────────────────────────────────
    public static class BudgetAlert {
        public enum Type { NEAR_LIMIT, EXCEEDED }

        private final Budget budget;
        private final Type   type;

        public BudgetAlert(Budget budget, Type type) {
            this.budget = budget;
            this.type   = type;
        }

        public Budget getBudget() { return budget; }
        public Type   getType()   { return type;   }

        public String getMessage() {
            if (type == Type.EXCEEDED) {
                return String.format(
                    "⚠ BUDGET EXCEEDED for %s!\n" +
                    "Limit: $%.2f  |  Spent: $%.2f  |  Over by: $%.2f",
                    budget.getCategoryName(),
                    budget.getLimitAmount(),
                    budget.getSpentAmount(),
                    budget.getSpentAmount().subtract(budget.getLimitAmount()));
            } else {
                return String.format(
                    "⚡ Budget Warning for %s\n" +
                    "You've used %.1f%% of your $%.2f budget.\n" +
                    "Remaining: $%.2f",
                    budget.getCategoryName(),
                    budget.getUsagePercent(),
                    budget.getLimitAmount(),
                    budget.getRemainingAmount());
            }
        }
    }
}
