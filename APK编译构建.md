# APK 编译构建问题排查记录

## 问题概述

在使用 Android Gradle Plugin 8.2.0 + JDK 21 构建项目时，遇到 `JdkImageTransform` 错误导致编译失败。

---

## 错误信息

```
Execution failed for task ':app:compileDebugJavaWithJavac'.
> Could not resolve all files for configuration ':app:androidJdkImage'.
   > Failed to transform core-for-system-modules.jar
      > Execution failed for JdkImageTransform
         > Error while executing process jlink.exe
```

---

## 问题根因

AGP 8.x 引入了新的 `JdkImageTransform` 机制，使用 `jlink.exe` 将 Android SDK 的 `core-for-system-modules.jar` 转换为 JDK 模块格式。

| 因素 | 影响 |
|------|------|
| JDK 21 | jlink 与 Android SDK 模块不兼容 |
| Windows 系统 | 路径处理可能有问题 |
| Gradle 缓存 | transforms-3 缓存可能损坏 |

---

## 尝试过的方案

| 方案 | 结果 | 原因 |
|------|------|------|
| 降级 SDK 34 → 33 | ❌ | AndroidX 依赖要求 SDK 34 |
| 降级 AndroidX 依赖 | ❌ | jlink 错误仍然存在 |
| 降级 AGP 8.2 → 7.4 | ❌ | JDK 21 要求 Gradle 8.5+ |
| 使用外部 JDK 21 | ❌ | jlink 问题与 JDK 版本无关 |

---

## 最终解决方案 ✅

**配置 Android Studio 使用 JDK 17（而非系统的 JDK 21）**

### 步骤

1. **打开设置**
   ```
   File → Settings → Build, Execution, Deployment → Build Tools → Gradle
   ```

2. **修改 Gradle JDK**
   - 点击 Gradle JDK 下拉菜单
   - 选择 `Download JDK...`
   - 选择 **Version: 17**，**Vendor: Eclipse Temurin**
   - 下载完成后选择它

3. **清理 Gradle 缓存**
   ```powershell
   Remove-Item -Recurse -Force "$env:USERPROFILE\.gradle\caches\transforms-3"
   ```

4. **重新同步项目**
   ```
   File → Sync Project with Gradle Files
   ```

---

## 最终配置

| 组件 | 版本 |
|------|------|
| Android Gradle Plugin | 8.2.0 |
| Gradle | 8.2 |
| Kotlin | 1.9.20 |
| **Gradle JDK** | **17 (Temurin)** |
| compileSdk | 34 |
| targetSdk | 34 |
| minSdk | 24 |

---

## 经验总结

> [!IMPORTANT]
> AGP 8.x 推荐使用 **JDK 17**，避免使用 JDK 21 可能遇到的 jlink 兼容性问题。

1. **JDK 版本很重要** — 不同 JDK 版本的 jlink 行为不一致
2. **系统 JDK 与 Gradle JDK 可以不同** — Android Studio 允许为 Gradle 配置独立的 JDK
3. **清理缓存是关键** — `transforms-3` 缓存损坏会导致问题持续存在
