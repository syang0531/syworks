package com.syang.yame.world.level.block.entity;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

/**
 * Small shared helpers for the two furnace-style machines, kept out of the block entities so the
 * transfer-API plumbing (immutable {@link ItemResource}s + amounts) lives in one place.
 */
final class FurnaceSupport {

    private FurnaceSupport() {
    }

    /** The container item left behind when {@code fuel} burns (lava bucket → bucket), or EMPTY. */
    static ItemStack craftingRemainder(ItemStack fuel) {
        ItemStackTemplate template = fuel.getCraftingRemainder();
        return template == null ? ItemStack.EMPTY : template.create();
    }

    /** Whether {@code result} fits into the output slot currently holding {@code out}. */
    static boolean outputAccepts(ItemStack out, ItemStack result) {
        if (out.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(out, result)) {
            return false;
        }
        return out.getCount() + result.getCount() <= out.getMaxStackSize();
    }

    /** Puts {@code result} into {@code slot}: starts a new stack or grows the matching one. */
    static void addToOutput(ItemStacksResourceHandler inventory, int slot, ItemStack result) {
        ItemResource current = inventory.getResource(slot);
        if (current.isEmpty()) {
            inventory.set(slot, ItemResource.of(result), result.getCount());
        } else {
            inventory.set(slot, current, inventory.getAmountAsInt(slot) + result.getCount());
        }
    }
}
