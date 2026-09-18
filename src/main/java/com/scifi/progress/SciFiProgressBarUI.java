package com.scifi.progress;

import com.intellij.ui.ColorUtil;
import com.intellij.util.ui.UIUtil;
import com.scifi.progress.settings.SciFiSettingsState;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicProgressBarUI;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;

/**
 * 科幻风进度条皮肤（Sci-Fi Style Progress Bar）。
 *
 * <p>determinate：浅灰圆角轨道 + 科幻蓝→代码绿渐变填充，机器人头像随进度移动，到 100% 弹对勾动画。</p>
 * <p>indeterminate：机器人左右往复滑行，底部小字滚动（默认「正在读取」）。</p>
 *
 * <p>完全使用 Swing Graphics2D 矢量绘制，不依赖任何外部动图资源，兼容所有官方主题。
 * 图标尺寸随进度条高度自动缩放（状态栏内联进度条等矮进度条不会被裁剪）。</p>
 */
public class SciFiProgressBarUI extends BasicProgressBarUI {

    private static final Color ACCENT_START = new Color(0x25, 0x63, 0xEB); // #2563EB 科幻蓝
    private static final Color ACCENT_END   = new Color(0x10, 0xB9, 0x81); // #10B981 代码绿

    // ---------- 动画状态 ----------
    private javax.swing.Timer animTimer;   // 仅在需要时运行（indeterminate 或 对勾动画中）
    private long animStart;                // 本实例动画起始墙钟时间
    private float checkPhase = -1f;        // -1 未激活；0..1 对勾动画进度；1 完成（静态对勾）
    private long lastPaintMs = 0;

    // ---------- 生命周期 ----------

    /**
     * 关键：UIManager 反射调用此静态方法创建 UI 实例（而不是继承 BasicProgressBarUI 的 createUI，
     * 否则普通 JProgressBar 会拿到 BasicProgressBarUI 而显示默认白色宽条）。
     */
    @SuppressWarnings("unused")
    public static ComponentUI createUI(JComponent c) {
        return new SciFiProgressBarUI();
    }

    @Override
    public void installUI(@NotNull JComponent c) {
        super.installUI(c);
        animStart = System.currentTimeMillis();
        checkPhase = -1f;
        lastPaintMs = 0;
        animTimer = new javax.swing.Timer(30, e -> tick());
        animTimer.setCoalesce(true);
    }

    @Override
    public void uninstallUI(@NotNull JComponent c) {
        if (animTimer != null) {
            animTimer.stop();
            animTimer = null;
        }
        super.uninstallUI(c);
    }

    // 禁用 BasicProgressBarUI 自带的动画计时器，统一由本类驱动（避免双动画源）
    @Override
    protected void startAnimationTimer() {
    }

    @Override
    protected void stopAnimationTimer() {
    }

    private void tick() {
        if (progressBar == null) {
            return;
        }
        if (progressBar.isShowing()) {
            progressBar.repaint();
        }
        updateTimerState();
    }

    private boolean timerNeeded() {
        if (progressBar == null) {
            return false;
        }
        return progressBar.isIndeterminate() || (checkPhase >= 0f && checkPhase < 1f);
    }

    private void updateTimerState() {
        if (animTimer == null) {
            return;
        }
        boolean need = timerNeeded();
        if (need && !animTimer.isRunning()) {
            animTimer.start();
        } else if (!need && animTimer.isRunning()) {
            animTimer.stop();
        }
    }

    // ---------- 尺寸 ----------

    @Override
    public Dimension getPreferredSize(JComponent c) {
        Dimension d = super.getPreferredSize(c);
        int minH = 26; // 轨道 + 机器人 + 底部滚动小字
        if (d.height < minH) {
            d.height = minH;
        }
        return d;
    }

    // ---------- 绘制入口 ----------

    @Override
    public void paint(Graphics g, JComponent c) {
        if (!(c instanceof JProgressBar) || progressBar == null) {
            super.paint(g, c);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (progressBar.isIndeterminate()) {
                paintIndeterminateCustom(g2);
            } else {
                paintDeterminateCustom(g2);
            }
        } finally {
            g2.dispose();
        }
        updateTimerState();
    }

    // ---------- determinate ----------

    private void paintDeterminateCustom(Graphics2D g2) {
        if (progressBar.getOrientation() != SwingConstants.HORIZONTAL
                || !progressBar.getComponentOrientation().isLeftToRight()) {
            super.paintDeterminate(g2, progressBar);
            return;
        }
        SciFiSettingsState st = SciFiSettingsState.getInstance();
        Insets b = progressBar.getInsets();
        int x = b.left;
        int y = b.top;
        int w = progressBar.getWidth() - b.left - b.right;
        int h = progressBar.getHeight() - b.top - b.bottom;
        if (w <= 0 || h <= 0) {
            return;
        }
        int cy = y + h / 2;
        int trackH = Math.max(4, Math.min(h, 7));
        int trackW = w;
        float iconScale = clamp((h - 2f) / 16f, 0.5f, 1f);

        // 1) 轨道
        Color trackColor = isDark()
                ? new Color(0x3C, 0x3F, 0x41)
                : new Color(0xE4, 0xE6, 0xEB);
        RoundRectangle2D track = new RoundRectangle2D.Float(x, cy - trackH / 2f, trackW, trackH, trackH, trackH);
        g2.setColor(trackColor);
        g2.fill(track);

        // 2) 渐变填充
        double pct = pct();
        float fillW = (float) (trackW * pct);
        Color c1 = parseColor(st.startColor, ACCENT_START);
        Color c2 = parseColor(st.endColor, ACCENT_END);
        if (fillW > 0.5f) {
            Shape oldClip = g2.getClip();
            g2.clip(track);
            g2.setPaint(new GradientPaint(x, 0, c1, x + trackW, 0, c2));
            g2.fill(new RoundRectangle2D.Float(x, cy - trackH / 2f, fillW, trackH, trackH, trackH));
            // 顶部高光
            g2.setColor(new Color(255, 255, 255, 55));
            g2.fill(new RoundRectangle2D.Float(x, cy - trackH / 2f, fillW, trackH * 0.45f, trackH, trackH));
            g2.setClip(oldClip);
        }

        // 3) 头部机器人 / 对勾（头部图标固定为机器人）
        boolean atMax = progressBar.getMaximum() > progressBar.getMinimum()
                && progressBar.getValue() >= progressBar.getMaximum();
        float headX = clamp(x + fillW, x + 9 * iconScale, x + trackW - 9 * iconScale);
        if (atMax) {
            advanceCheckPhase();
            float phase = checkPhase >= 0 ? Math.min(1f, checkPhase) : 1f;
            drawCheck(g2, headX, cy, phase, iconScale);
        } else {
            checkPhase = -1f;
            lastPaintMs = 0;
            drawRobot(g2, headX, cy, iconScale, c2, 1f);
        }

        // 4) 可选的居中文字（isStringPainted 场景）
        if (progressBar.isStringPainted()) {
            paintCenteredString(g2, x, cy, trackW, h);
        }
    }

    // ---------- indeterminate ----------

    private void paintIndeterminateCustom(Graphics2D g2) {
        SciFiSettingsState st = SciFiSettingsState.getInstance();
        Insets b = progressBar.getInsets();
        int x = b.left;
        int y = b.top;
        int w = progressBar.getWidth() - b.left - b.right;
        int h = progressBar.getHeight() - b.top - b.bottom;
        if (w <= 0 || h <= 0) {
            return;
        }
        int cy = y + h / 2;
        int trackH = Math.max(4, Math.min(h, 7));
        int trackW = w;
        float iconScale = clamp((h - 2f) / 16f, 0.5f, 1f);

        // 1) 轨道
        Color trackColor = isDark()
                ? new Color(0x3C, 0x3F, 0x41)
                : new Color(0xE4, 0xE6, 0xEB);
        RoundRectangle2D track = new RoundRectangle2D.Float(x, cy - trackH / 2f, trackW, trackH, trackH, trackH);
        g2.setColor(trackColor);
        g2.fill(track);

        // 2) 往复滑动的渐变段
        Color c1 = parseColor(st.startColor, ACCENT_START);
        Color c2 = parseColor(st.endColor, ACCENT_END);
        float periodMs = Math.max(600f, 1900f - st.speed * 150f);
        float t = ((System.currentTimeMillis() - animStart) % periodMs) / periodMs;
        float ping = t < 0.5f ? t * 2f : 2f - t * 2f; // 0..1..0
        float segW = Math.max(trackW * 0.28f, 30f);
        float left = x + (trackW - segW) * ping;
        Shape oldClip = g2.getClip();
        g2.clip(track);
        g2.setPaint(new GradientPaint(x, 0, c1, x + trackW, 0, c2));
        g2.fill(new RoundRectangle2D.Float(left, cy - trackH / 2f, segW, trackH, trackH, trackH));
        g2.setColor(new Color(255, 255, 255, 55));
        g2.fill(new RoundRectangle2D.Float(left, cy - trackH / 2f, segW, trackH * 0.45f, trackH, trackH));
        g2.setClip(oldClip);

        // 3) 机器人骑在滑动段前缘，轻微上下浮动
        float robotX = clamp(left + segW, x + 9 * iconScale, x + trackW - 9 * iconScale);
        float bob = (float) Math.sin(t * Math.PI * 4f) * 0.8f * iconScale;
        drawRobot(g2, robotX, cy + bob, iconScale, c2, 1f);

        // 4) 底部滚动小字
        if (h >= 22 && st.showText) {
            String text = st.scrollingText == null || st.scrollingText.trim().isEmpty()
                    ? "\u6B63\u5728\u8BFB\u53D6" // 正在读取
                    : st.scrollingText.trim();
            paintMarquee(g2, text, x, trackW, cy + trackH / 2 + 10, 20 + st.speed * 16);
        }

        if (progressBar.isStringPainted()) {
            paintCenteredString(g2, x, cy, trackW, h);
        }
    }

    // ---------- 图标绘制 ----------

    private void drawRobot(Graphics2D g, float cx, float cy, float s, Color accent, float alpha) {
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        Color line = new Color(0x33, 0x38, 0x42);
        Color eye = new Color(0x1F, 0x29, 0x37);

        // 阴影
        g.setColor(new Color(0, 0, 0, 45));
        g.fill(new Ellipse2D.Float(cx - 4.5f * s, cy + 4.2f * s, 9f * s, 2.2f * s));

        // 天线
        g.setStroke(new BasicStroke(Math.max(0.7f, 0.9f * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(line);
        g.drawLine(Math.round(cx), Math.round(cy - 6.6f * s), Math.round(cx), Math.round(cy - 9.2f * s));
        g.fill(new Ellipse2D.Float(cx - 1.3f * s, cy - 10.6f * s, 2.6f * s, 2.6f * s));

        // 头
        RoundRectangle2D head = new RoundRectangle2D.Float(cx - 5.2f * s, cy - 6.8f * s, 10.4f * s, 7.6f * s, 2.6f * s, 2.6f * s);
        g.setColor(Color.WHITE);
        g.fill(head);
        g.setColor(line);
        g.draw(head);

        // 眼睛
        g.setColor(eye);
        g.fill(new RoundRectangle2D.Float(cx - 4.0f * s, cy - 5.2f * s, 2.2f * s, 2.4f * s, 1f, 1f));
        g.fill(new RoundRectangle2D.Float(cx + 1.8f * s, cy - 5.2f * s, 2.2f * s, 2.4f * s, 1f, 1f));

        // 嘴
        g.setStroke(new BasicStroke(Math.max(0.6f, 0.7f * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Arc2D.Float(cx - 1.8f * s, cy - 1.4f * s, 3.6f * s, 2.2f * s, 0, 180, Arc2D.OPEN));

        // 身体
        RoundRectangle2D body = new RoundRectangle2D.Float(cx - 3.8f * s, cy + 0.4f * s, 7.6f * s, 5.0f * s, 1.8f * s, 1.8f * s);
        g.setColor(Color.WHITE);
        g.fill(body);
        g.setColor(line);
        g.draw(body);

        // 胸口彩色指示灯
        g.setColor(accent);
        g.fill(new RoundRectangle2D.Float(cx - 3.2f * s, cy + 2.2f * s, 6.4f * s, 1.5f * s, 0.8f * s, 0.8f * s));

        // 手臂
        g.setColor(Color.WHITE);
        g.fill(new RoundRectangle2D.Float(cx - 5.8f * s, cy + 0.6f * s, 1.9f * s, 3.6f * s, 0.9f * s, 0.9f * s));
        g.fill(new RoundRectangle2D.Float(cx + 3.9f * s, cy + 0.6f * s, 1.9f * s, 3.6f * s, 0.9f * s, 0.9f * s));
        g.setColor(line);
        g.draw(new RoundRectangle2D.Float(cx - 5.8f * s, cy + 0.6f * s, 1.9f * s, 3.6f * s, 0.9f * s, 0.9f * s));
        g.draw(new RoundRectangle2D.Float(cx + 3.9f * s, cy + 0.6f * s, 1.9f * s, 3.6f * s, 0.9f * s, 0.9f * s));
    }

    private void drawCheck(Graphics2D g, float cx, float cy, float t, float scale) {
        float animScale = 0.6f + 0.4f * easeOutBack(t);
        float s = scale * animScale;
        float alpha = Math.min(1f, t * 1.6f);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        float r = 7f * s;
        // 白色圆底
        g.setColor(Color.WHITE);
        g.fill(new Ellipse2D.Float(cx - r, cy - r, 2 * r, 2 * r));
        g.setColor(new Color(0, 0, 0, 45));
        g.setStroke(new BasicStroke(0.8f));
        g.draw(new Ellipse2D.Float(cx - r, cy - r, 2 * r, 2 * r));
        // 绿色对勾
        g.setColor(new Color(0x10, 0xB9, 0x81));
        g.setStroke(new BasicStroke(Math.max(1.2f, 2.2f * s), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Path2D p = new Path2D.Float();
        float k = 3.4f * s;
        p.moveTo(cx - k * 0.7f, cy);
        p.lineTo(cx - k * 0.15f, cy + k * 0.6f);
        p.lineTo(cx + k * 0.85f, cy - k * 0.6f);
        g.draw(p);
    }

    // ---------- 滚动文字 ----------

    private void paintMarquee(Graphics2D g2, String text, int x, int trackW, int baselineY, int speedPxPerSec) {
        Font font = progressBar.getFont().deriveFont(Font.PLAIN,
                Math.max(8f, progressBar.getFont().getSize2D() - 3f));
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int textW = fm.stringWidth(text);
        int gap = 28;
        int cycle = textW + gap;
        if (cycle <= 0) {
            return;
        }
        float elapsed = (System.currentTimeMillis() - animStart) / 1000f;
        float offset = (elapsed * speedPxPerSec) % cycle;
        g2.setColor(isDark() ? new Color(0x8A, 0x8A, 0x8A) : new Color(0x6E, 0x74, 0x7B));
        Shape oldClip = g2.getClip();
        g2.clip(new Rectangle(x, baselineY - fm.getAscent() - 1, trackW, fm.getAscent() + fm.getDescent() + 2));
        int startX = x - (int) offset;
        for (int i = -1; i <= 3; i++) {
            g2.drawString(text, startX + i * cycle, baselineY);
        }
        g2.setClip(oldClip);
    }

    // ---------- 文字 ----------

    private void paintCenteredString(Graphics2D g2, int x, int cy, int trackW, int h) {
        String s = progressBar.getString();
        if (s == null || s.isEmpty()) {
            return;
        }
        g2.setFont(progressBar.getFont());
        FontMetrics fm = g2.getFontMetrics();
        int sw = fm.stringWidth(s);
        int tx = x + (trackW - sw) / 2;
        int ty = cy + (fm.getAscent() - fm.getDescent()) / 2;
        g2.setColor(new Color(0, 0, 0, 90));
        g2.drawString(s, tx + 1, ty + 1);
        g2.setColor(isDark() ? new Color(0xE6, 0xE6, 0xE6) : new Color(0x33, 0x33, 0x33));
        g2.drawString(s, tx, ty);
    }

    // ---------- 工具 ----------

    private double pct() {
        int max = progressBar.getMaximum();
        int min = progressBar.getMinimum();
        if (max <= min) {
            return 0d;
        }
        return Math.max(0d, Math.min(1d, (double) (progressBar.getValue() - min) / (max - min)));
    }

    private void advanceCheckPhase() {
        long now = System.currentTimeMillis();
        if (checkPhase < 0) {
            checkPhase = 0f;
            lastPaintMs = now;
        } else if (checkPhase < 1f) {
            if (lastPaintMs > 0) {
                checkPhase = Math.min(1f, checkPhase + (now - lastPaintMs) / 420f);
            }
            lastPaintMs = now;
        }
    }

    private static float clamp(float v, float lo, float hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    private static float easeOutBack(float t) {
        float c1 = 1.70158f;
        float c3 = c1 + 1f;
        float x = t - 1f;
        return 1f + c3 * x * x * x + c1 * x * x;
    }

    private static Color parseColor(String hex, Color def) {
        if (hex == null) {
            return def;
        }
        try {
            return ColorUtil.fromHex(hex.trim(), def);
        } catch (Exception e) {
            return def;
        }
    }

    private static boolean isDark() {
        try {
            return UIUtil.isUnderDarcula() || ColorUtil.isDark(UIUtil.getPanelBackground());
        } catch (Throwable t) {
            return false;
        }
    }
}
