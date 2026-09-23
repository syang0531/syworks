package com.syang.syworks.registry;

import com.syang.syworks.SyWorks;
import com.syang.syworks.world.level.block.ModMachine;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** A single tab holding the machines and the incinerator. The mod has no items of its own. */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SyWorks.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.syworks.main"))
                    .icon(() -> new ItemStack(ModBlocks.MACHINES.get(ModMachine.CRUSHER).get()))
                    .displayItems((params, output) -> {
                        for (ModMachine machine : ModMachine.values()) {
                            output.accept(ModBlocks.MACHINES.get(machine).get());
                        }
                        output.accept(ModBlocks.INCINERATOR.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
