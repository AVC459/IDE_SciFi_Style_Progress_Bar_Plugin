package com.scifi.progress.settings;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

/**
 * 颜色选择控件：左侧色块 + 「选择…」按钮，弹出系统颜色选择器。
 * 使用纯 Swing 实现，与具体 LAF 无关。
 */
public class ColorWell extends JPanel {

    private Color color = Color.GRAY;
    private final JPanel swatch;

    public ColorWell(String title) {
        super(new BorderLayout(8, 0));
        setOpaque(false);
        swatch = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(color);
                    g2.fillRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 5, 5);
                    g2.setColor(new Color(0, 0, 0, 90));
                    g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 5, 5);
                } finally {
                    g2.dispose();
                }
            }
        };
        swatch.setPreferredSize(new Dimension(30, 20));
        swatch.setMinimumSize(new Dimension(30, 20));

        JButton button = new JButton("\u9009\u62E9\u2026"); // 选择…
        button.addActionListener(e -> {
            Color picked = JColorChooser.showDialog(ColorWell.this, title, color);
            if (picked != null) {
                color = picked;
                swatch.repaint();
            }
        });

        add(swatch, BorderLayout.WEST);
        add(button, BorderLayout.CENTER);
        setToolTipText(title);
    }

    public void setColor(Color c) {
        this.color = Objects.requireNonNullElse(c, Color.GRAY);
        swatch.repaint();
    }

    public Color getColor() {
        return color;
    }
}
