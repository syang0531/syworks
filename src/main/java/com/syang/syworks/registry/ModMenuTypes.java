package com.syang.syworks.registry;

import com.syang.syworks.SyWorks;
import com.syang.syworks.world.inventory.MachineMenu;
import com.syang.syworks.world.level.block.ModMachine;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ModMenuTypes {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SyWorks.MOD_ID);

    public static final Map<ModMachine, Supplier<MenuType<MachineMenu>>> MACHINES =
            new EnumMap<>(ModMachine.class);

    static {
        for (ModMachine machine : ModMachine.values()) {
            MACHINES.put(machine, MENUS.register(machine.id(), () -> IMenuTypeExtension.create(
                    (id, inventory, buf) -> new MachineMenu(machine, id, inventory, buf))));
        }
    }

    private ModMenuTypes() {
    }

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
