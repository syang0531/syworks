package com.syang.syworks.registry;

import com.syang.syworks.SyWorks;
import com.syang.syworks.world.level.block.ExtractionFurnaceBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The machines. Each also gets a BlockItem registered into {@link ModItems#ITEMS}.
 *
 * <p>Since 1.21.2 every block must carry its registry id in its properties ({@code setId});
 * {@code DeferredRegister.Blocks#registerBlock} does that for us when given a properties supplier.
 */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SyWorks.MOD_ID);

    public static final DeferredBlock<ExtractionFurnaceBlock> EXTRACTION_FURNACE = registerBlock("extraction_furnace",
            ExtractionFurnaceBlock::new,
            () -> BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE)
                    .strength(3.5F)
                    .requiresCorrectToolForDrops());

    private ModBlocks() {
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name,
            Function<BlockBehaviour.Properties, T> constructor, Supplier<BlockBehaviour.Properties> properties) {
        DeferredBlock<T> block = BLOCKS.registerBlock(name, constructor, properties);
        ModItems.ITEMS.registerSimpleBlockItem(block);
        return block;
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
