package com.syang.syworks.event;

import com.syang.syworks.SyWorks;
import com.syang.syworks.registry.ModBlockEntities;
import com.syang.syworks.world.level.block.ModMachine;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Exposes each machine's inventory as the item {@code ResourceHandler} capability so hoppers and
 * other automation can insert fuel and input and pull the result.
 *
 * <p>Note that a hopper draining the output does <b>not</b> collect the machine's experience —
 * that is paid out only when a player takes from the output slot, as with a furnace.
 */
@EventBusSubscriber(modid = SyWorks.MOD_ID)
public final class ModCapabilities {

    private ModCapabilities() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (ModMachine machine : ModMachine.values()) {
            event.registerBlockEntity(
                    Capabilities.Item.BLOCK,
                    ModBlockEntities.MACHINES.get(machine).get(),
                    (blockEntity, side) -> blockEntity.getInventory());
        }
    }
}
