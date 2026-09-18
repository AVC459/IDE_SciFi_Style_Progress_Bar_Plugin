package com.scifi.progress.settings;

import com.intellij.openapi.options.Configurable;
import com.scifi.progress.ProgressBarSkinner;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Settings → Appearance & Behavior → 科幻风进度条
 */
public class SciFiSettingsConfigurable implements Configurable {

    private SciFiSettingsPanel panel;

    @Override
    public String getDisplayName() {
        return "\u79D1\u5E7B\u98CE\u8FDB\u5EA6\u6761"; // 科幻风进度条
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return null;
    }

    @Override
    public JComponent createComponent() {
        if (panel == null) {
            panel = new SciFiSettingsPanel();
        }
        return panel.getRoot();
    }

    @Override
    public boolean isModified() {
        return panel != null && panel.isModified();
    }

    @Override
    public void apply() {
        if (panel != null) {
            panel.apply();
            ProgressBarSkinner.applyToAll();
        }
    }

    @Override
    public void reset() {
        if (panel != null) {
            panel.reset();
        }
    }

    @Override
    public void disposeUIResources() {
        panel = null;
    }
}
