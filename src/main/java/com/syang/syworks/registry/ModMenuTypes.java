package com.syang.syworks.registry;

import com.syang.syworks.SyWorks;
import com.syang.syworks.world.inventory.ExtractionFurnaceMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SyWorks.MOD_ID);

    public static final Supplier<MenuType<ExtractionFurnaceMenu>> EXTRACTION_FURNACE =
            MENUS.register("extraction_furnace", () -> IMenuTypeExtension.create(ExtractionFurnaceMenu::new));

    private ModMenuTypes() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
