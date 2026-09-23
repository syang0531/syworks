package com.syang.syworks.world.inventory;

import com.syang.syworks.registry.ModBlocks;
import com.syang.syworks.registry.ModMenuTypes;
import com.syang.syworks.world.level.block.entity.IncineratorBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/**
 * One slot that is always empty, over the player inventory. Whatever lands in the slot — clicked,
 * dragged or shift-clicked in — is handed to the incinerator's voiding handler and is gone.
 * There is no undo.
 */
public class IncineratorMenu extends AbstractContainerMenu {

    public static final int SLOT_X = 80;
    public static final int SLOT_Y = 35;

    private static final int INV_START = 1;

    private final ContainerLevelAccess access;
    private final ContainerData data;

    /** Client-side constructor: reads the block position from the network buffer. */
    public IncineratorMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(id, playerInventory,
                (IncineratorBlockEntity) playerInventory.player.level().getBlockEntity(buf.readBlockPos()),
                new SimpleContainerData(1));
    }

    public IncineratorMenu(int id, Inventory playerInventory, IncineratorBlockEntity blockEntity, ContainerData data) {
        super(ModMenuTypes.INCINERATOR.get(), id);
        this.data = data;
        this.access = ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos());

        // The handler already accepts everything and keeps nothing; the modifier is only told
        // what the player dropped in, so that is where the fire gets lit.
        addSlot(new ResourceHandlerSlot(blockEntity.getHandler(), (index, resource, amount) -> {
            if (!resource.isEmpty() && amount > 0) {
                blockEntity.burn();
            }
        }, 0, SLOT_X, SLOT_Y));

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

    /** Shift-click from the inventory burns the whole stack. The incinerator slot has nothing to give back. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (index < INV_START || slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack result = stack.copy();
        if (!moveItemStackTo(stack, 0, INV_START, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(access, player, ModBlocks.INCINERATOR.get());
    }

    /** True while the fire from the last burned item is still showing. */
    public boolean isBurning() {
        return data.get(0) > 0;
    }
}
