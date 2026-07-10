package com.syang.yame.net;

import com.syang.yame.Yame;
import com.syang.yame.registry.ModDataComponents;
import com.syang.yame.world.item.ModSpell;
import com.syang.yame.world.item.StaffItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.List;

/**
 * Registers the mod's network payloads and their server-side handlers.
 * Currently only {@link SelectSpellPayload} (Shift+scroll spell selection).
 */
@EventBusSubscriber(modid = Yame.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModPayloads {

    private ModPayloads() {
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(SelectSpellPayload.TYPE, SelectSpellPayload.STREAM_CODEC, ModPayloads::onSelectSpell);
    }

    private static void onSelectSpell(SelectSpellPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack stack = player.getMainHandItem();
            if (!(stack.getItem() instanceof StaffItem)) {
                return;
            }
            List<ModSpell> bound = ModDataComponents.getBoundSpells(stack);
            if (bound.size() < 2) {
                return;
            }
            ModSpell active = ModDataComponents.getActiveSpell(stack).orElse(bound.get(0));
            int index = bound.indexOf(active);
            int next = Math.floorMod(index + payload.direction(), bound.size());
            ModDataComponents.setActiveSpell(stack, bound.get(next));
        });
    }
}
