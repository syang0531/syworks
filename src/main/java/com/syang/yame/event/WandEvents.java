package com.syang.yame.event;

import com.syang.yame.Yame;
import com.syang.yame.registry.ModDataComponents;
import com.syang.yame.world.item.ModSpell;
import com.syang.yame.world.item.ModWand;
import com.syang.yame.world.item.SpellBookItem;
import com.syang.yame.world.item.WandItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

import java.util.List;

/**
 * Binds a spellbook onto a wand at an anvil (§5.7.5): {@code wand + spellbook → wand(+spell)}.
 * The tome is consumed and the spell is appended to the wand's {@code bound_spells} component,
 * up to the wand's slot limit. Duplicates and over-slot attempts produce no output.
 */
@EventBusSubscriber(modid = Yame.MOD_ID)
public final class WandEvents {

    private WandEvents() {
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (!(left.getItem() instanceof WandItem wandItem) || left.getCount() != 1) {
            return;
        }
        if (!(right.getItem() instanceof SpellBookItem book)) {
            return;
        }

        ModWand wand = wandItem.wand();
        ModSpell spell = book.spell();
        List<ModSpell> bound = ModDataComponents.getBoundSpells(left);

        // Reject duplicates and over-slot binds — leave the output empty.
        if (bound.contains(spell) || bound.size() >= wand.slots()) {
            return;
        }

        ItemStack result = left.copy();
        if (!ModDataComponents.addBoundSpell(result, spell)) {
            return;
        }
        // Make the first spell bound the active one so the wand is immediately usable.
        if (bound.isEmpty()) {
            ModDataComponents.setActiveSpell(result, spell);
        }

        event.setOutput(result);
        event.setMaterialCost(1);
        event.setCost(spell.runeCount() + bound.size() + 1);
    }
}
