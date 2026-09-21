package com.syang.syworks.registry;

import com.syang.syworks.SyWorks;
import com.syang.syworks.world.level.block.ModMachine;
import com.syang.syworks.world.level.block.entity.MachineBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SyWorks.MOD_ID);

    public static final Map<ModMachine, Supplier<BlockEntityType<MachineBlockEntity>>> MACHINES =
            new EnumMap<>(ModMachine.class);

    static {
        for (ModMachine machine : ModMachine.values()) {
            MACHINES.put(machine, BLOCK_ENTITIES.register(machine.id(), () -> new BlockEntityType<>(
                    (pos, state) -> new MachineBlockEntity(machine, pos, state),
                    ModBlocks.MACHINES.get(machine).get())));
        }
    }

    private ModBlockEntities() {
    }

    public static void register(IEventBus modBus) {
        BLOCK_ENTITIES.register(modBus);
    }
}
