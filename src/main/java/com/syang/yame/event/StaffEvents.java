package com.syang.yame.event;

import com.syang.yame.Yame;
import com.syang.yame.registry.ModDataComponents;
import com.syang.yame.world.item.ModSpell;
import com.syang.yame.world.item.ModStaff;
import com.syang.yame.world.item.SpellBookItem;
import com.syang.yame.world.item.StaffItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

import java.util.List;

/**
 * Binds a spellbook onto a staff at an anvil (§5.7.5): {@code staff + spellbook → staff(+spell)}.
 * The tome is consumed and the spell is appended to the staff's {@code bound_spells} component,
 * up to the staff's slot limit. Duplicates and over-slot attempts produce no output.
 */
@EventBusSubscriber(modid = Yame.MOD_ID)
public final class StaffEvents {

    private StaffEvents() {
    }

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        ItemStack left = event.getLeft();
        ItemStack right = event.getRight();

        if (!(left.getItem() instanceof StaffItem staffItem) || left.getCount() != 1) {
            return;
        }
        if (!(right.getItem() instanceof SpellBookItem book)) {
            return;
        }

        ModStaff staff = staffItem.staff();
        ModSpell spell = book.spell();
        List<ModSpell> bound = ModDataComponents.getBoundSpells(left);

        // Reject duplicates and over-slot binds — leave the output empty.
        if (bound.contains(spell) || bound.size() >= staff.slots()) {
            return;
        }

        ItemStack result = left.copy();
        if (!ModDataComponents.addBoundSpell(result, spell)) {
            return;
        }
        // Make the first spell bound the active one so the staff is immediately usable.
        if (bound.isEmpty()) {
            ModDataComponents.setActiveSpell(result, spell);
        }

        event.setOutput(result);
        event.setMaterialCost(1);
        event.setCost(spell.runeCount() + bound.size() + 1);
    }
}
