package com.syang.syworks.world.inventory;

import com.syang.syworks.registry.ModBlocks;
import com.syang.syworks.registry.ModMenuTypes;
import com.syang.syworks.world.level.block.entity.ExtractionFurnaceBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/**
 * Container menu for the Extraction Furnace. Slot layout matches the vanilla furnace so the
 * furnace GUI texture can be reused: input (56,17), fuel (56,53), output (116,35).
 */
public class ExtractionFurnaceMenu extends AbstractContainerMenu {

    private static final int MACHINE_SLOTS = ExtractionFurnaceBlockEntity.SLOT_COUNT; // 3
    private static final int INV_START = MACHINE_SLOTS;
    private static final int HOTBAR_START = MACHINE_SLOTS + 27;
    private static final int TOTAL_SLOTS = MACHINE_SLOTS + 36;

    private final ContainerLevelAccess access;
    private final ContainerData data;

    /** Client-side constructor: reads the block position from the network buffer. */
    public ExtractionFurnaceMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(id, playerInventory,
                (ExtractionFurnaceBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(4));
    }

    public ExtractionFurnaceMenu(int id, Inventory playerInventory, ExtractionFurnaceBlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.EXTRACTION_FURNACE.get(), id);
        this.data = data;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        ItemStacksResourceHandler handler = blockEntity.getInventory();
        addSlot(new ResourceHandlerSlot(handler, handler::set, ExtractionFurnaceBlockEntity.SLOT_INPUT, 56, 17));
        addSlot(new ResourceHandlerSlot(handler, handler::set, ExtractionFurnaceBlockEntity.SLOT_FUEL, 56, 53));
        addSlot(new OutputSlot(handler, ExtractionFurnaceBlockEntity.SLOT_OUTPUT, 116, 35));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        addDataSlots(data);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < MACHINE_SLOTS) {
                // Machine slot → player inventory.
                if (!moveItemStackTo(stack, INV_START, TOTAL_SLOTS, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player inventory → machine (fuel if burnable, otherwise input).
                boolean moved = false;
                if (player.level().fuelValues().burnDuration(stack) > 0) {
                    moved = moveItemStackTo(stack, ExtractionFurnaceBlockEntity.SLOT_FUEL,
                            ExtractionFurnaceBlockEntity.SLOT_FUEL + 1, false);
                }
                if (!moved) {
                    moved = moveItemStackTo(stack, ExtractionFurnaceBlockEntity.SLOT_INPUT,
                            ExtractionFurnaceBlockEntity.SLOT_INPUT + 1, false);
                }
                if (!moved) {
                    // Shuffle between main inventory and hotbar.
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
            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.EXTRACTION_FURNACE.get());
    }

    // --- GUI helpers ---

    public boolean isLit() {
        return data.get(0) > 0;
    }

    /** Burn flame height, 0..14. */
    public int getScaledFuel() {
        int lit = data.get(0);
        int duration = data.get(1);
        if (duration == 0) {
            duration = 200;
        }
        return lit * 14 / duration;
    }

    /** Progress arrow width, 0..24. */
    public int getScaledProgress() {
        int prog = data.get(2);
        int max = data.get(3);
        return (max == 0 || prog == 0) ? 0 : prog * 24 / max;
    }

    private static class OutputSlot extends ResourceHandlerSlot {
        OutputSlot(ItemStacksResourceHandler handler, int index, int x, int y) {
            super(handler, handler::set, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
