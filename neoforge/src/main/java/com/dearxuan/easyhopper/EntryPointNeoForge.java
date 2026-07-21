package com.dearxuan.easyhopper;


import com.dearxuan.easyhopper.gui.CommonConfigGUI;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(Constants.MOD_ID)
public class EntryPointNeoForge {

    public EntryPointNeoForge(IEventBus eventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(
                IConfigScreenFactory.class,
                (container, parentScreen) -> CommonConfigGUI.createScreen(parentScreen)
        );

        CommonClass.init();
    }
}
