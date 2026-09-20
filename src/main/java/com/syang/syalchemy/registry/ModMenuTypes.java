package com.syang.syalchemy.registry;

import com.syang.syalchemy.SyAlchemy;
import com.syang.syalchemy.world.inventory.AlloyFurnaceMenu;
import com.syang.syalchemy.world.inventory.ExtractionFurnaceMenu;
import com.syang.syalchemy.world.inventory.RuneAltarMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SyAlchemy.MOD_ID);

    public static final Supplier<MenuType<ExtractionFurnaceMenu>> EXTRACTION_FURNACE =
            MENUS.register("extraction_furnace", () -> IMenuTypeExtension.create(ExtractionFurnaceMenu::new));

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
