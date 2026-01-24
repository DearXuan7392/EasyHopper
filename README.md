# EasyHopper

EasyHopper provides classification functionality based on the vanilla version, without introducing any additional
blocks.

![screenshot](res/screenshot_EN.png)

## Dependencies

`modmenu` (client-side only)

**<font color=red> Warning </font>**:
The configuration UI will only be displayed after installing `modmenu`. If EasyHopper is running on a server or
`modmenu` is not installed, you can only manually modify the configuration file in the `config` folder.

## Download

[Download from Modrinth](https://modrinth.com/mod/easy-hopper) (recommended)

[Download from CurseForge](https://www.curseforge.com/minecraft/mc-mods/easyhopper)

## Features

### Cooldown

After an input or output operation, hoppers enter a cooldown period, and the original cooldown time is 8 ticks (20 ticks
per second). This MOD provides the ability to modify the cooldown, you can adjust the cooldown as needed to increase
transfer speed.

### Input/Output Quantity

Modifying the number of items a hopper can input or output at once allows it to transfer more items in a single
operation. Note that dropped items are only affected by cooldown.

### Classification

After enabling the classification feature in settings, the last slot of hoppers will be used as the classification slot.
Only identical items (including non-stackable items, or tools with different damage) can be input or output.

If a player forcibly places different items, they will remain stuck in the hopper and cannot flow out until manually
removed by the player.

When the last slot of the hopper is empty, the hopper no longer classifies items. Even when empty, items cannot flow
into the last slot; it can only be filled manually by the player.

For example, if you place redstone in the last slot, only redstone can enter or leave this hopper. The moment you remove
the redstone, the hopper's classification function becomes inactive.

### Item Extract Cooldown

In vanilla Minecraft, hoppers check for dropped items every tick. This MOD adds a cooldown period to this process,
preventing frequent checks. It can significantly improve performance when many hoppers are present.

## Others

### Performance Enhancement (`<= 1.20.4`)

> In versions up to `1.20.4`, when a full block is above a hopper, there is almost no chance for dropped items to be
> input, yet hoppers still check items every tick. This MOD optimizes the code so that when a full block is above the
> hopper, it will not check for dropped items. This feature may cause extremely rare cases where dropped items embedded in
> full blocks cannot be sucked up by hoppers, such as honey dropping from beehives.

The official version has optimized the code in `1.20.5`, and the related functionality has been removed from this MOD.

### Conflicts with Other MODs

Since this MOD modifies hopper behavior, it may conflict with other MODs that also modify hopper behavior.

### For Developers

Due to my busy schedule with studies and work, I may pay little attention to issues or not update for a long time.
Fortunately, this project requires only minimal modifications to adapt to new Minecraft versions. If you wish to
continue maintaining this MOD, you can fork this project and follow these steps:

1. Edit the game version numbers in [gradle.properties](gradle.properties), get it
   from [https://fabricmc.net/develop](https://fabricmc.net/develop), for example:

```properties
minecraft_version=1.21.11
yarn_mappings=1.21.11+build.4
loader_version=0.18.4
loom_version=1.15-SNAPSHOT
# Fabric API
fabric_api_version=0.141.2+1.21.11
```

2. Edit the `cloth config` and `modmenu` dependencies in [build.gradle](build.gradle), get it
   from [https://linkie.shedaniel.dev/dependencies?loader=fabric&version=1.21.11](https://linkie.shedaniel.dev/dependencies?loader=fabric&version=1.21.11),
   for example:

```properties
cloth_config_version=21.11.150
mod_menu_version=17.0.0-beta.1
```

3. Update the MOD version number in [build.gradle](build.gradle). The first number represents feature updates, the
   second number represents mod version updates, and the third number represents patch/minor updates. For bug fixes
   only, increment the third number.

```properties
mod_version=2.15.1
```

4. Replace the `resources/META-INF/jars/cloth-config-<cloth_config_version>-fabric.jar` file with the version
   corresponding to your updated `cloth_config_version`.

5. Build and test.