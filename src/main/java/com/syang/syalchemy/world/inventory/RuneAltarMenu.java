package com.syang.syalchemy.world.inventory;

import com.syang.syalchemy.registry.ModBlocks;
import com.syang.syalchemy.registry.ModMenuTypes;
import com.syang.syalchemy.world.level.block.entity.RuneAltarBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/**
 * Container menu for the Rune Altar. Two interchangeable input slots + one output slot that shows
 * the recipe result as an anvil-style preview: the output is recomputed each tick from the block
 * entity ({@link #updateResult}) and consuming it triggers {@link RuneAltarBlockEntity#consumeInputs}.
 * Slot coordinates match the GUI texture (textures/gui/rune_altar.png).
 */
public class RuneAltarMenu extends AbstractContainerMenu {

    private static final int RESULT_SLOT = 2;
    private static final int MACHINE_SLOTS = 3;              // base, catalyst, result
    private static final int INV_START = MACHINE_SLOTS;
    private static final int HOTBAR_START = MACHINE_SLOTS + 27;
    private static final int TOTAL_SLOTS = MACHINE_SLOTS + 36;

    private final RuneAltarBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final Container resultSlots = new SimpleContainer(1);

    public RuneAltarMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(id, playerInventory,
                (RuneAltarBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public RuneAltarMenu(int id, Inventory playerInventory, RuneAltarBlockEntity blockEntity) {
        super(ModMenuTypes.RUNE_ALTAR.get(), id);
        this.blockEntity = blockEntity;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        ItemStacksResourceHandler handler = blockEntity.getInventory();
        addSlot(new ResourceHandlerSlot(handler, handler::set, RuneAltarBlockEntity.SLOT_BASE, 44, 35));
        addSlot(new ResourceHandlerSlot(handler, handler::set, RuneAltarBlockEntity.SLOT_CATALYST, 76, 35));
        addSlot(new ResultSlot(resultSlots, 134, 35));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        updateResult();
    }

    /** Refresh the output preview from the current inputs (server-authoritative). */
    private void updateResult() {
        if (blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide()) {
            resultSlots.setItem(0, blockEntity.assembleResult());
        }
    }

    @Override
    public void broadcastChanges() {
        updateResult();
        super.broadcastChanges();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        // Result: craft one to the inventory, consuming inputs once (guarded on a live result).
        if (index == RESULT_SLOT) {
            ItemStack fresh = blockEntity.assembleResult();
            if (fresh.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack moving = fresh.copy();
            if (!moveItemStackTo(moving, INV_START, TOTAL_SLOTS, true) || moving.getCount() == fresh.getCount()) {
                return ItemStack.EMPTY;
            }
            blockEntity.consumeInputs();
            updateResult();
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        if (index < MACHINE_SLOTS) {
            // base / catalyst -> player inventory
            if (!moveItemStackTo(stack, INV_START, TOTAL_SLOTS, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            // player inventory -> input slots, else shuffle between inv/hotbar
            if (!moveItemStackTo(stack, RuneAltarBlockEntity.SLOT_BASE, RuneAltarBlockEntity.SLOT_CATALYST + 1, false)) {
                if (index < HOTBAR_START) {
                    if (!moveItemStackTo(stack, HOTBAR_START, TOTAL_SLOTS, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!moveItemStackTo(stack, INV_START, HOTBAR_START, false)) {
                    return ItemStack.EMPTY;
                }
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.RUNE_ALTAR.get());
    }

    /** Output slot: displays the synced preview; picking it up consumes the inputs. */
    private class ResultSlot extends Slot {
        ResultSlot(Container container, int x, int y) {
            super(container, 0, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack remove(int amount) {
            // Yield the live result (empty if inputs no longer form a recipe) — never a stale preview.
            return blockEntity.assembleResult();
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            blockEntity.consumeInputs();
            updateResult();
            super.onTake(player, stack);
        }
    }
}
