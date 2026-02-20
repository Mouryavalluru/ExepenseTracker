package com.expenseguard;

import com.expenseguard.db.DatabaseConnection;
import com.expenseguard.db.SchemaInitializer;
import com.expenseguard.ui.MainWindow;

import javax.swing.*;
import java.util.logging.Logger;

/**
 * Application entry point.
 */
public class App {

    private static final Logger LOGGER = Logger.getLogger(App.class.getName());

    public static void main(String[] args) {
        // 1. Set system look & feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // 2. Connect to database and initialise schema
        try {
            DatabaseConnection.getInstance();
            SchemaInitializer.initialize();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(null,
                "Cannot connect to the database.\n\n" + ex.getMessage() +
                "\n\nPlease check your PostgreSQL configuration in:\n" +
                "src/main/java/com/expenseguard/db/DatabaseConnection.java",
                "Database Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        // 3. Launch UI on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
            LOGGER.info("Application started successfully.");
        });
    }
}
