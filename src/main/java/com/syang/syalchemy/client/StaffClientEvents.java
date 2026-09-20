package com.syang.syalchemy.client;

import com.syang.syalchemy.SyAlchemy;
import com.syang.syalchemy.net.SelectSpellPayload;
import com.syang.syalchemy.registry.ModDataComponents;
import com.syang.syalchemy.world.item.ModSpell;
import com.syang.syalchemy.world.item.StaffItem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;
import java.util.Optional;

/**
 * Client-side staff UX (§5.7.5): Shift+scroll cycles the active spell, and a small HUD label above
 * the hotbar shows the current spell + element. The per-spell cooldown reuses the vanilla item
 * cooldown sweep on the hotbar, so nothing extra is drawn for it.
 */
@EventBusSubscriber(modid = SyAlchemy.MOD_ID, value = Dist.CLIENT)
public final class StaffClientEvents {

    /** Opaque white — GUI colours are ARGB since 1.21.2, so a bare 0xFFFFFF would be invisible. */
    private static final int WHITE = 0xFFFFFFFF;

    private StaffClientEvents() {
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !player.isShiftKeyDown()) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof StaffItem)) {
            return;
        }
        if (ModDataComponents.getCastableSpells(stack).size() < 2) {
            return;
        }
        double delta = event.getScrollDeltaY();
        if (delta == 0) {
            return;
        }
        // Scroll up = previous spell, scroll down = next.
        ClientPacketDistributor.sendToServer(new SelectSpellPayload(delta > 0 ? -1 : 1));
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof StaffItem)) {
            return;
        }
        List<ModSpell> castable = ModDataComponents.getCastableSpells(stack);
        Optional<ModSpell> active = ModDataComponents.getActiveSpell(stack);
        if (active.isEmpty()) {
            return;
        }

        ModSpell spell = active.get();
        String slots = castable.size() > 1 ? "  (" + (castable.indexOf(spell) + 1) + "/" + castable.size() + ")" : "";
        Component label = Component.literal(spell.element().glyph() + " " + spell.displayName())
                .withStyle(spell.element().color())
                .append(Component.literal(slots).withStyle(ChatFormatting.GRAY));

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        Font font = mc.font;
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int x = (width - font.width(label)) / 2;
        int y = height - 59;
        graphics.text(font, label, x, y, WHITE, true);
    }
}
