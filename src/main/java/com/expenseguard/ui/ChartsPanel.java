package com.expenseguard.ui;

import com.expenseguard.service.ExpenseService;
import com.expenseguard.util.CurrencyFormatter;
import com.expenseguard.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Panel showing visual spending reports (bar chart + pie chart).
 */
public class ChartsPanel extends JPanel {

    private final ExpenseService service = new ExpenseService();

    private JComboBox<String>   cbMonth;
    private BarChartCanvas      barChart;
    private PieChartCanvas      pieChart;
    private JPanel              legendPanel;

    // Distinct colours for up to 8 categories
    private static final Color[] PALETTE = {
        new Color(37,  99,  235), new Color(22,  163, 74),
        new Color(220, 38,  38),  new Color(217, 119, 6),
        new Color(139, 92,  246), new Color(236, 72,  153),
        new Color(20,  184, 166), new Color(251, 146, 60)
    };

    public ChartsPanel() {
        setLayout(new BorderLayout(0, 12));
        setBackground(UITheme.SURFACE);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        buildUI();
        refreshData();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UITheme.titleLabel("📊  Spending Reports"), BorderLayout.WEST);

        JPanel ctrl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        ctrl.setOpaque(false);
        cbMonth = new JComboBox<>();
        YearMonth cur = YearMonth.now();
        for (int i = 0; i < 12; i++)
            cbMonth.addItem(cur.minusMonths(i).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        cbMonth.addActionListener(e -> refreshData());
        ctrl.add(new JLabel("Month:"));
        ctrl.add(cbMonth);
        top.add(ctrl, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        split.setResizeWeight(0.6);
        split.setDividerSize(6);
        split.setBorder(null);
        split.setOpaque(false);

        barChart = new BarChartCanvas();
        pieChart = new PieChartCanvas();

        split.setLeftComponent(wrap(barChart, "Spending by Category"));
        split.setRightComponent(wrap(pieChart, "Distribution"));
        add(split, BorderLayout.CENTER);

        legendPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        legendPanel.setOpaque(false);
        add(legendPanel, BorderLayout.SOUTH);
    }

    private JPanel wrap(JComponent c, String title) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UITheme.CARD);
        p.setBorder(UITheme.cardBorder());
        JLabel lbl = UITheme.sectionLabel(title);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        p.add(lbl, BorderLayout.NORTH);
        p.add(c, BorderLayout.CENTER);
        return p;
    }

    public void refreshData() {
        try {
            String month = (String) cbMonth.getSelectedItem();
            List<Object[]> data = service.getMonthlyCategorySummary(month);
            barChart.setData(data);
            pieChart.setData(data);
            buildLegend(data);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error loading chart data: " + ex.getMessage());
        }
    }

    private void buildLegend(List<Object[]> data) {
        legendPanel.removeAll();
        for (int i = 0; i < data.size(); i++) {
            Object[] row = data.get(i);
            String cat   = (String) row[1];
            BigDecimal amt = (BigDecimal) row[2];
            Color color = PALETTE[i % PALETTE.length];

            JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
            item.setOpaque(false);
            JPanel swatch = new JPanel();
            swatch.setBackground(color);
            swatch.setPreferredSize(new Dimension(12, 12));
            JLabel lbl = new JLabel(cat + " (" + CurrencyFormatter.format(amt) + ")");
            lbl.setFont(UITheme.FONT_SMALL);
            item.add(swatch);
            item.add(lbl);
            legendPanel.add(item);
        }
        legendPanel.revalidate();
        legendPanel.repaint();
    }

    // ── Bar chart ────────────────────────────────────────────────────────────
    private class BarChartCanvas extends JPanel {
        private List<Object[]> data;

        BarChartCanvas() {
            setBackground(UITheme.CARD);
            setPreferredSize(new Dimension(400, 260));
        }

        void setData(List<Object[]> d) { this.data = d; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) {
                g.setColor(UITheme.TEXT_MUTED);
                g.drawString("No data", getWidth()/2 - 25, getHeight()/2);
                return;
            }
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int pad = 40, bottom = 30;
            int w = getWidth() - 2*pad, h = getHeight() - pad - bottom;
            BigDecimal max = data.stream().map(r -> (BigDecimal)r[2])
                                 .max(BigDecimal::compareTo).orElse(BigDecimal.ONE);
            if (max.compareTo(BigDecimal.ZERO) == 0) max = BigDecimal.ONE;

            int n = data.size();
            int barW = Math.max(20, w / (n*2 + 1));
            int gap  = barW;

            for (int i = 0; i < n; i++) {
                BigDecimal amt = (BigDecimal) data.get(i)[2];
                String cat    = (String) data.get(i)[1];
                Color color   = PALETTE[i % PALETTE.length];

                double ratio = amt.doubleValue() / max.doubleValue();
                int barH = (int)(ratio * h);
                int x = pad + gap + i*(barW + gap);
                int y = pad + h - barH;

                g2.setColor(color);
                g2.fillRoundRect(x, y, barW, barH, 4, 4);

                // amount label
                g2.setColor(UITheme.TEXT_PRIMARY);
                g2.setFont(UITheme.FONT_SMALL);
                String amtLabel = "$" + String.format("%.0f", amt.doubleValue());
                g2.drawString(amtLabel, x + barW/2 - g2.getFontMetrics().stringWidth(amtLabel)/2, y - 4);

                // category label (truncate)
                String label = cat.length() > 8 ? cat.substring(0,7) + "…" : cat;
                g2.drawString(label, x + barW/2 - g2.getFontMetrics().stringWidth(label)/2,
                              pad + h + 16);
            }

            // Baseline
            g2.setColor(UITheme.BORDER_COLOR);
            g2.drawLine(pad, pad + h, pad + w, pad + h);
        }
    }

    // ── Pie chart ────────────────────────────────────────────────────────────
    private class PieChartCanvas extends JPanel {
        private List<Object[]> data;

        PieChartCanvas() {
            setBackground(UITheme.CARD);
            setPreferredSize(new Dimension(240, 240));
        }

        void setData(List<Object[]> d) { this.data = d; repaint(); }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (data == null || data.isEmpty()) return;
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double total = data.stream().mapToDouble(r -> ((BigDecimal)r[2]).doubleValue()).sum();
            if (total == 0) return;

            int margin = 20;
            int size   = Math.min(getWidth(), getHeight()) - 2*margin;
            int x = (getWidth() - size)/2, y = (getHeight() - size)/2;

            double start = -90;
            for (int i = 0; i < data.size(); i++) {
                double val   = ((BigDecimal) data.get(i)[2]).doubleValue();
                double sweep = (val / total) * 360.0;
                g2.setColor(PALETTE[i % PALETTE.length]);
                g2.fill(new Arc2D.Double(x, y, size, size, start, sweep, Arc2D.PIE));
                start += sweep;
            }

            // White circle in center (donut look)
            int inner = size / 3;
            g2.setColor(UITheme.CARD);
            g2.fillOval(x + size/2 - inner/2, y + size/2 - inner/2, inner, inner);

            // Total label
            g2.setColor(UITheme.TEXT_PRIMARY);
            g2.setFont(UITheme.FONT_SMALL);
            String lbl = CurrencyFormatter.format(total);
            g2.drawString(lbl, x + size/2 - g2.getFontMetrics().stringWidth(lbl)/2,
                          y + size/2 + g2.getFontMetrics().getAscent()/2);
        }
    }
}
