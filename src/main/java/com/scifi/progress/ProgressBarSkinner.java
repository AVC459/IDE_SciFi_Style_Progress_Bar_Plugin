package com.scifi.progress;

import com.intellij.ide.ui.LafManagerListener;
import com.intellij.ide.ui.UISettings;
import com.intellij.ide.ui.UISettingsListener;
import com.intellij.openapi.application.ApplicationManager;
import com.scifi.progress.settings.SciFiSettingsState;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 进度条皮肤安装器。三层覆盖，确保任何进度条都能换肤：
 *
 * <ol>
 *   <li><b>构造即生效（主路径）</b>：把 UIManager 的 "ProgressBarUI" 默认值指向本插件 UI 类，
 *       并缓存 Class 对象 + 自定义静态 createUI()。这样任何地方 {@code new JProgressBar()} /
 *       {@code new JBProgressBar()}（普通 JProgressBar 走 UIManager 默认值）在构造时就直接拿到
 *       皮肤 UI —— 不依赖类加载器，也不依赖组件加入显示树的时机。</li>
 *   <li><b>AWT 组件监听（即时兜底）</b>：任何进度条被加入显示树（COMPONENT_ADDED）或被显示
 *       （COMPONENT_SHOWN，覆盖"先构建后显示"的对话框/弹窗里的进度条）时立刻替换 UI。</li>
 *   <li><b>周期扫描（最终兜底）</b>：2 秒扫描全部窗口组件树，防任何漏网。</li>
 * </ol>
 *
 * <p>附带：LAF / 主题切换、UI 设置（缩放）变化、应用重新激活时立即重新安装；
 * 设置关闭时恢复 IDE 原始进度条。</p>
 */
public final class ProgressBarSkinner {

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);
    private static javax.swing.Timer scanTimer;
    /** 首次安装前 IDE 自己的 "ProgressBarUI" 默认值，关闭皮肤时用它恢复。 */
    private static volatile Object originalProgressBarUIDefault;

    private ProgressBarSkinner() {
    }

    /** 插件启动入口（由生命周期监听器构造时调用一次）。 */
    public static void ensureRunning() {
        if (!STARTED.compareAndSet(false, true)) {
            return;
        }
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            applyToAll();
            installUIManagerDefaults();
            installAwtComponentListener();
            // UI 设置（缩放等）变化
            UISettings.getInstance().addUISettingsListener(new UISettingsListener() {
                @Override
                public void uiSettingsChanged(UISettings source) {
                    applyToAll();
                }
            }, ApplicationManager.getApplication());
            // 兜底：周期扫描新建的进度条
            scanTimer = new javax.swing.Timer(2000, e -> applyToAll());
            scanTimer.start();
        });
    }

    /**
     * 把 UIManager 的 "ProgressBarUI" 默认值指向自定义 UI。
     *
     * <p>现代 JDK 的 {@code UIDefaults.getUI} 流程：
     * 取 "ProgressBarUI" 的类名字符串 → 从 defaults 里按"类名"查缓存的 Class 对象（第二个 put）→
     * 反射调用该类的静态 {@code createUI(JComponent)}。因此：第二个 put 提供 Class（绕过类加载器），
     * 自定义 createUI() 保证创建的是本插件 UI（否则会继承 BasicProgressBarUI 的 createUI 返回白色宽条）。</p>
     *
     * <p>设置关闭时恢复 IDE 原始默认值，避免新建进度条仍被换肤。</p>
     */
    private static void installUIManagerDefaults() {
        try {
            if (originalProgressBarUIDefault == null) {
                originalProgressBarUIDefault = UIManager.get("ProgressBarUI");
            }
            boolean enabled = SciFiSettingsState.getInstance().enabled;
            if (enabled) {
                UIManager.put("ProgressBarUI", SciFiProgressBarUI.class.getName());
                UIManager.getDefaults().put(SciFiProgressBarUI.class.getName(), SciFiProgressBarUI.class);
            } else {
                if (originalProgressBarUIDefault != null) {
                    UIManager.put("ProgressBarUI", originalProgressBarUIDefault);
                }
                UIManager.getDefaults().put(SciFiProgressBarUI.class.getName(), null);
            }
        } catch (Throwable ignored) {
            // 失败无碍：AWT 监听 + 组件树扫描仍兜底
        }
    }

    /**
     * 即时换肤：任何进度条被加入显示树（COMPONENT_ADDED）或被显示（COMPONENT_SHOWN，
     * 覆盖"先构建后显示"的对话框/弹窗）时立刻替换 UI。
     */
    private static void installAwtComponentListener() {
        try {
            Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
                int id = event.getID();
                if (id != ContainerEvent.COMPONENT_ADDED && id != ComponentEvent.COMPONENT_SHOWN) {
                    return;
                }
                Component comp = event instanceof ContainerEvent
                        ? ((ContainerEvent) event).getChild()
                        : (Component) event.getSource();
                if (comp == null) {
                    return;
                }
                if (comp instanceof JProgressBar) {
                    skinBar((JProgressBar) comp);
                } else if (comp instanceof Container) {
                    applyTo(comp);
                }
                // 注意：COMPONENT_ADDED 属于 ContainerEvent（ID 601），必须同时启用
                // CONTAINER_EVENT_MASK 才能收到；COMPONENT_SHOWN 属 ComponentEvent（ID 102）。
            }, AWTEvent.COMPONENT_EVENT_MASK | AWTEvent.CONTAINER_EVENT_MASK);
        } catch (Throwable ignored) {
            // 个别受限环境不允许添加 AWT 监听器：UIManager 默认值 + 周期扫描仍有效
        }
    }

    /** 把全部可见/隐藏窗口中的进度条统一为自定义 UI（或按设置恢复为默认 UI）。 */
    public static void applyToAll() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        installUIManagerDefaults();
        for (Window window : Window.getWindows()) {
            applyTo(window);
        }
    }

    private static void skinBar(JProgressBar bar) {
        boolean enabled = SciFiSettingsState.getInstance().enabled;
        if (enabled) {
            if (!(bar.getUI() instanceof SciFiProgressBarUI)) {
                bar.setUI(new SciFiProgressBarUI());
            } else {
                bar.repaint();
            }
        } else if (bar.getUI() instanceof SciFiProgressBarUI) {
            bar.updateUI(); // 恢复 IDE 默认进度条
        }
    }

    private static void applyTo(Component c) {
        if (c instanceof JProgressBar) {
            skinBar((JProgressBar) c);
        } else if (c instanceof Container) {
            Container container = (Container) c;
            synchronized (container.getTreeLock()) {
                for (Component child : container.getComponents()) {
                    applyTo(child);
                }
            }
        }
    }
}
