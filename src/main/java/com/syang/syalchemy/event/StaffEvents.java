package com.syang.syalchemy.event;

import com.syang.syalchemy.SyAlchemy;
import com.syang.syalchemy.registry.ModDataComponents;
import com.syang.syalchemy.world.item.ModSpell;
import com.syang.syalchemy.world.item.ModStaff;
import com.syang.syalchemy.world.item.SpellBookItem;
import com.syang.syalchemy.world.item.StaffItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;
import net.neoforged.neoforge.event.GrindstoneEvent;

import java.util.List;

/**
 * Staff anvil/grindstone events (§5.7.5):
 * <ul>
 *   <li><b>Anvil bind</b>: {@code staff + spellbook → staff(+spell)} — the tome is consumed and the
 *       spell appended to {@code bound_spells}, up to the staff's slot limit. Duplicates, over-slot
 *       binds, and the innate Firebolt produce no output.</li>
 *   <li><b>Grindstone reset</b>: a lone staff is stripped of its bound spells <i>and</i> enchantments,
 *       back to a bare staff (which still casts the innate Firebolt).</li>
 * </ul>
 */
@EventBusSubscriber(modid = SyAlchemy.MOD_ID)
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

        // Firebolt is innate (free) — it neither needs binding nor should it consume a slot.
        if (spell == ModSpell.FIREBOLT) {
            return;
        }

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
        event.setXpCost(spell.runeCount() + bound.size() + 1);
    }

    /** A lone staff in a grindstone: wipe its bound spells + enchantments back to a bare staff. */
    @SubscribeEvent
    public static void onGrindstoneReset(GrindstoneEvent.OnPlaceItem event) {
        ItemStack top = event.getTopItem();
        ItemStack bottom = event.getBottomItem();
        ItemStack staff;
        if (top.getItem() instanceof StaffItem && bottom.isEmpty()) {
            staff = top;
        } else if (bottom.getItem() instanceof StaffItem && top.isEmpty()) {
            staff = bottom;
        } else {
            return;
        }

        boolean hasSpells = staff.has(ModDataComponents.BOUND_SPELLS.get())
                || staff.has(ModDataComponents.ACTIVE_SPELL.get());
        boolean hasEnchantments = !staff.getEnchantments().isEmpty();
        if (!hasSpells && !hasEnchantments) {
            return;
        }

        ItemStack result = staff.copy();
        result.remove(ModDataComponents.BOUND_SPELLS.get());
        result.remove(ModDataComponents.ACTIVE_SPELL.get());
        result.remove(DataComponents.ENCHANTMENTS);
        event.setOutput(result);
        event.setXp(0);
    }
}
