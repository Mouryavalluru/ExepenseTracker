# 💰 Personal Expense & Budget Guard System

A Java Swing desktop application for tracking daily expenses and managing monthly budgets, backed by PostgreSQL.

---

## ✅ Features

| Feature | Details |
|---|---|
| **Expense Tracking** | Add, edit, delete expenses with category, description, amount, date, and notes |
| **Budget Guard** | Set per-category monthly limits; auto-detects exceeded or near-limit budgets |
| **Smart Alerts** | Pop-up warnings when you reach 80 % (⚡ Near Limit) or 100 % (❌ Exceeded) |
| **Visual Reports** | Bar chart and donut pie chart for monthly spending by category |
| **Categories** | Full CRUD for expense categories; 8 defaults seeded on first run |

---

## 🛠 Tech Stack

- **Java 17** + **Swing** (GUI)
- **PostgreSQL 12+** (database)
- **JDBC** (connectivity via `org.postgresql` driver)
- **Maven** (build & dependency management)
- **OOP Principles** — Layered architecture: Model → DAO → Service → UI

---

## 📁 Project Structure

```
ExpenseBudgetGuard/
├── pom.xml                          Maven build file
├── schema.sql                       Raw SQL schema (optional manual setup)
└── src/main/java/com/expenseguard/
    ├── App.java                     Entry point
    ├── db/
    │   ├── DatabaseConnection.java  Singleton JDBC connection
    │   └── SchemaInitializer.java   Creates tables on first run
    ├── model/
    │   ├── Category.java
    │   ├── Expense.java
    │   └── Budget.java
    ├── dao/
    │   ├── CategoryDAO.java
    │   ├── ExpenseDAO.java
    │   └── BudgetDAO.java
    ├── service/
    │   └── ExpenseService.java      Business logic + BudgetAlert
    ├── ui/
    │   ├── MainWindow.java          JFrame shell
    │   ├── ExpensesPanel.java       Expenses tab
    │   ├── BudgetPanel.java         Budget management tab
    │   ├── ChartsPanel.java         Reports/charts tab
    │   ├── CategoriesPanel.java     Categories tab
    │   └── ExpenseFormDialog.java   Add/edit expense dialog
    └── util/
        ├── UITheme.java             Colours, fonts, factory helpers
        └── CurrencyFormatter.java   USD formatter
```

---

## ⚙️ Setup Instructions

### 1. Prerequisites
- **Java 17+** – [Download](https://adoptium.net/)
- **Maven 3.8+** – [Download](https://maven.apache.org/)
- **PostgreSQL 12+** – [Download](https://www.postgresql.org/)

### 2. Create the Database

```sql
-- In psql or pgAdmin:
CREATE DATABASE expense_guard;
```

The tables are **auto-created** by `SchemaInitializer.java` on first run.
Alternatively run `schema.sql` manually.

### 3. Configure the Connection

Open `src/main/java/com/expenseguard/db/DatabaseConnection.java` and update:

```java
private static final String URL      = "jdbc:postgresql://localhost:5432/expense_guard";
private static final String USERNAME = "postgres";        // your PostgreSQL username
private static final String PASSWORD = "your_password_here"; // your PostgreSQL password
```

### 4. Build & Run

```bash
# Build a runnable fat JAR:
mvn clean package

# Run:
java -jar target/expense-budget-guard-1.0.0.jar
```

Or run directly from your IDE by executing `App.main()`.

---

## 🚀 How to Use

| Tab | What to do |
|---|---|
| **💸 Expenses** | Click **+ Add** to log an expense. Select month in the dropdown to filter. Edit or delete with the respective buttons. |
| **🛡 Budgets** | Select a month, click **+ Set Budget**, pick a category and enter a limit. The table shows spent/remaining with colour coding. |
| **📊 Reports** | Bar chart + donut chart for the selected month. Automatically updated. |
| **🏷 Categories** | Manage spending categories. 8 defaults are seeded on first run. |

---

## 🎨 OOP Principles Demonstrated

- **Encapsulation** – Models with private fields and getters/setters
- **Abstraction** – DAO layer hides SQL from service/UI layers
- **Separation of Concerns** – Distinct layers (Model, DAO, Service, UI)
- **Single Responsibility** – Each class has one clear purpose
- **Singleton Pattern** – `DatabaseConnection` ensures one shared connection

---

## 📋 Requirements (for grading)

- ✅ Java desktop application (Swing)
- ✅ PostgreSQL database with JDBC
- ✅ Expense tracking (add/edit/delete)
- ✅ Expense categorisation
- ✅ Monthly budget monitoring with alerts
- ✅ Charts & visual reports
- ✅ Object-Oriented Programming principles
- ✅ Real-world financial problem solving



# APPLICATION VIEW

- <img width="1621" height="1039" alt="image" src="https://github.com/user-attachments/assets/52c7f8f6-a218-4c1d-8ef8-d82e55929441" />
- <img width="1631" height="1040" alt="image" src="https://github.com/user-attachments/assets/30bebf15-19e3-46d2-a3f8-d982bdb67216" />
- <img width="1618" height="1032" alt="image" src="https://github.com/user-attachments/assets/b668355d-c3d2-4e0c-9814-c1f1e6503e95" />
- <img width="1622" height="1046" alt="image" src="https://github.com/user-attachments/assets/08161122-6d36-44f9-bce0-bf844688d92b" />






