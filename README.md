# EasyHopper

EasyHopper adds item filtering capabilities directly to vanilla hoppers without introducing any new blocks. Because of
this, you can safely uninstall the mod at any time without affecting your save worlds.

# Reporting Issues

Due to work commitments, I am unable to spend extensive time testing. If you encounter any issues, please report them
at [https://github.com/DearXuan7392/EasyHopper/issues](https://www.google.com/search?q=https://github.com/DearXuan7392/EasyHopper/issues).
Please include your mod version, game version, steps to reproduce, and a description of the bug.

# Download

* Download from [Modrinth](https://modrinth.com/mod/easy-hopper) (Recommended)
* Download from [CurseForge](https://www.curseforge.com/minecraft/mc-mods/easyhopper) (Updates may be delayed)

# Usage

## Configuration

You can edit the configuration file located at `./config/easyhopper.yaml` in your game directory at any time; changes
will take effect after restarting the game.

* **Fabric:** You must install [Mod Menu](https://modrinth.com/mod/modmenu) to access the in-game configuration screen.
  Changes apply immediately.
* **NeoForge:** No extra mods are required. You can edit the settings directly in the NeoForge config menu, and changes
  will take effect immediately.

## New Features

### Transfer Speed

You can adjust the transfer cooldown and the item stack amount per transfer to speed up or slow down item flow.

### Detection Cooldown

Hoppers enter a cooldown after every attempt to pick up items, preventing excessive checks for nearby dropped items.
This significantly improves performance when using large numbers of hoppers, though it may cause issues with certain
redstone contraptions, such as super smelters.

### Hopper Filtering

The 5th slot of the hopper acts as the filter slot. Only items matching the item in this slot can enter or be extracted
from the hopper. If a player manually places an incorrect item inside, it will remain in the hopper, though other
hoppers underneath can still pull items from it.

### Performance Optimization (`<=1.20.4`)

Disables dropped item detection when a full block is placed directly above the hopper to improve performance.

*Note: As of Minecraft `1.20.5`, this optimization is built into vanilla, so this feature is no longer needed.*