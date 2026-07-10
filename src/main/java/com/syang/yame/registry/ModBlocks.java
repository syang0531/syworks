package com.syang.yame.registry;

import com.syang.yame.Yame;
import com.syang.yame.world.level.block.AlloyFurnaceBlock;
import com.syang.yame.world.level.block.ExtractionFurnaceBlock;
import com.syang.yame.world.level.block.RuneAltarBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * The two functional blocks. Each also gets a BlockItem registered into {@link ModItems#ITEMS}.
 */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Yame.MOD_ID);

    public static final DeferredBlock<ExtractionFurnaceBlock> EXTRACTION_FURNACE = registerBlock("extraction_furnace",
            () -> new ExtractionFurnaceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<AlloyFurnaceBlock> ALLOY_FURNACE = registerBlock("alloy_furnace",
            () -> new AlloyFurnaceBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.METAL)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()));

    public static final DeferredBlock<RuneAltarBlock> RUNE_ALTAR = registerBlock("rune_altar",
            () -> new RuneAltarBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_BLUE)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops()
                    .noOcclusion()
                    .lightLevel(state -> state.getValue(RuneAltarBlock.LIT) ? 7 : 0)));

    private ModBlocks() {
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> supplier) {
        DeferredBlock<T> block = BLOCKS.register(name, supplier);
        ModItems.ITEMS.registerSimpleBlockItem(block);
        return block;
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
