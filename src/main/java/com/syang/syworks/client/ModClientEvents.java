package com.syang.syworks.client;

import com.syang.syworks.SyWorks;
import com.syang.syworks.client.gui.screens.ExtractionFurnaceScreen;
import com.syang.syworks.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only mod-bus events: binds menus to their screens. */
@EventBusSubscriber(modid = SyWorks.MOD_ID, value = Dist.CLIENT)
public final class ModClientEvents {

    private ModClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.EXTRACTION_FURNACE.get(), ExtractionFurnaceScreen::new);
    }
}
