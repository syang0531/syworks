package com.syang.syalchemy.net;

import com.syang.syalchemy.SyAlchemy;
import com.syang.syalchemy.registry.ModDataComponents;
import com.syang.syalchemy.world.item.ModSpell;
import com.syang.syalchemy.world.item.StaffItem;
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
@EventBusSubscriber(modid = SyAlchemy.MOD_ID)
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
            List<ModSpell> castable = ModDataComponents.getCastableSpells(stack);
            if (castable.size() < 2) {
                return;
            }
            ModSpell active = ModDataComponents.getActiveSpell(stack).orElse(castable.get(0));
            int index = castable.indexOf(active);
            int next = Math.floorMod(index + payload.direction(), castable.size());
            ModDataComponents.setActiveSpell(stack, castable.get(next));
        });
    }
}
