package com.syang.mcextraction.client;

import com.syang.mcextraction.MCExtraction;
import com.syang.mcextraction.client.gui.screens.AlloyFurnaceScreen;
import com.syang.mcextraction.client.gui.screens.ExtractorScreen;
import com.syang.mcextraction.client.gui.screens.RuneAltarScreen;
import com.syang.mcextraction.client.renderer.RuneAltarRenderer;
import com.syang.mcextraction.registry.ModBlockEntities;
import com.syang.mcextraction.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only mod-bus events: binds menus to their screens. */
@EventBusSubscriber(modid = MCExtraction.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModClientEvents {

    private ModClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.EXTRACTOR.get(), ExtractorScreen::new);
        event.register(ModMenuTypes.ALLOY_FURNACE.get(), AlloyFurnaceScreen::new);
        event.register(ModMenuTypes.RUNE_ALTAR.get(), RuneAltarScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.RUNE_ALTAR.get(), RuneAltarRenderer::new);
    }
}
