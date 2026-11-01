# 轻账

[![License](https://img.shields.io/github/license/RainxButterfly/LightAccounting?style=flat-square)](LICENSE)
[![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat-square&logo=android&logoColor=white)](#环境要求)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2024.12.01-4285F4?style=flat-square)](#技术栈)

轻账（LightAccounting）是一款 Android 本地记账应用。支持手动记账、支付宝与微信账单导入，以及基于支付通知的待确认记账。账本、账户和流水只保存在设备本机。

作者：星河一叶Roxy（StarLeaf·Roxy）。版本记录见 [CHANGELOG.md](CHANGELOG.md)。

- 当前版本：1.0.1

## 目录

- [功能](#功能)
- [下载](#下载)
- [环境要求](#环境要求)
- [构建](#构建)
- [使用](#使用)
- [自动更新](#自动更新)
- [数据与隐私](#数据与隐私)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [已知限制](#已知限制)
- [常见问题](#常见问题)
- [贡献](#贡献)
- [许可证](#许可证)

## 功能

- 多账本，支持支出、收入、转账三类记录
- 账户余额由流水自动汇总，支持自定义账户与分类
- 导入支付宝、微信官方账单，以及钱迹等应用导出的 CSV / JSON
- 读取微信支付、支付宝的付款通知，生成待确认记录，确认后计入账本
- 月度收支汇总、分类占比环形图、近六个月趋势
- 浅色、深色与跟随系统三种主题
- 首次启动引导，可从「我的 → 使用引导」重新查看
- 应用内更新日志，检查更新与更新日志在同一入口
- 启动后静默检查更新，支持国内镜像下载

## 下载

签名 APK 发布在 [Releases](https://github.com/RainxButterfly/LightAccounting/releases)。当前 1.0.1 的 release APK 约 1.9 MB。首个版本发布前，请按下方步骤自行构建。

## 环境要求

- 运行：Android 8.0（API 26）及以上
- 构建：JDK 17 及以上、Android SDK 34、Gradle 8.7
- 开发工具：Android Studio

## 构建

```bash
git clone https://github.com/RainxButterfly/LightAccounting.git
cd LightAccounting
```

Debug 构建：

```bash
./gradlew assembleDebug
```

产物位于 `app/build/outputs/apk/debug/app-debug.apk`。

Gradle wrapper 默认从腾讯云镜像下载 Gradle 8.7。如需官方地址，修改 `gradle/wrapper/gradle-wrapper.properties` 中的 `distributionUrl`。

Release 构建：

```bash
./gradlew assembleRelease
```

Release 开启了 R8 代码压缩与资源压缩，产物位于 `app/build/outputs/apk/release/`。

Release 签名从根目录的 `signing.properties` 读取，该文件已在 `.gitignore` 中忽略。首次配置时复制示例文件并填写本地 keystore 路径：

```bash
cp signing.properties.example signing.properties
```

```properties
storeFile=/absolute/path/to/release.jks
storePassword=your-store-password
keyAlias=your-key-alias
keyPassword=your-key-password
```

没有 `signing.properties` 时，Release 产物为未签名 APK。自动更新要求后续版本始终使用同一个签名密钥，否则 Android 会拒绝安装。

本地发布用的签名包可以放在 `dist/`，该目录已加入忽略规则，不会进入版本库。

运行单元测试与静态检查：

```bash
./gradlew test lintDebug
```

## 使用

### 手动记账

首页底部的加号进入记账页，选择支出、收入或转账，输入金额并选择分类。转账只在账户之间转移资金，不计入收支统计。

### 导入账单

支付宝：我的 → 账单 → 右上角「…」→ 开具交易流水证明 → 用于个人对账，文件会发送到邮箱。

微信：我 → 服务 → 钱包 → 账单 → 右上角「常见问题」→ 下载账单 → 用于个人对账，解压密码会通过微信服务通知下发。

在「我的 → 账单导入」中选择下载好的 CSV、JSON 或 ZIP 文件，填写微信账单的解压密码，解析后确认导入。转账、退款、关闭状态以及「不计收支」的记录会被跳过；同一份账单重复导入不会产生重复记录。

也支持钱迹等第三方记账应用导出的 CSV / JSON。轻账按列名识别日期、金额、收支类型、分类和账户；分类与账户按名称匹配，不存在的会新建。导入的数据会合并到当前账本，不迁移第三方应用的账本结构和账户初始余额。

### 通知自动记账

在「我的 → 通知自动记账」中开启系统的通知使用权。应用只接收来自 `com.tencent.mm`（微信）和 `com.eg.android.AlipayGphone`（支付宝）的付款通知，不读取聊天内容。解析出的记录会先进入「待确认账单」，确认后才写入账本。

如果关闭了通知、隐藏了通知内容，或者应用被系统限制后台运行，对应通知可能无法捕获。退款、自动扣款等场景也可能遗漏，建议定期用账单导入补齐。

### 主题与引导

在「我的 → 外观 → 主题」中可以切换跟随系统、浅色、深色三种模式。首次启动会展示四页使用引导，之后可在「我的 → 使用引导」重新打开。

### 更新日志

「我的」中的检查更新与更新日志在同一行。点「更新日志」可查看各版本的改动，当前版本会标记出来。

## 自动更新

应用每次启动会在后台延迟 1.5 秒检查更新。检查过程不打扰用户，只有发现新版本时才弹出提示；下载和安装都由用户确认。Android 不允许普通应用静默安装。

版本信息维护在仓库根目录的 `update.json`，读取顺序为 jsDelivr、gitmirror、gh-proxy 等国内可访问的镜像，最后回退到 GitHub Raw。APK 下载同样会依次尝试 GitHub Release 地址和多个代理镜像，并支持 `sha256` 校验。

发布新版本：

1. 修改 `app/build.gradle.kts` 中的 `versionCode` 和 `versionName`。
2. 使用同一个签名密钥构建 release APK，命名为 `qingzhang-vX.Y.Z.apk`。
3. 在 GitHub 创建标签 `vX.Y.Z` 的 Release，上传该 APK。
4. 更新根目录 `update.json` 的 `version`、`notes`、`apk`、`mirrors` 和 `sha256`。

`update.json` 字段：

- `version`：新版本号，必须大于已安装版本
- `notes`：更新说明，会显示在更新弹窗中
- `apk`：GitHub Release 的 APK 直链
- `mirrors`：备用下载地址，建议同时填写 gh-proxy、ghfast 等镜像
- `sha256`：APK 的 SHA-256，留空则不校验

仓库地址配置在 `app/src/main/java/com/starleaf/qingzhang/update/UpdateConfig.kt` 的 `OWNER`、`REPO` 和 `BRANCH`。如果仓库或默认分支发生变化，请同时修改该文件和 `update.json` 中的链接。

jsDelivr 对 `@main` 有缓存，更新后可能需要等待一段时间才能拉到新版本。自动检查失败不会提示，可在「我的 → 检查更新」手动重试。

## 数据与隐私

- 应用申请 `INTERNET` 权限，仅用于检查更新和下载新版本，不会上传账本、账单或通知内容
- 更新检查会请求 jsDelivr、GitHub 与镜像站点的 `update.json`，请求中不包含任何记账数据
- `REQUEST_INSTALL_PACKAGES` 权限只用于用户确认后调起系统安装程序
- 账单文件在设备内解析，不经过第三方服务
- 通知监听只匹配微信支付、支付宝两个包名，并只保留支付相关的通知文本
- 应用不接入统计、广告或推送 SDK
- 卸载应用或在系统设置中清除应用数据会删除全部账本

## 技术栈

- Kotlin 2.0.21
- Jetpack Compose（BOM 2024.12.01）、Material 3
- Room 2.6.1、Navigation Compose 2.8.5
- zip4j 2.11.5（解析微信加密账单压缩包）
- Gradle 8.7、Android Gradle Plugin 8.5.1
- 更新检查使用系统自带的 `HttpURLConnection`，未引入网络库

## 项目结构

```text
app/src/main/java/com/starleaf/qingzhang/
├── data/       数据实体、Room DAO、仓库与默认数据
├── importer/   支付宝、微信与第三方应用账单解析
├── logic/      商户关键词分类规则
├── notify/     支付通知监听与文本解析
├── update/     更新检查、镜像下载与安装
├── ui/         Compose 页面、主题、引导与通用组件
└── util/       金额与日期工具

app/src/test/java/   账单解析、通知解析、分类规则、版本比较单元测试
```

## 已知限制

- 历史数据依赖用户主动导出账单，无法直接同步支付宝、微信的完整账单
- 实时记账依赖系统通知，通知被关闭或隐藏时无法捕获
- 通知文本不含完整的商户名称，自动分类可能需要手动调整
- 目前仅支持 Android，iOS 没有对应的通知读取能力
- 支付宝、微信的账单列名或格式调整后，解析规则需要同步更新
- 自动更新依赖 GitHub Release 和第三方镜像，镜像地址可能失效
- 新版本必须与已安装应用使用同一个签名密钥，且版本号必须递增
- 第三方应用账单会合并到当前账本，不迁移原应用的账本结构和初始余额
- 第三方分类与账户按名称匹配，匹配不到时会自动新建
- 不提供云同步，数据仅保存在本机

## 常见问题

### Android Studio 同步时报 github.com 无法解析

同步时 IDE 会尝试从 GitHub 下载 Gradle 8.7 的源码包 `gradle-8.7-src.zip`，国内网络无法访问，报错位置通常是 `:prepareKotlinBuildScriptModel`。这一步只影响源码索引，不影响依赖解析和构建，Gradle 最终仍会输出 `BUILD SUCCESSFUL`，可以忽略。

如果不想反复尝试下载，在设置左上角的搜索框里搜索 “Download sources”，取消勾选 “Download sources”（下载源代码），然后执行 File → Sync Project with Gradle Files。第一次同步需要下载依赖，耗时较长，之后会使用本地缓存。

### 提示有新的 Gradle 版本

请保持 wrapper 中的 Gradle 8.7。Android Gradle Plugin 8.5.1 不支持 Gradle 9.x，升级后同步和构建都会失败。

### 更新检查失败

先确认设备网络可用，再在「我的 → 检查更新」重试。更新清单和 APK 会依次尝试多个国内镜像，个别镜像不可用不影响其他源。

## 贡献

提交代码前请阅读 [CONTRIBUTING.md](CONTRIBUTING.md)。问题反馈与功能建议请使用 GitHub Issues。

## 许可证

本项目采用 [Apache License 2.0](LICENSE) 开源。

Copyright 2026 StarLeaf·Roxy (星河一叶Roxy)

## 免责声明

本项目与支付宝、微信支付没有关联，也未获得其授权。项目只处理用户主动导出的账单文件和系统通知，不包含破解、注入、绕过风控或读取他人数据的功能。使用前请自行评估账号与数据安全。
