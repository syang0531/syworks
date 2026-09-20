package com.syang.yame.event;

import com.syang.yame.Yame;
import com.syang.yame.registry.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Exposes the machines' inventories as the item {@code ResourceHandler} capability so hoppers and
 * other automation can insert fuel/source and pull the extracted metal / alloy.
 */
@EventBusSubscriber(modid = Yame.MOD_ID)
public final class ModCapabilities {

    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.EXTRACTION_FURNACE.get(),
                (blockEntity, side) -> blockEntity.getInventory());

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.ALLOY_FURNACE.get(),
                (blockEntity, side) -> blockEntity.getInventory());
    }
}
