package com.syang.syalchemy.client;

import com.syang.syalchemy.SyAlchemy;
import com.syang.syalchemy.client.gui.screens.AlloyFurnaceScreen;
import com.syang.syalchemy.client.gui.screens.ExtractionFurnaceScreen;
import com.syang.syalchemy.client.gui.screens.RuneAltarScreen;
import com.syang.syalchemy.client.renderer.RuneAltarRenderer;
import com.syang.syalchemy.registry.ModBlockEntities;
import com.syang.syalchemy.registry.ModMenuTypes;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only mod-bus events: binds menus to their screens and the altar to its renderer. */
@EventBusSubscriber(modid = SyAlchemy.MOD_ID, value = Dist.CLIENT)
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
