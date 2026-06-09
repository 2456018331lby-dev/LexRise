# GitHub Release Playbook

## GitHub Release 是什么

GitHub Release 是仓库里的版本发布页，通常绑定一个 tag，例如 `v0.1.0`。你可以在这个页面里：

- 写这一版的更新说明
- 上传 `APK`、截图、演示视频等附件
- 给用户一个稳定的下载入口

如果你把 `app-release.apk` 上传到 Release 附件区，别人就可以直接下载你的 App。

## 当前建议的发布节奏

### 开发自测

- 构建 `debug APK`
- 在自己的手机或模拟器上安装
- 重点验证导入词书、新词学习、复习训练、提醒设置

### 公开发布

- 构建 `release APK`
- 在 GitHub 创建 `Release`
- 上传 `app-release.apk`
- 补充更新日志、截图和安装说明

## 操作步骤

### 1. 代码推到 GitHub

- 创建新仓库
- 推送 `Englishdemo` 工程
- 保持默认分支可构建

### 2. 构建 APK

PowerShell:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat assembleRelease
```

常见输出目录：

- `app/build/outputs/apk/debug/app-debug.apk`
- `app/build/outputs/apk/release/app-release.apk`

### 2.5 release APK 签名

release APK 必须要有签名才能在大多数手机上安装。最小步骤：

1. 生成一个签名密钥（只需要做一次，务必备份好 `.jks` 文件）：

    ```powershell
    keytool -genkeypair -v `
      -keystore lexrise-release.jks `
      -keyalg RSA -keysize 2048 -validity 36500 `
      -alias lexrise
    ```

2. 把密钥路径和密码放到 `local.properties`（该文件已被 `.gitignore` 忽略）：

    ```properties
    LEXRISE_STORE_FILE=C\:/path/to/lexrise-release.jks
    LEXRISE_STORE_PASSWORD=你的 keystore 密码
    LEXRISE_KEY_ALIAS=lexrise
    LEXRISE_KEY_PASSWORD=你的 key 密码
    ```

3. 在 `app/build.gradle.kts` 的 `android {}` 里加入 `signingConfigs` 并挂到 `release` 构建类型。大致模板：

    ```kotlin
    val localProps = java.util.Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }

    android {
        signingConfigs {
            create("release") {
                storeFile = localProps.getProperty("LEXRISE_STORE_FILE")?.let(::file)
                storePassword = localProps.getProperty("LEXRISE_STORE_PASSWORD")
                keyAlias = localProps.getProperty("LEXRISE_KEY_ALIAS")
                keyPassword = localProps.getProperty("LEXRISE_KEY_PASSWORD")
            }
        }
        buildTypes {
            release {
                signingConfig = signingConfigs.getByName("release")
                // 其他配置保持不变
            }
        }
    }
    ```

4. 再跑 `./gradlew.bat assembleRelease`，产物会是已签名的 `app-release.apk`，可以直接挂到 GitHub Release。

### 3. 创建 Tag 和 Release

建议版本号：

- `v0.1.0` 初版可用
- `v0.2.0` 新功能版本
- `v0.2.1` 小修复版本

### 4. 上传发布资产

建议上传：

- `app-release.apk`
- 2~4 张应用截图
- 可选演示视频或 GIF

### 5. 写 Release Notes

建议结构：

- 本版新增
- 修复问题
- 已知限制
- 安装方式

## 初版发布建议文案

### 标题

`LexRise v0.1.0 - Offline vocabulary learning for CET / exams`

### 要点

- 本地离线优先
- 支持 CSV / TXT 词书导入
- 新词学习 + 复习训练双模式
- 系统 TTS 发音
- 无账号、无云同步、安装即用

## 发布前检查清单

- 单元测试通过
- `assembleDebug` 成功
- `assembleRelease` 成功
- 导入词书流程实际走通
- 新词和复习都能正常记录进度
- 提醒设置保存后重启仍保留
- README 和 Release Notes 已更新
