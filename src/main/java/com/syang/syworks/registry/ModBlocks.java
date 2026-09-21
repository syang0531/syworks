package com.syang.syworks.registry;

import com.syang.syworks.SyWorks;
import com.syang.syworks.world.level.block.MachineBlock;
import com.syang.syworks.world.level.block.ModMachine;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

/**
 * One block per {@link ModMachine}, plus a BlockItem for each registered into {@link ModItems#ITEMS}.
 *
 * <p>Since 1.21.2 every block must carry its registry id in its properties ({@code setId});
 * {@code DeferredRegister.Blocks#registerBlock} does that for us when given a properties supplier.
 */
public final class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SyWorks.MOD_ID);

    public static final Map<ModMachine, DeferredBlock<MachineBlock>> MACHINES = new EnumMap<>(ModMachine.class);

    static {
        for (ModMachine machine : ModMachine.values()) {
            DeferredBlock<MachineBlock> block = BLOCKS.registerBlock(machine.id(),
                    props -> new MachineBlock(machine, props),
                    () -> BlockBehaviour.Properties.of()
                            .mapColor(MapColor.STONE)
                            .strength(3.5F)
                            .requiresCorrectToolForDrops());
            ModItems.ITEMS.registerSimpleBlockItem(block);
            MACHINES.put(machine, block);
        }
    }

    private ModBlocks() {
    }

    public static void register(IEventBus modBus) {
        BLOCKS.register(modBus);
    }
}
