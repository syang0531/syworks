package com.syang.syworks.registry;

import com.syang.syworks.SyWorks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * The item register. The mod deliberately adds <b>no items of its own</b> — every machine turns a
 * vanilla item into another vanilla item — so this holds only the BlockItems that
 * {@link ModBlocks} registers into it.
 */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SyWorks.MOD_ID);

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
