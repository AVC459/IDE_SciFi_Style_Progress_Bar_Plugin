package com.scifi.progress.settings;

import com.scifi.progress.ProgressBarSkinner;

import javax.swing.*;
import java.awt.*;

/**
 * 科幻风进度条皮肤设置面板（纯 Swing，GridBagLayout）。
 */
public class SciFiSettingsPanel {

    private final JPanel root = new JPanel(new GridBagLayout());
    private final JCheckBox enabledBox = new JCheckBox("\u542F\u7528\u79D1\u5E7B\u98CE\u8FDB\u5EA6\u6761\u76AE\u80A4"); // 启用科幻风进度条皮肤
    private final ColorWell startWell = new ColorWell("\u6E10\u53D8\u8D77\u59CB\u8272"); // 渐变起始色
    private final ColorWell endWell = new ColorWell("\u6E10\u53D8\u7ED3\u675F\u8272");   // 渐变结束色
    private final JCheckBox showTextBox = new JCheckBox("indeterminate \u6A21\u5F0F\u663E\u793A\u6EDA\u52A8\u6587\u5B57"); // 显示滚动文字
    private final JTextField textField = new JTextField(16);
    private final JSlider speedSlider = new JSlider(1, 5, 3);
    private final JLabel speedLabel = new JLabel("3");

    public SciFiSettingsPanel() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 3;
        g.weightx = 1;
        root.add(enabledBox, g);
        row++;

        g.gridwidth = 1;
        g.weightx = 0;
        g.gridx = 0;
        g.gridy = row;
        root.add(new JLabel("\u6E10\u53D8\u8D77\u59CB\u8272\uFF1A"), g); // 渐变起始色：
        g.gridx = 1;
        root.add(startWell, g);
        g.gridx = 2;
        root.add(new JLabel("  #2563EB \u79D1\u5E7B\u84DD"), g); // 科幻蓝
        row++;

        g.gridx = 0;
        g.gridy = row;
        root.add(new JLabel("\u6E10\u53D8\u7ED3\u675F\u8272\uFF1A"), g); // 渐变结束色：
        g.gridx = 1;
        root.add(endWell, g);
        g.gridx = 2;
        root.add(new JLabel("  #10B981 \u4EE3\u7801\u7EFF"), g); // 代码绿
        row++;

        g.gridx = 0;
        g.gridy = row;
        g.gridwidth = 3;
        root.add(showTextBox, g);
        g.gridwidth = 1;
        row++;

        g.gridx = 0;
        g.gridy = row;
        root.add(new JLabel("\u6EDA\u52A8\u6587\u6848\uFF1A"), g); // 滚动文案：
        g.gridx = 1;
        g.gridwidth = 2;
        root.add(textField, g);
        g.gridwidth = 1;
        row++;

        g.gridx = 0;
        g.gridy = row;
        root.add(new JLabel("\u52A8\u753B\u901F\u5EA6\uFF1A"), g); // 动画速度：
        g.gridx = 1;
        g.weightx = 1;
        root.add(speedSlider, g);
        g.gridx = 2;
        g.weightx = 0;
        root.add(speedLabel, g);
        row++;

        speedSlider.addChangeListener(e -> speedLabel.setText(String.valueOf(speedSlider.getValue())));

        reset();
    }

    public JComponent getRoot() {
        return root;
    }

    public boolean isModified() {
        SciFiSettingsState st = SciFiSettingsState.getInstance();
        return enabledBox.isSelected() != st.enabled
                || !startWell.getColor().equals(parse(st.startColor, "2563EB"))
                || !endWell.getColor().equals(parse(st.endColor, "10B981"))
                || showTextBox.isSelected() != st.showText
                || !textField.getText().equals(st.scrollingText)
                || speedSlider.getValue() != st.speed;
    }

    public void apply() {
        SciFiSettingsState st = SciFiSettingsState.getInstance();
        st.enabled = enabledBox.isSelected();
        st.startColor = toHex(startWell.getColor());
        st.endColor = toHex(endWell.getColor());
        st.showText = showTextBox.isSelected();
        st.scrollingText = textField.getText();
        st.speed = speedSlider.getValue();
        ProgressBarSkinner.applyToAll();
    }

    public void reset() {
        SciFiSettingsState st = SciFiSettingsState.getInstance();
        enabledBox.setSelected(st.enabled);
        startWell.setColor(parse(st.startColor, st.startColor));
        endWell.setColor(parse(st.endColor, st.endColor));
        showTextBox.setSelected(st.showText);
        textField.setText(st.scrollingText);
        speedSlider.setValue(Math.max(1, Math.min(5, st.speed)));
        speedLabel.setText(String.valueOf(st.speed));
    }

    private static java.awt.Color parse(String hex, String def) {
        try {
            return com.intellij.ui.ColorUtil.fromHex(hex == null || hex.trim().isEmpty() ? def : hex.trim());
        } catch (Exception e) {
            return com.intellij.ui.ColorUtil.fromHex(def);
        }
    }

    private static String toHex(java.awt.Color c) {
        return String.format("%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }
}
