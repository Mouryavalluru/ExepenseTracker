package com.expenseguard.util;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;

/**
 * Shared colours, fonts, and factory methods for consistent UI styling.
 */
public final class UITheme {

    // Palette
    public static final Color PRIMARY      = new Color(37, 99, 235);   // blue-600
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);   // blue-700
    public static final Color SUCCESS      = new Color(22, 163, 74);   // green-600
    public static final Color DANGER       = new Color(220, 38,  38);  // red-600
    public static final Color WARNING      = new Color(217, 119, 6);   // amber-600
    public static final Color SURFACE      = new Color(248, 250, 252); // slate-50
    public static final Color CARD         = Color.WHITE;
    public static final Color BORDER_COLOR = new Color(226, 232, 240); // slate-200
    public static final Color TEXT_PRIMARY = new Color(15, 23, 42);    // slate-900
    public static final Color TEXT_MUTED   = new Color(100, 116, 139); // slate-500

    // Fonts
    public static final Font FONT_TITLE  = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_H2     = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FONT_BODY   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL  = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO   = new Font("Consolas",  Font.PLAIN, 13);

    private UITheme() {}

    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    public static JButton dangerButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(DANGER);
        return btn;
    }

    public static JButton secondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(BORDER_COLOR);
        btn.setForeground(TEXT_PRIMARY);
        btn.setFont(FONT_BODY);
        btn.setFocusPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    public static Border cardBorder() {
        return BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1, true),
            BorderFactory.createEmptyBorder(12, 14, 12, 14));
    }

    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static JLabel sectionLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_H2);
        lbl.setForeground(TEXT_PRIMARY);
        return lbl;
    }

    public static JTextField styledField() {
        JTextField tf = new JTextField();
        tf.setFont(FONT_BODY);
        tf.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)));
        return tf;
    }

    public static JComboBox<?> styledCombo() {
        JComboBox<?> cb = new JComboBox<>();
        cb.setFont(FONT_BODY);
        cb.setBackground(Color.WHITE);
        return cb;
    }

    /** Applies FlatLaf-compatible global defaults */
    public static void applyDefaults() {
        UIManager.put("Panel.background", SURFACE);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_BODY);
        UIManager.put("Button.font", FONT_BODY);
        UIManager.put("TabbedPane.font", FONT_BODY);
    }
}
