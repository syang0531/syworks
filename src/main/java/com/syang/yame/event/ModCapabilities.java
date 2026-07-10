package com.syang.yame.event;

import com.syang.yame.Yame;
import com.syang.yame.registry.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Exposes the Extraction Furnace's inventory as an item-handler capability so hoppers and other
 * automation can insert fuel/source and pull the extracted metal.
 */
@EventBusSubscriber(modid = Yame.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModCapabilities {

    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.EXTRACTION_FURNACE.get(),
                (blockEntity, side) -> blockEntity.getInventory());

        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.ALLOY_FURNACE.get(),
                (blockEntity, side) -> blockEntity.getInventory());
    }
}
