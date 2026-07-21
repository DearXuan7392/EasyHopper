# EasyHopper

EasyHopper adds item filtering functionality to vanilla hoppers without introducing any new blocks. As a result, you can
safely uninstall this mod at any time without affecting your save world.

# Reporting Issues

Due to work commitments, I am unable to spend a lot of time on extensive testing. If you encounter any issues, please
report them at [https://github.com/DearXuan7392/EasyHopper/issues](https://github.com/DearXuan7392/EasyHopper/issues).

Please include the **mod version** and **Minecraft version**, along with a clear explanation of what you did and what
went wrong.

# Download

* Download from [Modrinth](https://modrinth.com/mod/easy-hopper) (Recommended)
* Download from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/easyhopper) (Slower updates)

# Usage

## Configuration

### General / Server Configuration

In all cases, you can directly edit the config file located at `./config/easyhopper.yaml` within your Minecraft
directory. Changes will take effect after restarting the game.

When running on a server, configuration can only be modified via this file, and server restart is required. Client-side
settings do not affect the server.

### For Fabric

To enable the in-game configuration UI, you need to install the following mods:

* [Mod Menu](https://modrinth.com/mod/modmenu): Used for managing mods and adding the config button.
* Either [Cloth Config API](https://modrinth.com/mod/cloth-config) (Recommended)
  or [YACL](https://modrinth.com/mod/yacl) to render the GUI. If both are installed, Cloth Config API will take
  priority.

### For NeoForge

* Either [Cloth Config API](https://modrinth.com/mod/cloth-config) (Recommended)
  or [YACL](https://modrinth.com/mod/yacl) to render the GUI. If both are installed, Cloth Config API will take
  priority.

## Features

### Transfer Speed

You can customize the hopper transfer cooldown and the amount of items transferred to speed up or slow down item flow.

### Pickup Cooldown

Triggers a cooldown every time a hopper attempts to pick up items, preventing excessive entity checks. This
significantly improves performance when using a large number of hoppers, though it may cause issues with certain
redstone contraptions (e.g., arrayed super-smelters).

### Hopper Filtering

The 5th slot of the hopper acts as a filter slot. Only matching items can enter the hopper or be actively pushed out. If
a player manually inserts an unmatched item, it will remain in the hopper. However, other hoppers underneath can still
pull items from it as usual.

### Performance Optimization (`<=1.20.4`)

Disables item pickup checks when a full block is placed directly above the hopper, improving performance.

*Note: Starting from version `1.20.5`, this optimization has been natively implemented by Minecraft, making this feature
unnecessary for newer versions.*