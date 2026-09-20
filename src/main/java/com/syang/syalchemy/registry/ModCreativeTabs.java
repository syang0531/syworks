package com.syang.syalchemy.registry;

import com.syang.syalchemy.SyAlchemy;
import com.syang.syalchemy.world.item.ModAlloy;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * A single creative tab holding every item in the mod, in a readable order:
 * metals → alloy ingots → swords → pickaxes → blocks.
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SyAlchemy.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.syalchemy.main"))
                    .icon(() -> new ItemStack(ModItems.ALLOY_INGOTS.get(ModAlloy.STEEL).get()))
                    .displayItems((params, output) -> {
                        ModItems.METAL_INGOTS.values().forEach(i -> output.accept(i.get()));
                        ModItems.ALLOY_INGOTS.values().forEach(i -> output.accept(i.get()));
                        ModItems.ALLOY_SWORDS.values().forEach(i -> output.accept(i.get()));
                        ModItems.ALLOY_PICKAXES.values().forEach(i -> output.accept(i.get()));
                        ModItems.ALLOY_AXES.values().forEach(i -> output.accept(i.get()));
                        ModItems.ALLOY_SHOVELS.values().forEach(i -> output.accept(i.get()));
                        ModItems.ALLOY_HOES.values().forEach(i -> output.accept(i.get()));
                        ModItems.ALLOY_HELMETS.values().forEach(i -> output.accept(i.get()));
                        ModItems.ALLOY_CHESTPLATES.values().forEach(i -> output.accept(i.get()));
                        ModItems.ALLOY_LEGGINGS.values().forEach(i -> output.accept(i.get()));
                        ModItems.ALLOY_BOOTS.values().forEach(i -> output.accept(i.get()));
                        output.accept(ModBlocks.EXTRACTION_FURNACE.get());
                        output.accept(ModBlocks.ALLOY_FURNACE.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modBus) {
        TABS.register(modBus);
    }
}
