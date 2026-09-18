# Changelog

本项目的所有重要变更都会记录在此文件。
格式参考 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/)，版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [2.0.0] - 2026-09-18

### 新增
- **机器人领跑**：`determinate` 模式下迷你机器人头像随进度向右移动，到达 100% 时弹出微小的绿色对勾
- **无限加载动画**：`indeterminate` 模式下渐变光带左右往复滑行，机器人骑在光带前缘并伴随正弦上下浮动，底部小字滚动提示（默认「正在读取」）
- **设置面板**：`Settings / Preferences → Appearance & Behavior → 科幻风进度条`，可配置渐变起止色（带取色器）、滚动文案、动画速度与总开关
- **尺寸自适应**：图标缩放系数由进度条高度算出，状态栏内联进度条这类矮进度条上的机器人不会被裁剪
- **轨道高光**：填充上半部叠加 21% 白色高光，渐变呈现发光能量条质感
- **对勾回弹**：100% 的对勾使用 `easeOutBack` 曲线配合淡入，约 420ms 完成

### 技术说明
- 全部元素由 `Graphics2D` 矢量绘制，不依赖任何 GIF / PNG / 外部动图资源
- 三层换肤：`UIManager` 的 `ProgressBarUI` 默认值（构造即生效）→ AWT 组件监听（`COMPONENT_ADDED` / `COMPONENT_SHOWN` 即时兜底）→ 每 2 秒组件树扫描（最终兜底）
- 动画定时器（30ms）按需启停，仅在 `indeterminate` 或对勾动画期间运行；同时屏蔽 `BasicProgressBarUI` 自带的动画计时器，避免双动画源
- 轨道色与文字色在 `paint()` 时按背景明暗实时解析，主题切换无需额外监听器
- 关闭总开关时把 `UIManager` 默认值还原为进入前的原始对象，不留残留
- 状态持久化在 IDE 配置目录的 `sci-fi-progress-bar.xml`

### 兼容性
- 插件 ID：`com.scifi.progress`
- 最低版本：`since-build 211` → IntelliJ Platform 2021.1 及以上
- 只依赖 `com.intellij.modules.platform`，全部 JetBrains IDE 通用（IntelliJ IDEA、PyCharm、WebStorm、PhpStorm、GoLand、CLion、Rider、RubyMine、DataGrip、RustRover、Aqua 等）
- 字节码目标 Java 11

### 构建
- IntelliJ Platform Gradle Plugin 1.17.4 + Gradle 8.10.2（wrapper 配置腾讯云镜像）
- `updateSinceUntilBuild = false`、`instrumentCode = false`
- 可直接 `gradlew buildPlugin` 出包，或 `-PideaLocalPath=` 指定本地 IDE 发行版
