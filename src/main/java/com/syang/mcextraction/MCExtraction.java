package com.syang.mcextraction;

import com.syang.mcextraction.registry.ModArmorMaterials;
import com.syang.mcextraction.registry.ModBlockEntities;
import com.syang.mcextraction.registry.ModBlocks;
import com.syang.mcextraction.registry.ModCreativeTabs;
import com.syang.mcextraction.registry.ModDataComponents;
import com.syang.mcextraction.registry.ModItems;
import com.syang.mcextraction.registry.ModMenuTypes;
import com.syang.mcextraction.registry.ModRecipes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * MC Extraction — main mod entrypoint.
 *
 * <p>Wires up every {@code DeferredRegister} to the mod event bus. All content
 * (metals, alloys, tools, blocks) is declared in the {@code registry} package.
 */
@Mod(MCExtraction.MOD_ID)
public class MCExtraction {

    public static final String MOD_ID = "mcextraction";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public MCExtraction(IEventBus modBus, ModContainer container) {
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

        LOGGER.info("MC Extraction loaded.");
    }
}
