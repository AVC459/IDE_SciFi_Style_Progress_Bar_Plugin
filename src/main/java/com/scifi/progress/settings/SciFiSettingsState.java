package com.scifi.progress.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 科幻风进度条皮肤设置（应用级持久化，存储于 sci-fi-progress-bar.xml）。
 */
@State(name = "SciFiProgressBarSettings", storages = @Storage("sci-fi-progress-bar.xml"))
public class SciFiSettingsState implements PersistentStateComponent<SciFiSettingsState> {

    /** 总开关。 */
    public boolean enabled = true;
    /** 渐变起始色（科幻蓝 #2563EB）。 */
    public String startColor = "2563EB";
    /** 渐变结束色（代码绿 #10B981）。 */
    public String endColor = "10B981";
    /** indeterminate 模式是否显示底部滚动文字。 */
    public boolean showText = true;
    /** 底部滚动文案。 */
    public String scrollingText = "\u6B63\u5728\u8BFB\u53D6"; // 正在读取
    /** 动画速度 1..5。 */
    public int speed = 3;

    @Nullable
    @Override
    public SciFiSettingsState getState() {
        return this;
    }

    @Override
    public void loadState(@NotNull SciFiSettingsState state) {
        this.enabled = state.enabled;
        this.startColor = state.startColor;
        this.endColor = state.endColor;
        this.showText = state.showText;
        this.scrollingText = state.scrollingText;
        this.speed = Math.max(1, Math.min(5, state.speed));
    }

    public static SciFiSettingsState getInstance() {
        return ApplicationManager.getApplication().getService(SciFiSettingsState.class);
    }
}
