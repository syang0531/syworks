package com.syang.yame;

import com.syang.yame.registry.ModArmorMaterials;
import com.syang.yame.registry.ModBlockEntities;
import com.syang.yame.registry.ModBlocks;
import com.syang.yame.registry.ModCreativeTabs;
import com.syang.yame.registry.ModDataComponents;
import com.syang.yame.registry.ModItems;
import com.syang.yame.registry.ModMenuTypes;
import com.syang.yame.registry.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Yame — main mod entrypoint.
 *
 * <p>Wires up every {@code DeferredRegister} to the mod event bus. All content
 * (metals, alloys, tools, blocks) is declared in the {@code registry} package.
 */
@Mod(Yame.MOD_ID)
public class Yame {

    public static final String MOD_ID = "yame";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public Yame(IEventBus modBus, ModContainer container) {
        // Order matters only in that ModBlocks registers its BlockItems into ModItems.ITEMS,
        // so both registers must be attached before the RegisterEvent fires — which they are.
        ModArmorMaterials.register(modBus);
        ModItems.register(modBus);
        ModDataComponents.register(modBus);
        ModBlocks.register(modBus);
        ModBlockEntities.register(modBus);
        ModMenuTypes.register(modBus);
        ModRecipes.register(modBus);
        ModCreativeTabs.register(modBus);

        LOGGER.info("Yame loaded.");
    }
}
