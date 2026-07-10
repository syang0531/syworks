package com.syang.yame.datagen;

import com.syang.yame.Yame;
import com.syang.yame.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

/**
 * Generates blockstates + block models + block-item models for the two machines.
 * Both are furnace-style: an orientable body (distinct front/side/top textures) that
 * faces the placer, with a glowing "_on" front swapped in by the {@code LIT} state.
 * Textures live at assets/yame/textures/block/&lt;name&gt;_{side,top,front,front_on}.png.
 */
public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Yame.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        machine(ModBlocks.EXTRACTION_FURNACE.get(), "extraction_furnace");
        machine(ModBlocks.ALLOY_FURNACE.get(), "alloy_furnace");
        runeAltar();
    }

    /**
     * Rune Altar: enchanting-table-shaped (16x12x16) so the recolored side texture reads as it was
     * designed to (its lower 12px), instead of a full-cube dark band. The floating book is drawn by
     * the block-entity renderer, not the model. LIT does not change the model (glow is light + book).
     */
    private void runeAltar() {
        var model = models().withExistingParent("rune_altar", "block/block")
                .texture("particle", modLoc("block/rune_altar_bottom"))
                .texture("bottom", modLoc("block/rune_altar_bottom"))
                .texture("top", modLoc("block/rune_altar_top"))
                .texture("side", modLoc("block/rune_altar_side"))
                .element()
                    .from(0, 0, 0).to(16, 12, 16)
                    .face(net.minecraft.core.Direction.DOWN).uvs(0, 0, 16, 16).texture("#bottom").cullface(net.minecraft.core.Direction.DOWN).end()
                    .face(net.minecraft.core.Direction.UP).uvs(0, 0, 16, 16).texture("#top").end()
                    .face(net.minecraft.core.Direction.NORTH).uvs(0, 4, 16, 16).texture("#side").end()
                    .face(net.minecraft.core.Direction.SOUTH).uvs(0, 4, 16, 16).texture("#side").end()
                    .face(net.minecraft.core.Direction.WEST).uvs(0, 4, 16, 16).texture("#side").end()
                    .face(net.minecraft.core.Direction.EAST).uvs(0, 4, 16, 16).texture("#side").end()
                .end();
        getVariantBuilder(ModBlocks.RUNE_ALTAR.get())
                .forAllStates(state -> ConfiguredModel.builder().modelFile(model).build());
        simpleBlockItem(ModBlocks.RUNE_ALTAR.get(), model);
    }

    /** Orientable machine with a lit front variant, oriented by HORIZONTAL_FACING. */
    private void machine(Block block, String name) {
        ResourceLocation side = modLoc("block/" + name + "_side");
        ResourceLocation top = modLoc("block/" + name + "_top");
        ResourceLocation front = modLoc("block/" + name + "_front");
        ResourceLocation frontOn = modLoc("block/" + name + "_front_on");

        ModelFile off = models().orientable(name, side, front, top);
        ModelFile on = models().orientable(name + "_on", side, frontOn, top);

        getVariantBuilder(block).forAllStates(state -> ConfiguredModel.builder()
                .modelFile(state.getValue(BlockStateProperties.LIT) ? on : off)
                .rotationY((int) state.getValue(HorizontalDirectionalBlock.FACING).getOpposite().toYRot())
                .build());

        simpleBlockItem(block, off);
    }
}
