package com.syang.mcextraction.registry;

import com.syang.mcextraction.MCExtraction;
import com.syang.mcextraction.world.inventory.AlloyFurnaceMenu;
import com.syang.mcextraction.world.inventory.ExtractorMenu;
import com.syang.mcextraction.world.inventory.RuneAltarMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MCExtraction.MOD_ID);

    public static final Supplier<MenuType<ExtractorMenu>> EXTRACTOR =
            MENUS.register("extractor", () -> IMenuTypeExtension.create(ExtractorMenu::new));

    public static final Supplier<MenuType<AlloyFurnaceMenu>> ALLOY_FURNACE =
            MENUS.register("alloy_furnace", () -> IMenuTypeExtension.create(AlloyFurnaceMenu::new));

    public static final Supplier<MenuType<RuneAltarMenu>> RUNE_ALTAR =
            MENUS.register("rune_altar", () -> IMenuTypeExtension.create(RuneAltarMenu::new));

    private ModMenuTypes() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
