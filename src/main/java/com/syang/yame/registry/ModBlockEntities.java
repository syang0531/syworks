package com.syang.yame.registry;

import com.syang.yame.Yame;
import com.syang.yame.world.level.block.entity.AlloyFurnaceBlockEntity;
import com.syang.yame.world.level.block.entity.ExtractionFurnaceBlockEntity;
import com.syang.yame.world.level.block.entity.RuneAltarBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Yame.MOD_ID);

    public static final Supplier<BlockEntityType<ExtractionFurnaceBlockEntity>> EXTRACTION_FURNACE =
            BLOCK_ENTITIES.register("extraction_furnace", () -> new BlockEntityType<>(
                    ExtractionFurnaceBlockEntity::new, ModBlocks.EXTRACTION_FURNACE.get()));

    public static final Supplier<BlockEntityType<AlloyFurnaceBlockEntity>> ALLOY_FURNACE =
            BLOCK_ENTITIES.register("alloy_furnace", () -> new BlockEntityType<>(
                    AlloyFurnaceBlockEntity::new, ModBlocks.ALLOY_FURNACE.get()));

    public static final Supplier<BlockEntityType<RuneAltarBlockEntity>> RUNE_ALTAR =
            BLOCK_ENTITIES.register("rune_altar", () -> new BlockEntityType<>(
                    RuneAltarBlockEntity::new, ModBlocks.RUNE_ALTAR.get()));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
