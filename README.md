# MultiLoader 模板

该项目提供了一个 Gradle 项目模板, 可使用通用项目作为源码来源, 为多个模组加载器编译 Minecraft 模组. 该项目不需要任何第三方库或依赖. 如果您有任何问题或想讨论该项目, 请加入我们的 [Discord](https://discord.myceliummod.network).

## 入门指南

### IntelliJ IDEA
本指南将展示如何将 MultiLoader 模板导入 IntelliJ IDEA. 设置过程大致相当于独立设置各个模组加载器, 对于任何使用过其 MDK 的人来说应该非常熟悉.

1. 将此仓库克隆或下载到您的计算机.
2. 通过在 `gradle.properties` 文件中设置属性来配置项目. 您还需要更改 `settings.gradle` 中的 `rootProject.name` 属性, 该属性应与项目的文件夹名称一致, 否则 IDEA 可能会报错.
3. 在 IDEA 中将模板的根文件夹作为新项目打开. 该文件夹包含此 README.md 文件和 gradlew 可执行文件.
4. 如果您的默认 JVM/JDK 不是 Java 25, 打开项目时会遇到错误. 通过进入 `File > Settings > Build, Execution, Deployment > Build Tools > Gradle > Gradle JVM` 并将值更改为有效的 Java 25 JVM 即可修复此错误. 您还需要将项目 SDK 设置为 Java 25, 可通过 `File > Project Structure > Project SDK` 完成. 两者都设置好后, 打开 IDEA 中的 Gradle 选项卡并点击刷新按钮以重新加载项目.
5. 打开您的运行/调试配置. 在 `Application` 类别下, 现在应该会出现运行 Fabric 和 NeoForge 项目的选项. 选择其中一个客户端选项并尝试运行.
6. 假设您能在第 5 步中成功运行游戏, 那么您的工作区就已设置完成.

### Eclipse
虽然可以在 Eclipse 中使用此模板, 但不建议这样做. 在开发此模板期间, 在所需构建工具的几乎每个层面都发现了与 Eclipse 相关的多个严重错误和问题. 虽然我们继续与这些工具合作以报告和解决问题, 但对这类项目的支持尚未就绪. 目前, Eclipse 被视为该项目不支持的平台. 构建工具的开发周期众所周知地缓慢, 因此没有可用的预计完成时间.

## 开发指南
使用此模板时, 您的模组大部分代码应在 `common` 项目中开发. `common` 项目是针对原版游戏编译的, 用于存放您的模组在不同加载器特定版本之间共享的代码. `common` 项目不了解也无法访问特定模组加载器的代码、API 或概念. 需要特定加载器功能的代码必须通过该加载器特定的项目来完成, 例如 `fabric` 或 `neoforge` 项目.

加载器特定的项目 (如 `fabric` 和 `neoforge` 项目) 用于将 `common` 项目加载到游戏中. 这些项目还定义了特定于该加载器的代码. 加载器特定的项目可以访问 `common` 项目中的所有代码. 请务必记住, `common` 项目不能访问加载器特定项目中的代码.

## 移除平台和加载器
虽然此模板支持多个模组加载器, 但未来可能会出现新的加载器, 而现有加载器可能会变得不那么重要.

移除加载器特定的项目只需删除文件夹, 并从 `settings.gradle` 文件中移除 `include("projectname")` 行即可.
例如, 如果您想移除对 `forge` 的支持, 请按照以下步骤操作:

1. 删除子项目文件夹. 例如, 删除 `MultiLoader-Template/forge`.
2. 从 `settings.gradle` 中移除该项目. 例如, 移除 `include("forge")`.
