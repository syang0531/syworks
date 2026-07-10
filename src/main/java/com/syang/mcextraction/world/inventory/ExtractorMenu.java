package com.syang.mcextraction.world.inventory;

import com.syang.mcextraction.registry.ModBlocks;
import com.syang.mcextraction.registry.ModMenuTypes;
import com.syang.mcextraction.registry.ModRecipes;
import com.syang.mcextraction.world.level.block.entity.ExtractorBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Container menu for the Extractor. Slot layout matches the vanilla furnace so the
 * furnace GUI texture can be reused: input (56,17), fuel (56,53), output (116,35).
 */
public class ExtractorMenu extends AbstractContainerMenu {

    private static final int MACHINE_SLOTS = ExtractorBlockEntity.SLOT_COUNT; // 3
    private static final int INV_START = MACHINE_SLOTS;
    private static final int HOTBAR_START = MACHINE_SLOTS + 27;
    private static final int TOTAL_SLOTS = MACHINE_SLOTS + 36;

    private final ExtractorBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData data;

    /** Client-side constructor: reads the block position from the network buffer. */
    public ExtractorMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(id, playerInventory,
                (ExtractorBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(4));
    }

    public ExtractorMenu(int id, Inventory playerInventory, ExtractorBlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.EXTRACTOR.get(), id);
        this.blockEntity = blockEntity;
        this.data = data;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        IItemHandler handler = blockEntity.getInventory();
        addSlot(new SlotItemHandler(handler, ExtractorBlockEntity.SLOT_INPUT, 56, 17));
        addSlot(new SlotItemHandler(handler, ExtractorBlockEntity.SLOT_FUEL, 56, 53));
        addSlot(new OutputSlot(handler, ExtractorBlockEntity.SLOT_OUTPUT, 116, 35));

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
                if (stack.getBurnTime(ModRecipes.EXTRACTION_TYPE.get()) > 0) {
                    moved = moveItemStackTo(stack, ExtractorBlockEntity.SLOT_FUEL,
                            ExtractorBlockEntity.SLOT_FUEL + 1, false);
                }
                if (!moved) {
                    moved = moveItemStackTo(stack, ExtractorBlockEntity.SLOT_INPUT,
                            ExtractorBlockEntity.SLOT_INPUT + 1, false);
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
                slot.set(ItemStack.EMPTY);
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
        return stillValid(access, player, ModBlocks.EXTRACTOR.get());
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

    private static class OutputSlot extends SlotItemHandler {
        OutputSlot(IItemHandler handler, int index, int x, int y) {
            super(handler, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }
    }
}
