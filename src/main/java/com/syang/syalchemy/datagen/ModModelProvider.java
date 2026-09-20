package com.syang.syalchemy.datagen;

import com.syang.syalchemy.SyAlchemy;
import com.syang.syalchemy.registry.ModBlocks;
import com.syang.syalchemy.registry.ModItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Generates blockstates, block models, item models and the client item definitions
 * ({@code assets/syalchemy/items/*.json}, required since 1.21.4) for every block and item.
 *
 * <ul>
 *   <li>The two machines are furnace-style: an orientable body (distinct front/side/top
 *       textures) that faces the placer, with a glowing {@code _front_on} swapped in by {@code LIT}.
 *       Textures: assets/syalchemy/textures/block/&lt;name&gt;_{side,top,front,front_on}.png.</li>
 *   <li>Ingots and armor use {@code item/generated}; tools use {@code item/handheld}.
 *       Block items get a plain model pointing at the block model automatically.</li>
 * </ul>
 */
public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, SyAlchemy.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createFurnace(ModBlocks.EXTRACTION_FURNACE.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.ALLOY_FURNACE.get(), TexturedModel.ORIENTABLE_ONLY_TOP);

        ModItems.METAL_INGOTS.values().forEach(i -> flat(itemModels, i, ModelTemplates.FLAT_ITEM));
        ModItems.ALLOY_INGOTS.values().forEach(i -> flat(itemModels, i, ModelTemplates.FLAT_ITEM));

        ModItems.ALLOY_SWORDS.values().forEach(i -> flat(itemModels, i, ModelTemplates.FLAT_HANDHELD_ITEM));
        ModItems.ALLOY_PICKAXES.values().forEach(i -> flat(itemModels, i, ModelTemplates.FLAT_HANDHELD_ITEM));
        ModItems.ALLOY_AXES.values().forEach(i -> flat(itemModels, i, ModelTemplates.FLAT_HANDHELD_ITEM));
        ModItems.ALLOY_SHOVELS.values().forEach(i -> flat(itemModels, i, ModelTemplates.FLAT_HANDHELD_ITEM));
        ModItems.ALLOY_HOES.values().forEach(i -> flat(itemModels, i, ModelTemplates.FLAT_HANDHELD_ITEM));

        ModItems.ALLOY_HELMETS.values().forEach(i -> flat(itemModels, i, ModelTemplates.FLAT_ITEM));
        ModItems.ALLOY_CHESTPLATES.values().forEach(i -> flat(itemModels, i, ModelTemplates.FLAT_ITEM));
        ModItems.ALLOY_LEGGINGS.values().forEach(i -> flat(itemModels, i, ModelTemplates.FLAT_ITEM));
        ModItems.ALLOY_BOOTS.values().forEach(i -> flat(itemModels, i, ModelTemplates.FLAT_ITEM));
    }

    private static void flat(ItemModelGenerators itemModels, DeferredItem<?> item, ModelTemplate template) {
        itemModels.generateFlatItem(item.get(), template);
    }
}
