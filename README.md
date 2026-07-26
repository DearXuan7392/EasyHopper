# EasyHopper

<p align="center">
  <img src="https://raw.githubusercontent.com/lipis/flag-icons/main/flags/4x3/us.svg" width="18" valign="middle"> <b>English</b> | 
  <a href="./README_CN.md"><img src="https://raw.githubusercontent.com/lipis/flag-icons/main/flags/4x3/cn.svg" width="18" valign="middle"> 简体中文</a> | 
  <a href="./README_TW.md"><img src="https://raw.githubusercontent.com/lipis/flag-icons/main/flags/4x3/tw.svg" width="18" valign="middle"> 繁體中文</a> | 
  <a href="./README_DE.md"><img src="https://raw.githubusercontent.com/lipis/flag-icons/main/flags/4x3/de.svg" width="18" valign="middle"> Deutsch</a>
</p>

---

EasyHopper adds item filtering capabilities to vanilla hoppers without introducing any new blocks. Because no custom
blocks are added, you can safely uninstall this mod at any time without affecting your save world.

![Screenshot](https://cdn.dearxuan.com/project/easyhopper/screen_us.png)

## Reporting Issues

Due to work commitments, thorough testing for every scenario can be difficult. If you encounter any issues or bugs,
please report them on [GitHub Issues](https://github.com/DearXuan7392/EasyHopper/issues).

When submitting an issue, please include:

- Mod version and Minecraft version.
- Steps to reproduce the issue and a description of what went wrong.

## Downloads

- Download from [Modrinth](https://modrinth.com/mod/easyhopper) (Recommended)
- Download from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/easyhopper) (Slower updates)

## Configuration & Feature Setup

### Configuration Access

- **Any Scenario:** You can directly edit `./config/easyhopper.yaml` in your game directory. Changes will take effect
  after re-entering the world.
- **Singleplayer:** You can adjust configurations via the in-game GUI (requires graphical UI dependencies; see below).
- **LAN Host:** The host can freely modify configurations in-game.
- **Dedicated Servers:** Players on a server (including non-host LAN players) can only modify configurations in-game if
  **Operator Modification** (`ALLOW_OP_MODIFY`) is enabled in the server config file AND the player has **Operator (OP)
  ** permissions.

### Graphical UI Dependencies

#### For Fabric

To enable the config GUI, you must install the following:

- [Mod Menu](https://modrinth.com/mod/modmenu): Adds a configuration button in the mod list.
- **Either** [Cloth Config API](https://modrinth.com/mod/cloth-config) *(Recommended)* **or
  ** [YACL](https://modrinth.com/mod/yacl): Provides the graphical interface. If both are installed, Cloth Config API
  will be used by default.

#### For NeoForge

To enable the config GUI, install **either**:

- [Cloth Config API](https://modrinth.com/mod/cloth-config) *(Recommended)* **or
  ** [YACL](https://modrinth.com/mod/yacl). If both are installed, Cloth Config API will be used by default.

## Features

### Transfer Speed Adjustment

Modify hopper transfer cooldowns and the number of items transferred per tick/batch to speed up or slow down item
movement.

### Search / Extract Cooldown

Adds a cooldown whenever a hopper attempts to search for or extract nearby dropped items. This significantly reduces lag
when large numbers of hoppers are placed. *Note: This may affect timing-sensitive redstone contraptions, such as
high-speed super smelters.*

### Hopper Item Filtering

Uses the 5th (last) slot of a hopper as a filter slot. Only items matching the item type in the 5th slot will be pulled
in or outputted. If an invalid item is manually placed into the hopper, it remains in place, but other hoppers below can
still pull valid items out.

### Performance Optimization (`<= 1.20.4`)

Disables dropped item entity checks when a hopper is positioned directly below a full solid block or container, reducing
server tick lag.

> **Note:** Starting from version `1.20.5`, Mojang natively integrated this optimization into vanilla Minecraft code,
> making this option no longer necessary for newer versions.