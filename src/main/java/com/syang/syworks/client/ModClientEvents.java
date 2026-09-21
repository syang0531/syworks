package com.syang.syworks.client;

import com.syang.syworks.SyWorks;
import com.syang.syworks.client.gui.screens.MachineScreen;
import com.syang.syworks.registry.ModMenuTypes;
import com.syang.syworks.world.level.block.ModMachine;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only mod-bus events: binds every machine's menu to the shared screen. */
@EventBusSubscriber(modid = SyWorks.MOD_ID, value = Dist.CLIENT)
public final class ModClientEvents {

    private ModClientEvents() {
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        for (ModMachine machine : ModMachine.values()) {
            event.register(ModMenuTypes.MACHINES.get(machine).get(), MachineScreen::new);
        }
    }
}
