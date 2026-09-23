package com.syang.syworks.datagen;

import com.syang.syworks.SyWorks;
import com.syang.syworks.registry.ModBlocks;
import com.syang.syworks.world.level.block.ModMachine;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;

/**
 * Generates blockstates, block models and the client item definitions
 * ({@code assets/syworks/items/*.json}, required since 1.21.4) for each machine and the incinerator.
 *
 * <p>The machines are furnace-style: an orientable body (distinct front/side/top textures) that
 * faces the placer, with a glowing {@code _front_on} swapped in by {@code LIT}. Textures live at
 * assets/syworks/textures/block/&lt;name&gt;_{side,top,front,front_on}.png and are produced by
 * {@code tools/gen_block_textures.py}. Block items get a plain model pointing at the block model
 * automatically.
 */
public class ModModelProvider extends ModelProvider {

    public ModModelProvider(PackOutput output) {
        super(output, SyWorks.MOD_ID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        for (ModMachine machine : ModMachine.values()) {
            blockModels.createFurnace(ModBlocks.MACHINES.get(machine).get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        }
        // Same shape as the machines, so the same generator: FACING and a lit front.
        blockModels.createFurnace(ModBlocks.INCINERATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
    }
}
