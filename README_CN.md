# EasyHopper (轻松漏斗)

EasyHopper 在原版漏斗的基础上提供分类功能, 而无需引入任何额外方块. 因此可以在任何时候卸载该 Mod, 而不会对存档造成任何影响.

![截图](https://cdn.dearxuan.com/project/easyhopper/screen_zh.png)

## 报告问题

由于工作原因, 难以花费大量时间进行测试, 如果出现任何问题,
请在 [https://github.com/DearXuan7392/EasyHopper/issues](https://github.com/DearXuan7392/EasyHopper/issues) 报告. 请附带
Mod 版本和游戏版本, 并解释你进行了哪些操作, 出现了什么问题.

## 下载

- 从 [Modrinth](https://modrinth.com/mod/easy-hopper) 下载 (推荐)
- 从 [CurseForge](https://www.curseforge.com/minecraft/mc-mods/easyhopper) 下载 (更新较慢)

## 启用功能

### 任何情况

在任何情况下, 你都可以在游戏目录下 ``./config/easyhopper.yaml`` 里直接编辑内容, 并在重启游戏后更新.

在服务器上运行时, 你只能通过修改文件的方式来修改配置, 并在服务器重启后更新. 客户端设置对服务器不生效.

### 对于 Fabric

想要显示图形界面, 你必须首先下载下列模组:

- [Mod Menu](https://modrinth.com/mod/modmenu), 用于管理模组, 并添加配置按钮.
- 从 [Cloth Config API](https://modrinth.com/mod/cloth-config) (推荐) 或 [YACL](https://modrinth.com/mod/yacl) 中任选一个下载,
  用于提供图形界面. 如果同时存在两个模组, 则优先使用 Cloth Config API.

### 对于 NeoForge

- 从 [Cloth Config API](https://modrinth.com/mod/cloth-config) (推荐) 或 [YACL](https://modrinth.com/mod/yacl) 中任选一个下载,
  用于提供图形界面. 如果同时存在两个模组, 则优先使用 Cloth Config API.

## 功能

### 传输速度

你可以修改漏斗的传输冷却, 以及每次传输的物品数量, 加快或减缓物品流动速度.

### 检测冷却

每次尝试吸取物品都, 都使自己陷入冷却, 从而避免频繁检测掉落物. 在大量漏斗的情况下, 能够有效提升性能, 但容易导致部分红石机器出错,
例如高速熔炉.

### 漏斗分类

将漏斗的第 5 格作为分类物品格, 只有相同类型的物品才能进入漏斗或被主动输出. 如果玩家强行放入错误的物品, 那么该物品会一直留在漏斗中.
但其他漏斗仍然可以从下方吸取物品.

### 性能优化 (``<=1.20.4``)

当漏斗上方为完整方块时, 禁用漏斗的凋落物检测, 以提升性能.

从 ``1.20.5`` 版本起, 官方已引入该优化, 无需使用此功能.

## 测试版 (v3.1-alpha)

正在开发并测试以下功能, 可能不稳定:

- 在客户端修改服务器配置. 需要在配置文件中启用该功能, 且玩家为管理员