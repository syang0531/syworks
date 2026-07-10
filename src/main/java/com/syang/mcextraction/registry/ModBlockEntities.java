package com.syang.mcextraction.registry;

import com.syang.mcextraction.MCExtraction;
import com.syang.mcextraction.world.level.block.entity.AlloyFurnaceBlockEntity;
import com.syang.mcextraction.world.level.block.entity.ExtractorBlockEntity;
import com.syang.mcextraction.world.level.block.entity.RuneAltarBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MCExtraction.MOD_ID);

    public static final Supplier<BlockEntityType<ExtractorBlockEntity>> EXTRACTOR =
            BLOCK_ENTITIES.register("extractor", () -> BlockEntityType.Builder
                    .of(ExtractorBlockEntity::new, ModBlocks.EXTRACTOR.get())
                    .build(null));

    public static final Supplier<BlockEntityType<AlloyFurnaceBlockEntity>> ALLOY_FURNACE =
            BLOCK_ENTITIES.register("alloy_furnace", () -> BlockEntityType.Builder
                    .of(AlloyFurnaceBlockEntity::new, ModBlocks.ALLOY_FURNACE.get())
                    .build(null));

    public static final Supplier<BlockEntityType<RuneAltarBlockEntity>> RUNE_ALTAR =
            BLOCK_ENTITIES.register("rune_altar", () -> BlockEntityType.Builder
                    .of(RuneAltarBlockEntity::new, ModBlocks.RUNE_ALTAR.get())
                    .build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
