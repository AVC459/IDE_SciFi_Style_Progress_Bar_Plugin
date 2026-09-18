package com.scifi.progress;

import com.intellij.ide.ui.LafManager;
import com.intellij.ide.ui.LafManagerListener;
import com.intellij.openapi.application.ApplicationActivationListener;
import com.intellij.openapi.wm.IdeFrame;
import org.jetbrains.annotations.NotNull;

/**
 * 插件启动 / 主题切换 / 应用激活时安装进度条皮肤。
 *
 * <p>通过 plugin.xml 的 {@code <applicationListeners>} 注册：监听器实例在 IDE 启动时被创建
 * （构造函数即触发首次安装），之后在 LAF 切换与应用重新激活时重新安装。</p>
 *
 * <p>注意：LafManagerListener 的注册通过 MessageBus 在 {@link ProgressBarSkinner#ensureRunning()}
 * 中完成，此处的 {@link #lookAndFeelChanged} 仅为 API 兼容保留（不再重复调用 applyToAll）。</p>
 */
public class SciFiLifecycleListener implements ApplicationActivationListener {

    public SciFiLifecycleListener() {
        ProgressBarSkinner.ensureRunning();
    }

    @Override
    public void applicationActivated(@NotNull IdeFrame ideFrame) {
        ProgressBarSkinner.applyToAll();
    }
}
