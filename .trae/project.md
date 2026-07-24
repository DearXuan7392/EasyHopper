---
AIGC:
  ContentProducer: '001191110102MAD55U9H0F10002'
  ContentPropagator: '001191110102MAD55U9H0F10002'
  Label: '1'
  ProduceID: 'fb98e2d5-063e-4438-84af-5e292c5bd52f'
  PropagateID: 'fb98e2d5-063e-4438-84af-5e292c5bd52f'
  ReservedCode1: '5fce9cd0-77e9-4533-a907-edcc79334b27'
  ReservedCode2: '5fce9cd0-77e9-4533-a907-edcc79334b27'
---

# EasyHopper 技术说明文档

> 对应 EasyHopper `3.1` 版本（Minecraft `26.2`）。

---

## 目录

- [1. 项目概述](#1-项目概述)
- [2. 技术栈与运行环境](#2-技术栈与运行环境)
- [3. 项目结构](#3-项目结构)
- [4. 多加载器架构设计](#4-多加载器架构设计)
- [5. 构建系统](#5-构建系统)
- [6. 核心功能说明](#6-核心功能说明)
- [7. 配置系统](#7-配置系统)
- [8. 客户端/服务端代码隔离](#8-客户端服务端代码隔离)
- [9. Mixin 模块](#9-mixin-模块)
- [10. 网络功能](#10-网络功能)
- [11. 配置 GUI 系统](#11-配置-gui-系统)
- [12. 平台抽象层](#12-平台抽象层)
- [13. 入口与初始化流程](#13-入口与初始化流程)
- [14. 资源文件与国际化](#14-资源文件与国际化)
- [附录：配置项速查表](#附录配置项速查表)
- [附录：类索引](#附录类索引)

---

## 1. 项目概述

EasyHopper（轻松漏斗）是一个 Minecraft 模组，旨在增强原版漏斗（Hopper）的功能，而**不引入任何新方块**。这意味着玩家可以随时卸载本模组，而不会对存档造成任何影响。

### 核心能力

| 功能 | 说明 |
|------|------|
| **传输速度调节** | 自定义漏斗传输冷却时间与单次传输物品数量 |
| **检测冷却** | 每次尝试吸取物品后进入冷却，避免频繁检测掉落物，大幅提升大量漏斗场景下的性能 |
| **漏斗分类过滤** | 漏斗第 5 格作为过滤槽，只有相同类型的物品才能进入或被主动输出 |
| **漏斗矿车冷却** | 控制漏斗矿车的传输频率 |

### 设计理念

- **零侵入**：通过 Mixin 修改原版漏斗行为，不添加新方块/物品，卸载无副作用。
- **多加载器**：同时支持 Fabric 和 NeoForge 两个主流加载器，共享核心代码。
- **配置优先**：所有功能均可通过 YAML 配置文件或游戏内 GUI 进行调整。
- **代码隔离**：通过包结构（`client/` / `server/`）物理分离客户端与服务端代码，配合各加载器的环境机制（Fabric 入口点分离、NeoForge `@EventBusSubscriber(Dist)` ）实现运行时隔离。

---

## 2. 技术栈与运行环境

| 项目              | 值                         |
|-----------------|---------------------------|
| 模组版本            | `3.1`                     |
| 目标 Minecraft 版本 | `26.2`（范围 `[26.2, 26.3)`） |
| Java 版本         | `25`                      |
| 包路径             | `com.dearxuan.easyhopper` |
| 许可证             | MIT License               |
| 作者              | DearXuan                  |

### 依赖组件

| 组件 | 版本 | 用途 |
|------|------|------|
| Fabric Loader | `0.19.3` | Fabric 加载器 |
| Fabric API | `0.155.2+26.2` | Fabric 运行时 API |
| NeoForge | `26.2.0.28-beta` | NeoForge 加载器 |
| YACL | `3.9.6+26.2` | 配置 GUI 库（可选） |
| Cloth Config API | `26.2.155` | 配置 GUI 库（可选，优先级高于 YACL） |
| Mod Menu | `20.0.1` | Fabric 模组菜单集成（仅 Fabric） |
| SnakeYAML | `2.6` | YAML 配置文件读写 |
| MixinExtras | `0.3.5` | Mixin 增强工具 |

### 构建工具

| 工具 | 版本 | 用途 |
|------|------|------|
| Gradle | (通过 wrapper) | 项目构建 |
| Fabric Loom | `1.15.5` | Fabric 模组开发插件 |
| NeoForge ModDev | `2.0.141` | NeoForge 模组开发插件 |
| Shadow | `8.3.6` | 依赖打包 |

---

## 3. 项目结构

```
EasyHopper/
├── build.gradle                    # 根构建脚本（插件声明、仓库配置、产物命名）
├── settings.gradle                 # 项目设置（包含 common/fabric/neoforge 三个子项目）
├── gradle.properties               # 全局属性（版本号、依赖版本等）
├── buildSrc/                       # 自定义 Gradle 约定插件
│   └── src/main/groovy/
│       ├── easyhopper-common.gradle    # 通用构建配置（Java工具链、资源展开、发布）
│   └── easyhopper-loader.gradle    # 加载器子项目配置（合并 common 源码与资源）
│
├── common/                         # 共享代码模块（核心逻辑）
│   └── src/main/
│       ├── java/com/dearxuan/easyhopper/
│       │   ├── CommonClass.java            # 共享初始化入口
│       │   ├── Constants.java              # 常量定义（MOD_ID、LOG）
│       │   ├── config/
│       │   │   ├── ModConfig.java          # 配置数据类
│       │   │   ├── ConfigManager.java      # 配置读写管理器
│       │   │   ├── ConfigSyncPayload.java  # 配置同步网络包（C2S + S2C 双向）
│       │   │   └── retention/
│       │   │       ├── EasyConfig.java     # 配置注解
│       │   │       └── Value.java          # 数值范围注解
│       │   ├── client/
│       │   │   ├── CommonClientEntryPoint.java  # 客户端公共入口（预留）
│       │   │   ├── gui/
│       │   │   │   ├── CommonConfigGUI.java    # 配置界面统一入口
│       │   │   │   ├── ClothConfigGUI.java     # Cloth Config 实现
│       │   │   │   └── YaclConfigGUI.java      # YACL 实现
│       │   │   └── net/
│       │   │       └── NetManager.java          # 客户端网络管理（权限检查、配置推送/缓存）
│       │   ├── server/
│       │   │   ├── CommonServerEntryPoint.java  # 服务端公共入口（预留）
│       │   │   ├── impl/
│       │   │   │   └── IHopperBlockEntityImpl.java  # 分类功能接口
│       │   │   ├── mixin/
│       │   │   │   ├── HopperBlockEntityMixin.java   # 漏斗实体核心修改
│       │   │   │   ├── MinecartHopperMixin.java       # 漏斗矿车修改
│       │   │   │   ├── ServerPlayerMixin.java         # ServerPlayer Accessor（暴露 server 字段）
│       │   │   │   └── IHopperBlockEntityMixin.java   # Accessor/Invoker 接口
│       │   │   └── net/
│       │   │       └── ServerConfigHandler.java  # 服务端配置处理器
│       │   ├── net/
│       │   │   └── INetHelper.java         # 网络辅助接口（平台无关）
│       │   ├── utils/
│       │   │   └── PlayerUtil.java          # 玩家权限检查工具类
│       │   └── platform/
│       │       ├── Services.java           # ServiceLoader 服务加载
│       │       └── IPlatformHelper.java    # 平台抽象接口
│       └── resources/
│           ├── easyhopper.mixins.json       # Mixin 配置
│           ├── pack.mcmeta
│           ├── easyhopper.png               # 模组图标
│           └── assets/easyhopper/lang/
│               ├── en_us.json               # 英文语言文件
│               └── zh_cn.json               # 中文语言文件
│
├── fabric/                         # Fabric 加载器模块
│   └── src/main/
│       ├── java/com/dearxuan/easyhopper/
│       │   ├── EntryPointFabric.java       # Fabric 通用入口点
│       │   ├── FabricNetHelper.java       # Fabric 网络辅助（实现 INetHelper）
│       │   ├── ModMenuIntegration.java     # ModMenu 集成
│       │   ├── client/
│       │   │   └── FabricClientEntryPoint.java  # Fabric 客户端入口点
│       │   ├── server/
│       │   │   └── FabricServerEntryPoint.java  # Fabric 服务端入口点（预留）
│       │   └── platform/
│       │       └── FabricPlatformHelper.java   # Fabric 平台实现
│       └── resources/
│           ├── fabric.mod.json             # Fabric 模组元数据
│           ├── easyhopper.fabric.mixins.json   # Fabric 专用 Mixin 配置
│           └── META-INF/services/...IPlatformHelper  # SPI 服务声明
│
└── neoforge/                       # NeoForge 加载器模块
    └── src/main/
        ├── java/com/dearxuan/easyhopper/
        │   ├── EntryPointNeoForge.java     # NeoForge 通用入口点
        │   ├── NeoForgeNetHelper.java     # NeoForge 网络辅助（实现 INetHelper）
        │   ├── client/
        │   │   └── NeoForgeClientEntryPoint.java  # NeoForge 客户端入口点
        │   ├── server/
        │   │   └── NeoForgeServerEntryPoint.java  # NeoForge 服务端入口点
        │   └── platform/
        │       └── NeoForgePlatformHelper.java  # NeoForge 平台实现
        └── resources/
            ├── META-INF/neoforge.mods.toml         # NeoForge 模组元数据
            ├── easyhopper.neoforge.mixins.json     # NeoForge 专用 Mixin 配置
            └── META-INF/services/...IPlatformHelper  # SPI 服务声明
```

---

## 4. 多加载器架构设计

EasyHopper 采用 **Multi-loader（多加载器）架构**，使同一套核心代码可以同时运行在 Fabric 和 NeoForge 上。这是通过以下机制实现的：

### 4.1 三模块分离 + 客户端/服务端子包

```
┌──────────────────────────────────────────────────────┐
│                     common 模块                       │
│  ┌──────────────┐  ┌─────────────┐  ┌──────────────┐ │
│  │ server/      │  │  config/    │  │  client/     │ │
│  │  mixin/      │  │  (配置)      │  │  gui/        │ │
│  │  impl/       │  │             │  │  net/        │ │
│  │  net/        │  │             │  │              │ │
│  │ (服务端逻辑)   │  │             │  │ (客户端逻辑)   │ │
│  └──────────────┘  └─────────────┘  └──────────────┘ │
│  ┌────────────────────────────────────────────────┐  │
│  │     IPlatformHelper + INetHelper (平台抽象)     │  │
│  └────────────────────────────────────────────────┘  │
└──────────────────────┬───────────────────────────────┘
                       │ ServiceLoader / 接口实现
          ┌────────────┴────────────┐
          ▼                         ▼
┌──────────────────┐  ┌──────────────────┐
│  fabric 模块      │  │  neoforge 模块     │
│                   │  │                   │
│ FabricPlatform   │  │ NeoForgePlatform  │
│    Helper         │  │     Helper        │
│ FabricNetHelper   │  │ NeoForgeNetHelper │
│  (implements      │  │  (implements      │
│   INetHelper)     │  │   INetHelper)     │
│                   │  │                   │
│ FabricClient      │  │ NeoForgeClient    │
│  EntryPoint       │  │  EntryPoint       │
│ FabricServer      │  │ NeoForgeServer    │
│  EntryPoint       │  │  EntryPoint       │
└──────────────────┘  └──────────────────┘
```

- **common**：共享代码层，只能使用原版 Minecraft API 和通用第三方库，不依赖任何加载器特有 API。内部按客户端/服务端划分为 `client/` 和 `server/` 子包，客户端代码（GUI、NetManager）和服务端代码（Mixin、ServerConfigHandler）物理隔离。
- **fabric**：Fabric 专用代码，提供入口点、平台实现和 ModMenu 集成。客户端和服务端入口点分别位于 `client/` 和 `server/` 子包。
- **neoforge**：NeoForge 专用代码，提供入口点和平台实现。客户端和服务端入口点分别位于 `client/` 和 `server/` 子包。

### 4.2 SPI 服务加载机制

common 模块通过 Java 标准的 `ServiceLoader` 机制实现平台抽象：

```java
// Services.java - 服务加载入口
public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

public static <T> T load(Class<T> clazz) {
    return ServiceLoader.load(clazz, Services.class.getClassLoader())
        .findFirst()
        .orElseThrow(...);
}
```

各加载器通过 SPI 配置文件声明自己的实现：

- Fabric: `META-INF/services/com.dearxuan.easyhopper.platform.IPlatformHelper` → `FabricPlatformHelper`
- NeoForge: 同名文件 → `NeoForgePlatformHelper`

运行时，`ServiceLoader` 会自动找到当前加载器打包的实现类并实例化。

### 4.3 源码合并机制

构建时，fabric 和 neoforge 子项目通过 `easyhopper-loader.gradle` 约定插件将 common 的源码和资源合并到自身的编译输出中：

```groovy
// buildSrc/easyhopper-loader.gradle
dependencies {
    commonJava project(path: ':common', configuration: 'commonJava')
    commonResources project(path: ':common', configuration: 'commonResources')
    annotationProcessor project(path: ':common')
}

tasks.named('compileJava', JavaCompile) {
    source(configurations.commonJava)  // 将 common 源码纳入编译
}

processResources {
    from(configurations.commonResources)  // 将 common 资源纳入打包
}
```

`annotationProcessor` 依赖确保 common 中的注解处理器在编译时被激活。common 的代码在编译时会被"注入"到 fabric 和 neoforge 的产物中，最终生成两个独立的 jar 文件。

---

## 5. 构建系统

### 5.1 Gradle 多项目结构

`settings.gradle` 定义了三个子项目：

```groovy
rootProject.name = 'EasyHopper'
include('common')
include('fabric')
include('neoforge')
```

### 5.2 约定插件 (buildSrc)

项目在 `buildSrc` 中定义了两个自定义 Gradle 插件：

#### `easyhopper-common.gradle`

提供所有子项目共享的构建配置：
- **Java 工具链**：统一使用 `gradle.properties` 中指定的 Java 版本
- **资源属性展开**：将 `gradle.properties` 中的变量（如 `${mod_id}`、`${version}`）替换到 `fabric.mod.json`、`neoforge.mods.toml`、`*.mixins.json` 等资源文件中
- **JAR 清单**：注入 `Specification-*` 和 `Implementation-*` 元数据
- **许可证打包**：将 `LICENSE` 文件打入每个 jar
- **Maven 发布**：配置本地发布能力

#### `easyhopper-loader.gradle`

继承 `easyhopper-common`，为加载器子项目（fabric/neoforge）额外提供：
- 依赖 common 项目（编译时）
- 将 common 的 Java 源码和资源合并到当前项目的编译/打包流程
- 将 common 作为 annotationProcessor 依赖
- 使用 Gradle Attribute 机制确保依赖解析时选择正确的加载器变体

### 5.3 仓库配置

项目使用以下 Maven 仓库（按顺序）：

| 仓库 | URL | 用途 |
|------|-----|------|
| Maven Central | 默认 | 通用依赖（SnakeYAML 等） |
| Fabric | `https://maven.fabricmc.net/` | Fabric Loader / API |
| Modrinth | `https://api.modrinth.com/maven` | YACL、Cloth Config、ModMenu |

YACL 和 Cloth Config 的依赖坐标统一使用 Modrinth Maven 格式（`maven.modrinth:yacl`、`maven.modrinth:cloth-config`），后缀区分加载器（`-fabric` / `-neoforge`）。

### 5.4 产物命名

构建产物统一输出到 `target/{version}+mc{minecraft_version}/` 目录，命名格式为：

```
easyhopper-{version}+mc{minecraft_version}-{loader}.jar
easyhopper-{version}+mc{minecraft_version}-{loader}-sources.jar
```

例如：`easyhopper-3.1+mc26.2-fabric.jar`

---

## 6. 核心功能说明

### 6.1 传输速度调节

通过 Mixin 拦截 `HopperBlockEntity.setCooldown()` 方法，将原版固定的传输冷却（8 tick）替换为用户配置的值。同时拦截 `tryMoveItems()` 方法，循环执行多次输入/输出操作，实现单次传输多个物品。

**相关配置项**：
- `HOPPER_TRANSFER_COOLDOWN`：传输冷却 tick 数（1~1200，默认 8）
- `HOPPER_INPUT_COUNT`：单次输入循环次数（1~64，默认 1）
- `HOPPER_OUTPUT_COUNT`：单次输出循环次数（1~64，默认 1）

### 6.2 检测冷却

启用后，漏斗即使没有实际传输物品也会进入冷却（设置冷却时间为 8 tick 并经过 `setCooldown` Mixin 调整），从而避免频繁检测掉落物实体。在大量漏斗的场景下能显著提升性能。

**相关配置项**：
- `HOPPER_EXTRACT_COOLDOWN`：是否启用（默认 false）

### 6.3 漏斗分类过滤

启用后，漏斗的第 5 格（索引 4，最后一格）被用作**过滤槽**：

- **输入限制**：只有与过滤槽物品类型相同的物品才能进入漏斗前 4 格
- **输出限制**：漏斗只会主动输出与过滤槽匹配的物品
- **手动放入**：玩家手动放入不匹配的物品会留在漏斗中，但下方漏斗仍可正常吸取
- **空过滤槽**：当过滤槽为空时，不进行过滤（所有物品均可通过）

**相关配置项**：
- `HOPPER_FILTERING`：是否启用（默认 false）

### 6.4 漏斗矿车冷却

通过 Mixin 拦截 `MinecartHopper.tick()` 方法，使用自定义冷却计数器控制漏斗矿车的传输频率。每次 tick 递减计数器，未归零时取消原版 tick 逻辑，归零后重置为配置的冷却值。

**相关配置项**：
- `HOPPER_MINECART_TRANSFER_COOLDOWN`：冷却 tick 数（1~1200，默认 1）

---

## 7. 配置系统

配置系统由三个核心类组成，采用注解驱动的设计模式。

### 7.1 注解定义

#### `@EasyConfig`

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface EasyConfig {
    Value value() default @Value;          // 数值范围约束
    String tooltip() default "<modid>.<name>.tooltip";  // 自定义 tooltip 键
    boolean allowInGame() default true;    // 是否允许在游戏内 GUI 中编辑
}
```

标记在 `ModConfig` 的字段上，表示该字段是一个可配置项。运行时通过反射读取此注解，自动完成 GUI 构建、YAML 注释生成和数值校验。`allowInGame` 控制该配置项是否在游戏内 GUI 中可编辑（设为 `false` 时 GUI 中灰显且不可修改，只能通过配置文件修改）。

#### `@Value`

```java
@Retention(RetentionPolicy.RUNTIME)
public @interface Value {
    float min() default 0;
    float max() default 255;
}
```

定义数值类型配置项的最小值和最大值范围。

### 7.2 ModConfig — 配置数据类

```java
public class ModConfig {
    public static ModConfig INSTANCE = new ModConfig();
    public static ModConfig SERVER_CONFIG = new ModConfig();

    @EasyConfig(value = @Value(min = 1, max = 1200))
    public int HOPPER_TRANSFER_COOLDOWN = 8;

    @EasyConfig(value = @Value(min = 1, max = 64))
    public int HOPPER_INPUT_COUNT = 1;

    @EasyConfig(value = @Value(min = 1, max = 64))
    public int HOPPER_OUTPUT_COUNT = 1;

    @EasyConfig
    public boolean HOPPER_FILTERING = false;

    @EasyConfig
    public boolean HOPPER_EXTRACT_COOLDOWN = false;

    @EasyConfig(value = @Value(min = 1, max = 1200))
    public int HOPPER_MINECART_TRANSFER_COOLDOWN = 1;

    @EasyConfig(allowInGame = false)
    public boolean ALLOW_OP_MODIFY = true;
}
```

使用单例模式（`INSTANCE`），字段名即为 YAML 键名，字段的初始值即为默认值。`@EasyConfig` 注解为 GUI 和配置文件生成提供元数据。`ALLOW_OP_MODIFY` 标记为 `allowInGame = false`，在游戏内 GUI 中灰显不可编辑，只能通过配置文件修改。

`INSTANCE` 静态字段作为服务器配置缓存：

- 进入服务器时，服务端推送配置覆盖此实例
- 服务端修改配置时，广播消息覆盖此实例
- 断开连接时，重新初始化为本地配置

`load()` 方法在加载完成后同步初始化 `INSTANCE` 为 `INSTANCE` 的深拷贝副本。

### 7.3 ConfigManager — 配置管理器

负责配置的读取、写入、注释生成和数值校正。

#### 配置文件位置

```
config/easyhopper.yaml
```

#### 加载流程 (`load()`)

```
配置文件不存在？
  ├── 是 → 调用 save() 创建默认配置文件 → 返回
  └── 否 → 1. 用原生 Yaml 读取为 Map，检查是否有缺失的配置 key
           2. 用 SnakeYAML 反序列化为 ModConfig
              → 成功 → 替换 INSTANCE
              → 失败 → 使用默认 INSTANCE
           3. validateAndSanitize() 内存校正超限数值
           4. 若步骤 1 检测到缺失配置项 → 调用 save() 补全写回磁盘
```

关键设计：当检测到老配置文件缺失新增的配置项时，`load()` 会自动调用 `save()` 将缺失项补全写回磁盘；其余情况下仅内存校正，不覆写磁盘。

#### 保存流程 (`save()`)

```
确保 config 目录存在
→ generateYamlString() 生成带注释的 YAML 字符串
→ 写入 config/easyhopper.yaml
```

#### YAML 注释生成 (`generateYamlString()`)

通过反射读取 `@EasyConfig` 注解，为每个配置项生成：
1. 翻译后的配置名称（以 `# ` 开头的注释行）
2. 翻译后的 tooltip 描述（多行注释）
3. 数值范围提示（如 `# Range: [1 ~ 1200]`）

翻译文本通过 `getTranslationWithFallback()` 获取，优先使用 Minecraft 当前语言，若不存在则回退到内置的 `en_us.json`。

#### 数值校正 (`validateAndSanitize()`)

遍历所有 `@EasyConfig` 标记的 int 类型字段，将超出 `[min, max]` 范围的值钳制到合法范围内：

```java
int sanitized = Math.max(min, Math.min(max, currentVal));
```

---

## 8. 客户端/服务端代码隔离

EasyHopper 通过**包结构物理隔离** + **加载器环境机制**实现客户端与服务端代码分离，防止跨环境引用导致的运行时崩溃。

### 8.1 包结构隔离

common 模块内部按客户端/服务端划分为 `client/` 和 `server/` 子包：

| 子包 | 内容 | 运行环境 |
|------|------|----------|
| `client/gui/` | 配置界面（CommonConfigGUI、ClothConfigGUI、YaclConfigGUI） | 仅客户端 |
| `client/net/` | 客户端网络管理（NetManager） | 仅客户端 |
| `server/mixin/` | Mixin 类（HopperBlockEntityMixin 等） | Mixin 配置声明在 `server` 端，仅服务端加载 |
| `server/impl/` | 服务端接口（IHopperBlockEntityImpl） | 与 Mixin 共享，仅服务端实际使用 |
| `server/net/` | 服务端配置处理（ServerConfigHandler） | 仅服务端 |
| `config/` | 配置数据类、ConfigManager、ConfigSyncPayload | 双端共享 |
| `net/` | INetHelper 接口 | 双端共享 |
| `platform/` | IPlatformHelper、Services | 双端共享 |
| `utils/` | PlayerUtil | 双端共享 |

根包下的 `CommonClass`、`Constants` 为双端共享代码。

### 8.2 加载器环境机制

各加载器提供了自己的客户端/服务端隔离机制：

- **Fabric**：通过 `fabric.mod.json` 中的 `entrypoints.client` 和 `entrypoints.server` 分别声明客户端和服务端入口点（`ClientModInitializer` / `ModInitializer`），Fabric Loader 仅在对应物理端加载这些类
- **NeoForge**：通过 `@EventBusSubscriber(value = Dist.CLIENT)` / `@EventBusSubscriber(value = Dist.DEDICATED_SERVER)` 注解确保事件监听类仅在对应物理端加载
- **Mixin 配置**：`easyhopper.mixins.json` 的 `server` 数组确保 Mixin 仅在服务端环境被应用

### 8.3 隔离设计总结

客户端代码（`client/` 包）依赖 `net.minecraft.client.*`，服务端代码（`server/` 包）依赖 `net.minecraft.server.*`，通过包结构和加载器机制实现物理隔离。双端共享代码（`config/`、`platform/`、`net/`、`utils/`）不依赖任何端特定 API，确保在两个环境中均可安全执行。

---

## 9. Mixin 模块

Mixin 是本模组的核心技术，通过运行时字节码注入修改原版漏斗的行为。所有 Mixin 类位于 `server/mixin/` 子包下，通过 `easyhopper.mixins.json` 的 `server` 端声明控制仅在服务端加载 Mixin。

### 9.1 HopperBlockEntityMixin

这是最核心的 Mixin 类，修改了原版 `HopperBlockEntity`（漏斗方块实体）的多个行为。

**类声明**：

```java
@Mixin(value = HopperBlockEntity.class, priority = 500)
@Implements(@Interface(iface = IHopperBlockEntityImpl.class, prefix = "impl$"))
public abstract class HopperBlockEntityMixin
    extends RandomizableContainerBlockEntity
    implements Hopper, IHopperBlockEntityImpl
```

- `priority = 500`：设置较低的优先级，以便与其他模组的 Mixin 兼容。
- `@Implements` + `@Interface`：使用 Mixin 的接口注入功能，将 `impl$` 前缀的方法暴露为 `IHopperBlockEntityImpl` 接口方法，使其他代码可通过接口类型安全调用。

**Shadow 字段与方法**：

```java
@Shadow private int cooldownTime;       // 漏斗冷却时间
@Shadow private Direction facing;       // 漏斗朝向
@Shadow private NonNullList<ItemStack> items;  // 物品列表
@Shadow private static boolean ejectItems(...);  // 原版静态输出方法
```

`@Shadow` 声明对原版私有字段和方法的访问。

**构造函数**：将物品列表初始化为 5 格（保持原版一致），冷却设为 -1，朝向从 BlockState 获取。

**该类实现的功能**：

1. **传输冷却自定义**：通过 `@ModifyVariable` 拦截 `setCooldown` 方法的参数，将原版冷却值替换为用户配置的 `HOPPER_TRANSFER_COOLDOWN`。计算方式为 `原值 - MOVE_ITEM_SPEED + 用户配置值`。

2. **多次输入/输出**：通过 `@Inject` 完全接管 `tryMoveItems` 方法，在冷却检查通过后，循环执行输出（`ejectItems`）最多 `HOPPER_OUTPUT_COUNT` 次（且漏斗非空时继续），循环执行输入（`action.getAsBoolean()`）最多 `HOPPER_INPUT_COUNT` 次（且漏斗未满时继续）。客户端直接跳过。若发生物品变动**或** `HOPPER_EXTRACT_COOLDOWN` 启用，则设置冷却并标记方块更新。

3. **过滤槽保护**：通过 `@Redirect` 拦截 `tryTakeInItemFromSlot` 中的 `getItem` 调用，当当前漏斗从上方容器吸取物品时，若上方容器也是漏斗且目标为第 5 格（过滤槽），则返回空物品，防止过滤物品被吸走。

4. **容器大小修改**：通过两个 `@Redirect` 分别拦截 `ejectItems` 和 `addItem` 中的 `getContainerSize()` 调用，当启用过滤时返回 `容器大小 - 1`，使前 4 格参与传输，过滤槽被排除。其中 `ejectItems` 的拦截目标为 `HopperBlockEntity.getContainerSize()`，`addItem` 的拦截目标为 `Container.getContainerSize()` 且仅对 `HopperBlockEntity` 实例生效。

5. **输出过滤**：通过 `@Redirect` 拦截 `ejectItems` 中的 `isEmpty()` 调用，不匹配过滤类型的物品被视为"空"，从而跳过输出。

6. **放置限制**：重写 `canPlaceItem`，启用过滤时禁止向过滤槽放入物品，且只允许放入与过滤类型匹配的物品。

7. **空状态判断**：重写 `isEmpty()`，启用过滤时只检查前 4 格是否为空，忽略过滤槽。

8. **满载判断**：通过 `@Inject` 注入 `inventoryFull`，启用过滤时只检查前 4 格是否满载。

9. **接口实现**：通过 `impl$` 前缀方法实现 `IHopperBlockEntityImpl` 接口的三个方法（获取过滤槽物品、判断物品是否匹配过滤、获取过滤后有效容器大小）。

### 9.2 MinecartHopperMixin

```java
@Mixin(value = MinecartHopper.class, priority = 500)
public abstract class MinecartHopperMixin extends AbstractMinecartContainer implements Hopper
```

注入 `MinecartHopper.tick()` 方法（`@Inject` HEAD + cancellable），使用 `@Unique` 标记的自定义冷却计数器（字段名 `easyHopperNeoForge$cooldown`）控制漏斗矿车的传输频率。每次 tick 递减计数器，未归零时取消原版 tick 逻辑，归零后重置为 `HOPPER_MINECART_TRANSFER_COOLDOWN` 配置值。

### 9.3 ServerPlayerMixin — Accessor

```java
@Mixin(value = ServerPlayer.class, priority = 500)
public interface ServerPlayerMixin {
  @Accessor("server")
  MinecraftServer getServer();
}
```

通过 Mixin 的 `@Accessor` 注解，暴露原版 `ServerPlayer` 的 `server` 字段（`MinecraftServer` 实例）。NeoForge 端在处理 C2S 配置修改请求时，需要通过此接口获取 `MinecraftServer` 实例来遍历在线玩家列表进行配置广播。

### 9.4 IHopperBlockEntityMixin — Accessor/Invoker

```java
@Mixin(value = HopperBlockEntity.class, priority = 500)
interface IHopperBlockEntityMixin
```

通过 Mixin 的 `@Accessor` 和 `@Invoker` 注解，暴露原版 `HopperBlockEntity` 的内部字段和不可外部调用的方法，供 `HopperBlockEntityMixin` 内部使用：

| 注解 | 方法 | 用途 |
|------|------|------|
| `@Accessor` | `accessGetItems()` | 访问 `items` 字段 |
| `@Invoker` | `invokeGetItems()` | 调用 `getItems()` 方法 |
| `@Invoker` | `invokeInventoryFull()` | 调用 `inventoryFull()` 方法 |
| `@Invoker` | `invokeIsOnCooldown()` | 调用 `isOnCooldown()` 方法 |
| `@Invoker` | `invokeSetCooldown(int)` | 调用 `setCooldown()` 方法 |

### 9.5 IHopperBlockEntityImpl — 接口定义

```java
public interface IHopperBlockEntityImpl {
    ItemStack getClassifiedItemStack();
    boolean canTransferItem(ItemStack itemStack);
    int getContainerSizeAfterClassification();
}
```

定义分类功能相关的三个方法，由 `HopperBlockEntityMixin` 通过 `@Interface` 机制实现。这使得其他 Mixin 代码可以通过接口类型安全地调用这些方法，而非依赖 Duck Typing。

---

## 10. 网络功能

3.1 版本新增了**多人服务器配置同步**功能，允许客户端连接服务器时查看和修改服务端的 EasyHopper 配置，管理员的修改可实时同步到服务端并广播给所有在线玩家。

### 10.1 设计概要

- **单一 Payload**：使用 `ConfigSyncPayload` 一个包类型同时承载 C2S（客户端推送配置）和 S2C（服务端同步配置），通过 JSON 字符串序列化 `ModConfig`
- **服务器配置缓存**：`ModConfig.SERVER_CONFIG` 在客户端缓存服务端配置，断开连接时自动恢复本地配置
- **OP 权限校验**：服务端接收配置推送时通过 `PlayerUtil.hasPermissionToPushConfig()` 校验玩家 OP 等级及 `ALLOW_OP_MODIFY` 配置项
- **内存-only 更新**：服务端收到 C2S 配置推送后仅更新 `ModConfig.INSTANCE`（内存），不调用 `ConfigManager.save()` 写入磁盘，服务器重启后恢复为磁盘上的原始配置
- **自动推送**：玩家进服时自动推送当前服务端配置；断开连接时自动清除缓存
- **GUI 联动**：配置界面根据多人/单人模式切换显示，服务器模式下非管理员只读
- **代码隔离**：客户端网络逻辑（`NetManager`，位于 `client/net/` 包）和服务端配置处理（`ServerConfigHandler`，位于 `server/net/` 包）通过包结构物理隔离，`INetHelper` 接口解耦

### 10.2 ConfigSyncPayload — 网络包定义

```java
public record ConfigSyncPayload(String jsonConfig) implements CustomPacketPayload {
  public ConfigSyncPayload(ModConfig config)  // 从 ModConfig 序列化
  public ModConfig toConfig()                  // 反序列化为 ModConfig
}
```

位于 `config/` 包，使用 Gson 将 `ModConfig` 序列化为 JSON 字符串传输。包标识为 `easyhopper:config_sync`，同一 Payload 类型同时注册为 C2S 和 S2C。由于客户端和服务端都需要序列化/反序列化此包，它位于双端共享的 `config/` 包中。

### 10.3 INetHelper — 网络辅助接口

```java
public interface INetHelper {
    void registerPackets();                              // 注册 Payload 类型及处理器
    void registerPlayerJoinEvent();                       // 注册玩家进服事件
    void sendToPlayer(ServerPlayer player, ConfigSyncPayload payload);  // 发送配置包
}
```

定义与模组加载端无关的网络操作接口，由 `FabricNetHelper` 和 `NeoForgeNetHelper` 分别实现。这是 Fabric/NeoForge 网络差异的抽象层，common 中的入口点通过此接口初始化网络功能，无需直接依赖加载器特有 API。

### 10.4 NetManager — 客户端网络管理

```java
public class NetManager { ... }
```

位于 `client/net/` 包，是客户端专属的静态工具类：

| 方法                              | 说明                                            |
|---------------------------------|-----------------------------------------------|
| `hasPermissionToPush()`         | 检查当前玩家是否有 OP 权限且 `ALLOW_OP_MODIFY` 为 true |
| `loadConfigFromServer()`        | 获取 `INSTANCE` 的深拷贝副本（供 GUI 读取）           |
| `pushConfigToServer(ModConfig)` | 通过 `ServerboundCustomPayloadPacket` 将配置推送到服务端 |
| `updateServerConfig(ModConfig)` | 接收服务端推送的配置，更新 `INSTANCE` 和 `INSTANCE`    |
| `clearCache()`                  | 断开连接时清除缓存，重新加载本地配置                            |
| `deepCopy(ModConfig)`           | 使用 Gson 进行深拷贝                                 |

### 10.5 ServerConfigHandler — 服务端配置处理器

```java
public class ServerConfigHandler {
  public static boolean applyConfigFromPlayer(ServerPlayer player, ConfigSyncPayload payload)
}
```

位于 `server/net/` 包，仅依赖服务端安全的类，处理客户端推送的配置修改：校验权限（`PlayerUtil.hasPermissionToPushConfig()`），应用配置（`ModConfig.INSTANCE = config`），打印日志。权限校验同时检查 OP 等级和 `ALLOW_OP_MODIFY` 配置项。

### 10.6 PlayerUtil — 权限工具类

```java
public class PlayerUtil {
  public static boolean hasOpPermission(Player player)              // 检查 OP 等级 >= GAMEMASTERS
  public static boolean hasPermissionToPushConfig(Player player)    // hasOpPermission + ALLOW_OP_MODIFY
}
```

使用 Minecraft 原版权限 API 检查玩家权限。`hasPermissionToPushConfig()` 新增了对 `ALLOW_OP_MODIFY` 配置项的检查，只有同时满足 OP 权限和 `ALLOW_OP_MODIFY = true` 时才允许推送配置。

### 10.7 Fabric 网络实现

**FabricNetHelper** 实现 `INetHelper` 接口：

- `registerPackets()`：向 `PayloadTypeRegistry` 注册 C2S 和 S2C 的 `ConfigSyncPayload`，并注册服务端 `registerGlobalReceiver` 接收器，接收客户端配置推送，通过 `ServerConfigHandler.applyConfigFromPlayer()` 校验并应用，成功后广播给其他在线玩家
- `registerPlayerJoinEvent()`：注册 `ServerPlayConnectionEvents.JOIN`，玩家进服时推送配置
- `sendToPlayer()`：使用 `ServerPlayNetworking.send()` 发送

**FabricClientEntryPoint**（`ClientModInitializer`）负责客户端事件：

- 连接建立时注册 `ClientPlayNetworking` 接收器，接收服务端配置同步并调用 `NetManager.updateServerConfig()`
- 断开连接时调用 `NetManager.clearCache()` 恢复本地配置

**EntryPointFabric** 中创建 `FabricNetHelper` 实例并调用 `registerPackets()` 和 `registerPlayerJoinEvent()`。

### 10.8 NeoForge 网络实现

**NeoForgeNetHelper** 实现 `INetHelper` 接口（实例类，持有 `modEventBus` 和 `neoForgeEventBus`）：

- `registerPackets()`：在 Mod 事件总线上监听 `RegisterPayloadHandlersEvent`，使用 `registrar.playBidirectional()` 同时注册 C2S 和 S2C 处理器
- `registerPlayerJoinEvent()`：在 NeoForge 事件总线上监听 `PlayerLoggedInEvent`，向新加入的玩家推送配置
- `sendToPlayer()`：使用 `PacketDistributor.sendToPlayer()` 发送

C2S 处理器中通过 `ServerConfigHandler.applyConfigFromPlayer()` 校验并应用配置，成功后通过 `ServerPlayerMixin` Accessor 获取 `MinecraftServer` 广播给其他玩家。S2C 处理器调用 `NetManager.updateServerConfig()`。

**EntryPointNeoForge** 中创建 `NeoForgeNetHelper` 实例并调用 `registerPackets()` 和 `registerPlayerJoinEvent()`。

---

## 11. 配置 GUI 系统

所有 GUI 类位于 `client/gui/` 子包下，仅客户端可用。

### 11.1 CommonConfigGUI — 统一入口

```java
public class CommonConfigGUI {

    public static Screen createScreen(Screen parentScreen) {
        // 优先级: Cloth Config > YACL > Toast 提示
        if (isModLoaded("cloth-config") || isModLoaded("cloth_config")) {
          try {
            return ClothConfigGUI.createScreen(parentScreen);
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }
        if (isModLoaded("yet_another_config_lib_v3") || isModLoaded("yacl")) {
          try {
            return YaclConfigGUI.createScreen(parentScreen);
          } catch (Throwable e) {
            e.printStackTrace();
          }
        }
        showMissingConfigToast();  // 弹出原生 Toast 提示
        return null;
    }
}
```

通过 `Services.PLATFORM.isModLoaded()` 检测当前安装了哪个配置库，按优先级返回对应的配置界面。每次 `createScreen` 调用均包裹 `try-catch`，即使配置库存在但初始化异常时也不会导致崩溃，而是静默降级到下一个备选或弹出 Toast。若都未安装，弹出游戏内 Toast 通知用户。

多人模式支持的辅助方法：

| 方法                      | 说明                                                                          |
|-------------------------|-----------------------------------------------------------------------------|
| `getConfig()`           | 多人模式返回 `NetManager.loadConfigFromServer()`（深拷贝），单人模式返回 `ModConfig.INSTANCE` |
| `saveConfig(ModConfig)` | 多人模式推送指定配置到服务器，单人模式调用 `ConfigManager.save()`                                |
| `isInMultiplayer()`     | 通过 `Minecraft.getInstance().level != null && getCurrentServer() != null` 判断 |
| `hasPermission()`       | 单人模式始终允许；多人模式需要 OP 权限且 `ALLOW_OP_MODIFY = true`                          |

### 11.2 ClothConfigGUI

基于 Cloth Config API 构建配置界面。通过反射遍历 `ModConfig` 的所有 `@EasyConfig` 字段，根据字段类型（int / boolean）创建对应的配置条目（`startIntField` / `startBooleanToggle`），设置范围约束和 tooltip，并通过 `setSaveConsumer` 将值写回目标配置对象。

**多人服务器支持**：

- 分类标题根据模式切换：服务器模式显示 `easyhopper.gui.server_config`，本地模式显示 `easyhopper.gui.local_config`
- 顶部添加提示栏：管理员显示可编辑提示（绿色），非管理员显示只读提示（红色），本地模式显示本地编辑提示（蓝色）
- 读取目标配置使用 `CommonConfigGUI.getConfig()`（多人模式从服务器缓存读取，单人模式读取本地）
- 编辑性判断：`easyConfig.allowInGame() && hasPermission`，无权限时所有选项置灰
- 保存逻辑：通过 `CommonConfigGUI.saveConfig(targetConfig)` 统一处理，多人模式推送到服务器，单人模式写入本地磁盘

### 11.3 YaclConfigGUI

基于 YACL (Yet Another Config Lib) 构建配置界面，逻辑结构与 ClothConfigGUI 类似，但使用 YACL 的 API：通过 `Option.binding()` 绑定配置值的 getter/setter，使用 `IntegerFieldControllerBuilder` / `BooleanControllerBuilder` 创建控制器。

**多人服务器支持**：

- 与 ClothConfigGUI 相同的模式检测、标题切换、权限提示逻辑
- 分类标题根据模式切换，顶部添加 `LabelOption` 提示文字
- `.available(editable)` 中 `editable = easyConfig.allowInGame() && hasPermission`
- binding 的 getter/setter 操作目标配置（`targetConfig`）而非固定的 `ModConfig.INSTANCE`
- 保存逻辑通过 `CommonConfigGUI.saveConfig(targetConfig)` 统一处理

### 11.4 GUI 集成入口

| 平台 | 集成方式 |
|------|----------|
| **Fabric** | 通过 `ModMenuIntegration` 实现 `ModMenuApi`，在 ModMenu 中添加配置按钮 |
| **NeoForge** | 通过 `NeoForgeClientEntryPoint` 中的 `@EventBusSubscriber(value = Dist.CLIENT)` 在 `FMLClientSetupEvent` 中注册 `IConfigScreenFactory` |

两者最终都调用 `CommonConfigGUI.createScreen()` 获取配置界面。

---

## 12. 平台抽象层

### 12.1 IPlatformHelper 接口

> `IPlatformHelper` 位于 `platform` 包（非 `platform.services`），SPI 配置文件路径与之对应。

```java
public interface IPlatformHelper {
    String getPlatformName();           // 获取平台名称 ("Fabric" / "NeoForge")
    boolean isModLoaded(String modId);  // 检查指定模组是否已加载
    boolean isDevelopmentEnvironment(); // 是否处于开发环境
    default String getEnvironmentName() { ... }  // 环境名称 ("development" / "production")
}
```

### 12.2 FabricPlatformHelper

```java
public class FabricPlatformHelper implements IPlatformHelper {
    public String getPlatformName() { return "Fabric"; }
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }
}
```

### 12.3 NeoForgePlatformHelper

```java
public class NeoForgePlatformHelper implements IPlatformHelper {
    public String getPlatformName() { return "NeoForge"; }
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.getCurrent().isProduction();
    }
}
```

### 12.4 Services 加载器

`Services` 类使用 Java `ServiceLoader` 在运行时动态加载当前平台的实现。这是多加载器架构的关键——common 代码不需要知道当前运行在哪个平台上，只需通过 `Services.PLATFORM` 即可获取平台信息。

> 3.1 版本的网络功能**不使用** SPI 机制，而是通过 `INetHelper` 接口由各加载器的 `FabricNetHelper` / `NeoForgeNetHelper` 实现，common 中的入口点通过接口引用调用。

---

## 13. 入口与初始化流程

### 13.1 Fabric 初始化流程

```
游戏启动
  → Fabric Loader 发现 fabric.mod.json 中的 entrypoints.main
  → 调用 EntryPointFabric.onInitialize()
    → CommonClass.init()
      → ModConfig.load()
        → ConfigManager.load()
          → 读取 config/easyhopper.yaml（不存在则创建默认）
          → 反序列化 + 数值校正
          → 加载完成，ModConfig.INSTANCE 就绪
          → ModConfig.SERVER_CONFIG = deepCopy(INSTANCE)

    → 创建 FabricNetHelper 实例（实现 INetHelper）
    → netHelper.registerPackets()               # 注册 C2S/S2C Payload + 服务端接收器
    → netHelper.registerPlayerJoinEvent()       # 玩家进服自动推送配置

  → Fabric Loader 发现 entrypoints.client
  → 调用 FabricClientEntryPoint.onInitializeClient()
    → ClientPlayConnectionEvents.INIT          # 注册 S2C 配置同步接收器
    → ClientPlayConnectionEvents.DISCONNECT    # 断开连接时清除缓存

  → Fabric Loader 发现 entrypoints.server
  → 调用 FabricServerEntryPoint（预留占位）

  → Fabric Loader 发现 entrypoints.modmenu
  → 注册 ModMenuIntegration（提供配置界面按钮）
```

### 13.2 NeoForge 初始化流程

```
游戏启动
  → NeoForge 发现 @Mod("easyhopper") 注解
  → 创建 EntryPointNeoForge 实例
    → CommonClass.init()
      → ModConfig.load()  （同 Fabric）

    → 创建 NeoForgeNetHelper 实例（实现 INetHelper，持有 modEventBus + neoForgeEventBus）
    → netHelper.registerPackets()               # 注册 C2S/S2C Payload（playBidirectional）
    → netHelper.registerPlayerJoinEvent()       # 玩家进服自动推送配置

  → NeoForge 客户端加载时：
  → NeoForgeClientEntryPoint.onClientSetup()  (@EventBusSubscriber(Dist.CLIENT))
    → 注册 IConfigScreenFactory（提供配置界面按钮）

  → NeoForge 专用服务端加载时：
  → NeoForgeServerEntryPoint.onServerSetup()  (@EventBusSubscriber(Dist.DEDICATED_SERVER))
    → 服务端初始化逻辑（预留）
```

### 13.3 配置界面打开流程

```
玩家点击"配置"按钮
  │
  ├─ [Fabric] ModMenuIntegration.getModConfigScreenFactory()
  └─ [NeoForge] NeoForgeClientEntryPoint → IConfigScreenFactory 回调
      │
      ▼（两者均调用）
  CommonConfigGUI.createScreen(parentScreen)
    ├─ 检测到 Cloth Config → ClothConfigGUI.createScreen()
    ├─ 或检测到 YACL → YaclConfigGUI.createScreen()
    └─ 都未安装 → 弹出 Toast 提示 → 返回 null

  → GUI 判断多人/单人模式（CommonConfigGUI.isInMultiplayer()）
  → 读取目标配置（CommonConfigGUI.getConfig()）
  → 判断修改权限（CommonConfigGUI.hasPermission()）
  → 分类标题切换 + 权限提示栏
  → 无权限时所有选项置灰

  → 显示配置界面
  → 玩家修改值并点击保存
    ├─ [本地模式] CommonConfigGUI.saveConfig(config) → ConfigManager.save() 写入磁盘
    └─ [服务器模式] CommonConfigGUI.saveConfig(config) → NetManager.pushConfigToServer()
        → 服务端校验 OP + ALLOW_OP_MODIFY + 更新 ModConfig.INSTANCE + 广播给其他玩家（不写入磁盘）
```

### 13.4 Mixin 加载流程

```
游戏启动
  → Mixin 环境初始化
  → 读取 easyhopper.mixins.json（common Mixin 配置）
     package = "com.dearxuan.easyhopper"
     server = [server.mixin.HopperBlockEntityMixin, server.mixin.IHopperBlockEntityMixin,
              server.mixin.MinecartHopperMixin, server.mixin.ServerPlayerMixin]
  → 读取 easyhopper.fabric.mixins.json / easyhopper.neoforge.mixins.json（平台专用，当前为空）
  → 服务端环境应用以下 Mixin:
    - HopperBlockEntityMixin（核心漏斗逻辑）
    - IHopperBlockEntityMixin（Accessor/Invoker）
    - MinecartHopperMixin（漏斗矿车）
    - ServerPlayerMixin（ServerPlayer Accessor，暴露 server 字段）
  → 原版 HopperBlockEntity / MinecartHopper / ServerPlayer 字节码被修改
  → 游戏运行时使用修改后的类
```

> 所有 Mixin 类均声明在 `server` 端配置中（`easyhopper.mixins.json` 的 `server` 数组），Mixin 框架仅在服务端加载并应用这些 Mixin。客户端不会实际加载 Mixin 的字节码修改；`HopperBlockEntityMixin` 内部的 `isClientSide()` 检查也在逻辑层面保护客户端安全跳过。

---

## 14. 资源文件与国际化

### 14.1 Mixin 配置文件

| 文件                                | 位置       | 说明                              |
|-----------------------------------|----------|---------------------------------|
| `easyhopper.mixins.json`          | common   | 核心 Mixin 配置，4 个 Mixin 类声明在 `server` 端 |
| `easyhopper.fabric.mixins.json`   | fabric   | Fabric 专用 Mixin 配置（当前为空，预留扩展）   |
| `easyhopper.neoforge.mixins.json` | neoforge | NeoForge 专用 Mixin 配置（当前为空，预留扩展） |

核心 Mixin 配置（`easyhopper.mixins.json`）：

```json
{
  "package": "com.dearxuan.easyhopper",
  "mixins": [],
  "client": [],
  "server": [
    "server.mixin.HopperBlockEntityMixin",
    "server.mixin.IHopperBlockEntityMixin",
    "server.mixin.MinecartHopperMixin",
    "server.mixin.ServerPlayerMixin"
  ]
}
```

基础包路径为 `com.dearxuan.easyhopper`，所有 Mixin 类使用 `server.mixin.` 前缀的相对路径，声明在 `server` 端以确保仅服务端加载。

### 14.2 模组元数据

| 平台       | 文件                   | 关键信息                                                                                                                                                 |
|----------|----------------------|------------------------------------------------------------------------------------------------------------------------------------------------------|
| Fabric   | `fabric.mod.json`    | 声明入口点 `EntryPointFabric`（main）、`FabricClientEntryPoint`（client）、`FabricServerEntryPoint`（server）和 `ModMenuIntegration`（modmenu），Mixin 引用，依赖声明（fabricloader、fabric-api、minecraft、java） |
| NeoForge | `neoforge.mods.toml` | 声明模组 ID、版本、许可证、作者等信息，Mixin 引用，NeoForge + Minecraft 依赖声明。入口点由 Java 源码中的 `@Mod("easyhopper")` 注解驱动，而非在此文件中声明                                           |

### 14.3 语言文件

支持中文（`zh_cn.json`）和英文（`en_us.json`）两种语言，包含：

- 配置项名称与 tooltip 翻译
- ModMenu 模组名称与描述
- Toast 提示文本
- 服务器模式提示文本（3.1 新增）：
  - `easyhopper.gui.server_config` / `easyhopper.gui.local_config`：分类标题
  - `easyhopper.gui.notice.server_editable`：服务器模式可编辑提示（绿色）
  - `easyhopper.gui.notice.server_readonly`：服务器模式只读提示（红色）
  - `easyhopper.gui.notice.local`：本地模式提示（蓝色）

语言键命名规则：
- 配置项名称：`easyhopper.{FIELD_NAME}`
- 配置项 tooltip：`easyhopper.{FIELD_NAME}.tooltip`
- GUI 提示：`easyhopper.gui.{KEY}`

### 14.4 SPI 服务声明

| 平台       | 文件路径                                                                 | 内容                                                        |
|----------|----------------------------------------------------------------------|-----------------------------------------------------------|
| Fabric   | `META-INF/services/com.dearxuan.easyhopper.platform.IPlatformHelper` | `com.dearxuan.easyhopper.platform.FabricPlatformHelper`   |
| NeoForge | 同名文件                                                                 | `com.dearxuan.easyhopper.platform.NeoForgePlatformHelper` |

> 3.1 版本的网络功能不使用 SPI 机制，Fabric 和 NeoForge 各自的 NetHelper 通过实现 `INetHelper` 接口在入口点中实例化调用。

---

## 附录：配置项速查表

| 配置键 | 类型 | 默认值 | 范围 | 说明 |
|--------|------|--------|------|------|
| `HOPPER_TRANSFER_COOLDOWN` | int | 8 | 1~1200 | 漏斗传输冷却（tick） |
| `HOPPER_INPUT_COUNT` | int | 1 | 1~64 | 漏斗单次输入循环次数 |
| `HOPPER_OUTPUT_COUNT` | int | 1 | 1~64 | 漏斗单次输出循环次数 |
| `HOPPER_FILTERING` | boolean | false | — | 是否启用漏斗分类过滤 |
| `HOPPER_EXTRACT_COOLDOWN` | boolean | false | — | 是否启用检测冷却 |
| `HOPPER_MINECART_TRANSFER_COOLDOWN` | int | 1 | 1~1200 | 漏斗矿车传输冷却（tick） |
| `ALLOW_OP_MODIFY` | boolean | true | — | 是否允许管理员在游戏内修改配置（游戏内灰显不可编辑，仅配置文件可改；同时控制服务器配置推送权限） |

---

## 附录：类索引

| 类名                        | 包路径 (相对 com.dearxuan.easyhopper)  | 职责                                                                         |
|---------------------------|-----------------------------------|----------------------------------------------------------------------------|
| `Constants`               | (root)                            | 定义 MOD_ID、MOD_NAME、LOG（Logger 实例）                                          |
| `CommonClass`             | (root)                            | 共享初始化入口，加载配置                                                               |
| `ModConfig`               | config                            | 配置数据类（单例），定义所有可配置项 + 服务器配置缓存                                               |
| `ConfigManager`           | config                            | 配置文件读写、YAML 注释生成、数值校正、翻译回退                                                |
| `ConfigSyncPayload`       | config                            | 配置同步网络包：JSON 序列化 ModConfig，C2S + S2C 双向                                    |
| `EasyConfig`              | config.retention                   | 配置注解，标记可配置字段，声明数值范围、tooltip 键与游戏内可编辑性                                      |
| `Value`                   | config.retention                   | 数值范围注解（min/max）                                                            |
| `CommonClientEntryPoint`  | client                            | 客户端公共入口（预留占位）                                                              |
| `CommonConfigGUI`         | client.gui                        | 配置 GUI 统一入口 + 多人模式辅助方法（getConfig/saveConfig/isInMultiplayer/hasPermission） |
| `ClothConfigGUI`          | client.gui                        | Cloth Config 配置界面构建，支持服务器模式权限控制                                            |
| `YaclConfigGUI`           | client.gui                        | YACL 配置界面构建，支持服务器模式权限控制                                                    |
| `NetManager`              | client.net                        | 客户端网络管理：权限检查、配置推送/缓存/清除/深拷贝                                                |
| `CommonServerEntryPoint`  | server                            | 服务端公共入口（预留占位）                                                              |
| `IHopperBlockEntityImpl`  | server.impl                       | 分类功能接口定义，由 HopperBlockEntityMixin 实现                                       |
| `HopperBlockEntityMixin`  | server.mixin                      | 漏斗核心逻辑 Mixin：传输冷却、多次输入输出、分类过滤                                               |
| `MinecartHopperMixin`     | server.mixin                      | 漏斗矿车 Mixin：`@Unique` 字段 `easyHopperNeoForge$cooldown` 控制传输频率               |
| `ServerPlayerMixin`       | server.mixin                      | ServerPlayer Accessor Mixin：暴露 server 字段供网络广播使用                            |
| `IHopperBlockEntityMixin` | server.mixin                      | Accessor/Invoker Mixin：暴露原版内部字段和方法                                         |
| `ServerConfigHandler`     | server.net                        | 服务端配置处理器：校验权限并应用配置推送                                                       |
| `INetHelper`              | net                               | 网络辅助接口，定义平台无关的网络操作（registerPackets / registerPlayerJoinEvent / sendToPlayer） |
| `PlayerUtil`              | utils                             | 玩家权限检查工具类（hasOpPermission + hasPermissionToPushConfig）                      |
| `Services`                | platform                          | ServiceLoader 服务加载器，运行时加载平台实现                                              |
| `IPlatformHelper`         | platform                          | 平台抽象接口                                                                      |
| `EntryPointFabric`        | fabric (root)                     | Fabric 通用入口点，初始化配置 + 通过 INetHelper 初始化网络                                   |
| `FabricNetHelper`         | fabric (root)                     | Fabric 网络辅助：实现 INetHelper，Payload 注册、服务端接收器、配置同步推送                          |
| `FabricClientEntryPoint`  | fabric.client                     | Fabric 客户端入口点，注册 S2C 接收器与断连清理                                              |
| `FabricServerEntryPoint`  | fabric.server                     | Fabric 服务端入口点（预留占位）                                                        |
| `FabricPlatformHelper`    | fabric.platform                   | Fabric 平台实现                                                                |
| `ModMenuIntegration`      | fabric (root)                     | ModMenu 集成，提供配置界面入口                                                        |
| `EntryPointNeoForge`      | neoforge (root)                   | NeoForge 通用入口点，初始化配置 + 通过 INetHelper 初始化网络                                 |
| `NeoForgeNetHelper`       | neoforge (root)                    | NeoForge 网络辅助：实现 INetHelper，Payload 注册（playBidirectional）、C2S/S2C 处理器       |
| `NeoForgeClientEntryPoint` | neoforge.client                  | NeoForge 客户端入口点，通过 @EventBusSubscriber(Dist.CLIENT) 注册 IConfigScreenFactory |
| `NeoForgeServerEntryPoint` | neoforge.server                  | NeoForge 服务端入口点，通过 @EventBusSubscriber(Dist.DEDICATED_SERVER) 预留服务端初始化       |
| `NeoForgePlatformHelper`  | neoforge.platform                 | NeoForge 平台实现                                                              |