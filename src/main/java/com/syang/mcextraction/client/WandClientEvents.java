package com.syang.mcextraction.client;

import com.syang.mcextraction.MCExtraction;
import com.syang.mcextraction.net.SelectSpellPayload;
import com.syang.mcextraction.registry.ModDataComponents;
import com.syang.mcextraction.world.item.ModSpell;
import com.syang.mcextraction.world.item.WandItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;
import java.util.Optional;

/**
 * Client-side wand UX (§5.7.5): Shift+scroll cycles the active spell, and a small HUD label above
 * the hotbar shows the current spell + element. The per-spell cooldown reuses the vanilla item
 * cooldown sweep on the hotbar, so nothing extra is drawn for it.
 */
@EventBusSubscriber(modid = MCExtraction.MOD_ID, value = Dist.CLIENT)
public final class WandClientEvents {

    private WandClientEvents() {
    }

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !player.isShiftKeyDown()) {
            return;
        }
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof WandItem)) {
            return;
        }
        if (ModDataComponents.getBoundSpells(stack).size() < 2) {
            return;
        }
        double delta = event.getScrollDeltaY();
        if (delta == 0) {
            return;
        }
        // Scroll up = previous spell, scroll down = next.
        PacketDistributor.sendToServer(new SelectSpellPayload(delta > 0 ? -1 : 1));
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
        if (!(stack.getItem() instanceof WandItem)) {
            return;
        }
        List<ModSpell> bound = ModDataComponents.getBoundSpells(stack);
        Optional<ModSpell> active = ModDataComponents.getActiveSpell(stack);
        if (active.isEmpty()) {
            return;
        }

        ModSpell spell = active.get();
        String slots = bound.size() > 1 ? "  (" + (bound.indexOf(spell) + 1) + "/" + bound.size() + ")" : "";
        Component label = Component.literal(spell.element().glyph() + " " + spell.displayName())
                .withStyle(spell.element().color())
                .append(Component.literal(slots).withStyle(net.minecraft.ChatFormatting.GRAY));

        GuiGraphics graphics = event.getGuiGraphics();
        Font font = mc.font;
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();
        int x = (width - font.width(label)) / 2;
        int y = height - 59;
        graphics.drawString(font, label, x, y, 0xFFFFFF, true);
    }
}
