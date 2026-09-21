package com.syang.syworks;

import com.syang.syworks.registry.ModBlockEntities;
import com.syang.syworks.registry.ModBlocks;
import com.syang.syworks.registry.ModCreativeTabs;
import com.syang.syworks.registry.ModItems;
import com.syang.syworks.registry.ModMenuTypes;
import com.syang.syworks.registry.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SY Works — main mod entrypoint.
 *
 * <p>Wires up every {@code DeferredRegister} to the mod event bus. All content
 * (the machines) is declared in the {@code registry} package.
 *
 * <p>Targets Minecraft 26.2 / NeoForge 26.2 (Java 25).
 */
@Mod(SyWorks.MOD_ID)
public class SyWorks {

    public static final String MOD_ID = "syworks";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public SyWorks(IEventBus modBus, ModContainer container) {
        // Order matters only in that ModBlocks registers its BlockItems into ModItems.ITEMS,
        // so both registers must be attached before the RegisterEvent fires — which they are.
        ModItems.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenuTypes.register(modBus);
        ModRecipes.register(modBus);
        ModCreativeTabs.register(modBus);

        LOGGER.info("SY Works loaded.");
    }
}
