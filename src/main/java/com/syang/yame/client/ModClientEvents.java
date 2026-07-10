package com.syang.yame.client;

import com.syang.yame.Yame;
import com.syang.yame.client.gui.screens.AlloyFurnaceScreen;
import com.syang.yame.client.gui.screens.ExtractionFurnaceScreen;
import com.syang.yame.client.gui.screens.RuneAltarScreen;
import com.syang.yame.client.renderer.RuneAltarRenderer;
import com.syang.yame.registry.ModBlockEntities;
import com.syang.yame.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only mod-bus events: binds menus to their screens. */
@EventBusSubscriber(modid = Yame.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModClientEvents {

    private ModClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.EXTRACTION_FURNACE.get(), ExtractionFurnaceScreen::new);
        event.register(ModMenuTypes.ALLOY_FURNACE.get(), AlloyFurnaceScreen::new);
        event.register(ModMenuTypes.RUNE_ALTAR.get(), RuneAltarScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.RUNE_ALTAR.get(), RuneAltarRenderer::new);
    }
}
