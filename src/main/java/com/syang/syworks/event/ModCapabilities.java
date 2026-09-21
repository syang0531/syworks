package com.syang.syworks.event;

import com.syang.syworks.SyWorks;
import com.syang.syworks.registry.ModBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Exposes each machine's inventory as the item {@code ResourceHandler} capability so hoppers and
 * other automation can insert fuel and input and pull the result.
 */
@EventBusSubscriber(modid = SyWorks.MOD_ID)
public final class ModCapabilities {

    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.EXTRACTION_FURNACE.get(),
                (blockEntity, side) -> blockEntity.getInventory());
    }
}
