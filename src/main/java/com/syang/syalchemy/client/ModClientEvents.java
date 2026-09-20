package com.syang.syalchemy.client;

import com.syang.syalchemy.SyAlchemy;
import com.syang.syalchemy.client.gui.screens.AlloyFurnaceScreen;
import com.syang.syalchemy.client.gui.screens.ExtractionFurnaceScreen;
import com.syang.syalchemy.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only mod-bus events: binds menus to their screens. */
@EventBusSubscriber(modid = SyAlchemy.MOD_ID, value = Dist.CLIENT)
public final class ModClientEvents {

    private ModClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.EXTRACTION_FURNACE.get(), ExtractionFurnaceScreen::new);
        event.register(ModMenuTypes.ALLOY_FURNACE.get(), AlloyFurnaceScreen::new);
    }
}
