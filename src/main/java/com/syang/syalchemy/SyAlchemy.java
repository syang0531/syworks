package com.syang.syalchemy;

import com.syang.syalchemy.registry.ModBlockEntities;
import com.syang.syalchemy.registry.ModBlocks;
import com.syang.syalchemy.registry.ModCreativeTabs;
import com.syang.syalchemy.registry.ModDataComponents;
import com.syang.syalchemy.registry.ModItems;
import com.syang.syalchemy.registry.ModMenuTypes;
import com.syang.syalchemy.registry.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SY Alchemy (formerly Yame) — main mod entrypoint.
 *
 * <p>Wires up every {@code DeferredRegister} to the mod event bus. All content
 * (metals, alloys, tools, blocks) is declared in the {@code registry} package.
 *
 * <p>Targets Minecraft 26.2 / NeoForge 26.2 (Java 25), the same stack as the sibling
 * {@code placitum} mod so the two can be played together.
 */
@Mod(SyAlchemy.MOD_ID)
public class SyAlchemy {

    public static final String MOD_ID = "syalchemy";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public SyAlchemy(IEventBus modBus, ModContainer container) {
        // Order matters only in that ModBlocks registers its BlockItems into ModItems.ITEMS,
        // so both registers must be attached before the RegisterEvent fires — which they are.
        // Armor materials are plain records since 1.21.2 (no registry), see ModArmorMaterials.
        ModItems.register(modBus);
        ModDataComponents.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenuTypes.register(modBus);
        ModRecipes.register(modBus);
        ModCreativeTabs.register(modBus);

        LOGGER.info("SY Alchemy loaded.");
    }
}
