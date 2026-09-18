<div align="center">
  <img src="images/banner.svg" alt="科幻风进度条 Sci-Fi Style Progress Bar" width="100%">

  <h1>科幻风进度条</h1>

  <p><b>给 JetBrains IDE 的进度条换上一身科幻皮肤。</b><br>
  一个迷你机器人领着科幻蓝 → 代码绿的渐变光带走完每一段等待。</p>

  <p>
    <a href="../../releases/latest"><img src="https://img.shields.io/github/v/release/AVC459/IDE_SciFi_Style_Progress_Bar_Plugin?style=flat-square&label=version&color=2563EB" alt="version"></a>
    <img src="https://img.shields.io/badge/JetBrains-2021.1%2B-10B981?style=flat-square&logo=jetbrains&logoColor=white" alt="JetBrains 2021.1+">
    <img src="https://img.shields.io/badge/Java-11-2563EB?style=flat-square&logo=openjdk&logoColor=white" alt="Java 11">
    <img src="https://img.shields.io/badge/animation%20assets-zero-10B981?style=flat-square" alt="zero assets">
    <img src="https://img.shields.io/badge/license-MIT-64748B?style=flat-square" alt="MIT">
    <a href="../../stargazers"><img src="https://img.shields.io/github/stars/AVC459/IDE_SciFi_Style_Progress_Bar_Plugin?style=flat-square&color=10B981" alt="stars"></a>
  </p>

  <p><a href="README_EN.md">English</a> · <b>简体中文</b></p>
</div>

---

## 这是什么

JetBrains IDE 的进度条一直是那条灰扑扑的横杠 —— 构建、索引、下载依赖，你盯着它看的时间其实不短。

**科幻风进度条**把这条横杠重画了一遍：浅灰圆角轨道，科幻蓝到代码绿的渐变填充，一个迷你机器人头像跟着进度一路向右滑，跑满 100% 时弹出一个小小的对勾。轨道上方还有一道微妙的高光，让渐变更像一条发光的能量条。

不换主题、不改快捷键、不装运行时，只换掉你每天要看几十次的那几秒。

> ### 🧩 不只是 IDEA —— 整个 JetBrains 家族通用
>
> **IntelliJ IDEA、PyCharm、WebStorm、GoLand、PhpStorm、CLion、Rider、RubyMine、DataGrip、RustRover、Aqua**，以及基于 IntelliJ Platform 的 **Android Studio** 等，全部直接可用，装法完全一样。
>
> 插件只依赖 `com.intellij.modules.platform`，不针对某个具体产品编译，所以它的适用范围是**整个 IntelliJ Platform**，而不是某一个 IDE。

## 效果预览

<img src="images/preview.svg" alt="三种状态预览" width="100%">

| 场景 | 表现 |
|---|---|
| **确定进度 · 进行中** | 圆角轨道 + 渐变填充，机器人骑在进度前端，图标尺寸随进度条高度自适应 |
| **确定进度 · 100%** | 机器人被白色圆底 + 绿色对勾替换，带轻微回弹的缩放动画 |
| **无限加载** | 一段渐变光带左右往复滑行，机器人骑在光带前缘并轻微上下浮动，底部小字滚动提示 |

> 预览图按插件的**实际绘制参数**（坐标、渐变色值、阈值、图标缩放公式）还原 —— 因为插件本身就是纯矢量绘制，没有位图资源可以"截"。

## 功能特性

| | 特性 | 说明 |
|---|---|---|
| 🤖 | **机器人领跑** | 迷你机器人头像随进度向右移动；到达 100% 弹出带 `easeOutBack` 回弹的对勾动画 |
| 🎨 | **科幻蓝 → 代码绿** | 默认渐变 `#2563EB → #10B981`，两端颜色都可自定义 |
| ✨ | **轨道高光** | 填充上半部叠加一道 21% 白色高光，渐变看起来像发光能量条 |
| 🔁 | **无限加载也有戏** | `indeterminate` 模式下光带左右往复滑行，机器人随光带前缘并上下浮动，底部跑马灯文字 |
| 📐 | **尺寸自适应** | 图标缩放系数由进度条高度算出，状态栏里的矮进度条不会被裁剪 |
| ⚡ | **纯矢量绘制** | 完全由 `Graphics2D` 绘制，不依赖任何 GIF / PNG / 外部动图资源 |
| 🪶 | **动画按需运行** | 30ms 定时器只在 `indeterminate` 或对勾动画期间启动，其余时间自动停表；同时屏蔽 Swing 默认动画计时器，避免双动画源 |
| 👀 | **不会出现白色宽条** | 直接改写 `UIManager` 的 `ProgressBarUI` 默认值，任何 `new JProgressBar()` 在构造时就拿到皮肤（详见下方「换肤原理」） |
| 🌗 | **主题即时适配** | 轨道色与文字色在 `paint()` 时按背景明暗实时解析，切换 New UI / Darcula / 亮色 / 深色主题即刻生效 |
| 🧩 | **JetBrains 全家桶通用** | 只依赖 `com.intellij.modules.platform`，IDEA / PyCharm / WebStorm / GoLand / CLion / DataGrip 等全线产品通用 |
| 🎛️ | **就近可配** | 设置项就在 Appearance & Behavior 下，带取色器与速度滑块 |
| 🔌 | **零侵入** | 不动你的工程、不联网、不注册后台任务，随时禁用即恢复原样 |

## 安装

### 方式一：从磁盘安装（推荐）

1. 到 [Releases](../../releases/latest) 下载 `sci-fi-progress-bar-2.0.0.zip`
   > 也可以直接用仓库里的 [`dist/sci-fi-progress-bar-2.0.0.zip`](dist/sci-fi-progress-bar-2.0.0.zip)
2. 打开 IDE → `Settings / Preferences` → `Plugins` → 右上角 ⚙️ → **Install Plugin from Disk...**
3. 选中刚下载的 **zip 包（无需解压）**，重启 IDE 即可生效

### 方式二：手动放入插件目录

把 zip 解压后，让目录结构长成这样：

```
<IDE 配置目录>/plugins/
└── sci-fi-progress-bar/
    └── lib/
        ├── sci-fi-progress-bar-2.0.0.jar
        └── searchableOptions-2.0.0.jar
```

常见的 `<IDE 配置目录>`：

| 系统 | 路径 |
|---|---|
| Windows | `%APPDATA%\JetBrains\<产品><版本>\plugins` |
| macOS | `~/Library/Application Support/JetBrains/<产品><版本>/plugins` |
| Linux | `~/.local/share/JetBrains/<产品><版本>/plugins` |

放好后重启 IDE。想卸载直接用 `Plugins` 面板里的 Uninstall，或删掉这个文件夹。

## 设置

入口：`Settings / Preferences → Appearance & Behavior → 科幻风进度条`

| 选项 | 说明 | 默认值 |
|---|---|---|
| 启用科幻风进度条皮肤 | 总开关，关掉即回到 IDE 原生进度条 | ✅ 开启 |
| 渐变起始色 | 轨道填充的左端颜色（带取色器） | `#2563EB` 科幻蓝 |
| 渐变结束色 | 轨道填充的右端颜色（带取色器） | `#10B981` 代码绿 |
| indeterminate 模式显示滚动文字 | 无限加载时是否在底部跑马灯 | ✅ 开启 |
| 滚动文案 | 跑马灯显示的文字 | `正在读取` |
| 动画速度 | 1~5 滑块，数值越大往复与跑马灯越快 | `3` |

> 机器人头部图标为固定样式，不可更换 —— 它是这套皮肤的签名。
> 设置持久化在 IDE 配置目录的 `sci-fi-progress-bar.xml`。

## 兼容性

- **插件 ID**：`com.scifi.progress`
- **最低版本**：`since-build 211` → **2021.1 及以上**（IntelliJ Platform 版本）
- **依赖模块**：只依赖 `com.intellij.modules.platform`，因此**与具体产品无关**
- **字节码目标**：Java 11（`options.release = 11`），在 JDK 11 ~ 21 运行的 IDE 上均可加载

### 支持的 IDE

| 类别 | 产品 |
|---|---|
| JetBrains 官方 IDE | IntelliJ IDEA · PyCharm · WebStorm · PhpStorm · GoLand · CLion · Rider · RubyMine · DataGrip · RustRover · Aqua 等 |
| 基于 IntelliJ Platform 的其他 IDE | Android Studio 等 |

> 一句话：**只要它是 2021.1 之后基于 IntelliJ Platform 的 IDE，这个皮肤就能装上。** 换产品不需要换包，同一个 zip 通用。

## 换肤原理

Swing 的进度条有两个坑：一是**短命组件**（状态栏那些刚创建就销毁的进度条）来不及被遍历到，二是**默认白色宽条**。这个插件用三层覆盖把它们一起解决：

**① 构造即生效（主路径）** —— 改写 `UIManager` 的 `"ProgressBarUI"` 默认值指向自己的 UI 类，并把 `Class` 对象缓存进 `UIDefaults`、提供静态 `createUI(JComponent)`。JDK 的 `UIDefaults.getUI` 会按类名取缓存 `Class` 再反射调用 `createUI`，**不经过插件类加载器**，所以任何地方 `new JProgressBar()` 在构造那一刻就已经是皮肤 UI。

> 这里有个坑：不自定义 `createUI()` 的话，会继承 `BasicProgressBarUI.createUI` 返回默认进度条 —— 表现就是界面上一根**白色宽条**。这是 1.1 及更早版本问题的根因。

**② AWT 组件监听（即时兜底）** —— 监听 `COMPONENT_ADDED` 与 `COMPONENT_SHOWN`：前者覆盖"被加入显示树"，后者覆盖"先构建、后显示"的对话框与弹窗。注意 `COMPONENT_ADDED` 属于 `ContainerEvent`（ID 601），必须同时启用 `CONTAINER_EVENT_MASK` 才收得到。

**③ 周期扫描（最终兜底）** —— 每 2 秒遍历全部窗口的组件树，兜住任何漏网之鱼。

另外：主题切换不需要额外监听 —— 轨道色和文字色是在每次 `paint()` 时用 `UIUtil.isUnderDarcula()` / 背景明暗算出来的，所以换主题后一重绘就是新配色；设置面板点 Apply 会立刻对全部窗口重新应用。关闭总开关时会把 `UIManager` 的默认值还原成进入前的原始对象，不留残留。

## 从源码构建

```bash
./gradlew buildPlugin          # 产物：build/distributions/sci-fi-progress-bar-2.0.0.zip
./gradlew verifyPlugin         # 插件兼容性校验（可选）

# 已有本地 IDE 发行版时，跳过 SDK 下载：
./gradlew buildPlugin -PideaLocalPath="/path/to/ideaIC"
```

Windows 用 `gradlew.bat`。构建要点：

- 本工程用 **IntelliJ Platform Gradle Plugin 1.17.4 + Gradle 8.10.2**（wrapper 已配置腾讯云镜像）
- 默认平台版本 `2023.1.5 (IC)`；通过 `-PideaLocalPath=` 可指向本地 IDE 目录
- `updateSinceUntilBuild = false`：只声明 `since-build`，不设上限，让插件在新 IDE 上也能直接加载
- `instrumentCode = false`：不做 Ant 字节码插桩（新版平台已移除该机制）
- 首次构建需要网络可达 JetBrains CDN（或配置镜像）以拉取平台 SDK

## 项目结构

```
├── build.gradle.kts                 构建脚本（Java 11 / IntelliJ Platform）
├── gradle/wrapper/                  Gradle 8.10.2（腾讯云镜像）
├── src/main/java/com/scifi/progress/
│   ├── SciFiProgressBarUI.java      全部矢量绘制与动画（determinate / indeterminate / 机器人 / 对勾 / 跑马灯）
│   ├── ProgressBarSkinner.java      三层换肤安装器 + 开关还原
│   ├── SciFiLifecycleListener.java  插件启动与应用重新激活时安装
│   └── settings/
│       ├── SciFiSettingsState.java  PersistentStateComponent，落盘 sci-fi-progress-bar.xml
│       ├── SciFiSettingsPanel.java  纯 Swing 设置面板
│       ├── SciFiSettingsConfigurable.java
│       └── ColorWell.java           渐变色的取色控件
├── src/main/resources/META-INF/plugin.xml
├── images/                          Banner 与效果预览（矢量）
└── dist/                            构建好的可安装 zip
```

## 常见问题

<details>
<summary><b>装完之后没变化？</b></summary>

到 `Settings → Appearance & Behavior → 科幻风进度条` 确认总开关是打开的。另外索引 / 构建这类进度条只在任务运行时出现，空跑一次 Build 就能看到。
</details>

<details>
<summary><b>我在 PyCharm / WebStorm / GoLand 等其他 JetBrains IDE 上能用吗？</b></summary>

能，而且用的是同一个安装包。插件不针对具体产品编译，只依赖 `com.intellij.modules.platform`，所以在 IDEA、PyCharm、WebStorm、PhpStorm、GoLand、CLion、Rider、RubyMine、DataGrip、RustRover 等全线产品上表现一致，设置入口的位置也一样。
</details>

<details>
<summary><b>为什么有 2 秒扫描？不会浪费性能吗？</b></summary>

扫描只做组件树遍历与 `instanceof` 判断，已经换过肤的进度条会直接跳过，开销可以忽略；它是为了兜住那些绕过 `UIManager` 与 AWT 事件创建进度条的极端情况。动画定时器才是耗 CPU 的部分，而它只在 `indeterminate` 或对勾动画期间运行，其余时间自动停表。
</details>

<details>
<summary><b>会和别的主题插件冲突吗？</b></summary>

插件只接管进度条这一种组件的绘制，不修改任何主题颜色键，因此与主流主题插件共存正常。遇到异常把总开关关掉即可精确回退 —— 关闭时会把 `UIManager` 默认值还原成进入前的对象。
</details>

<details>
<summary><b>为什么不做成一堆动图资源？</b></summary>

因为不想为了好看而变卡。序列帧会带来解码与内存开销，纯矢量方案在 4K / 多显示器下更清晰，占用也更低。机器人、对勾、跑马灯全部是 `Graphics2D` 现画的。
</details>

<details>
<summary><b>插件需要联网吗？需要我的工程权限吗？</b></summary>

都不需要。它不联网、不读取工程内容、不注册后台任务，只做 UI 绘制。
</details>

## 更新日志

见 [CHANGELOG.md](CHANGELOG.md)。

**v2.0.0**
- 机器人领跑、100% 对勾动画、无限加载跑马灯，全部 `Graphics2D` 矢量绘制
- 图标尺寸随进度条高度自适应缩放，状态栏矮进度条不再被裁剪
- 填充叠加顶部高光；对勾动画使用 `easeOutBack` 回弹
- 动画定时器按需启停，屏蔽 Swing 默认动画计时器
- 标准 Gradle 插件工程，可直接 `gradlew buildPlugin` 出包

## 参与贡献

发现问题或想要新特性，欢迎开 [Issue](../../issues)。提交代码前请确保：改动聚焦在进度条渲染这一件事上、不引入外部资源文件、不增加常驻后台任务。

如果这个小插件让你每天舒服了一点，给个 ⭐ 就是最好的支持。

## 赞赏

这个插件不联网、不带广告、也不打算收费。如果它让你每天那几秒的等待舒服了一点，可以请我喝杯咖啡 —— 完全随意，不请也照样用，功能一个不少。

<table align="center">
  <tr>
    <td align="center" width="240"><img src="images/alipay-qr.jpg" width="200" alt="支付宝收款码"><br><b>支付宝</b></td>
    <td align="center" width="240"><img src="images/wechat-qr.png" width="200" alt="微信收款码"><br><b>微信</b></td>
  </tr>
</table>

## License

[MIT](LICENSE)
