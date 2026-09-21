package com.syang.syworks.registry;

import com.syang.syworks.SyWorks;
import com.syang.syworks.world.level.block.entity.ExtractionFurnaceBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SyWorks.MOD_ID);

    public static final Supplier<BlockEntityType<ExtractionFurnaceBlockEntity>> EXTRACTION_FURNACE =
            BLOCK_ENTITIES.register("extraction_furnace", () -> new BlockEntityType<>(
                    ExtractionFurnaceBlockEntity::new, ModBlocks.EXTRACTION_FURNACE.get()));

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
