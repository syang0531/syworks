package com.syang.syworks.world.level.block.entity;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * What a hopper or pipe sees of a machine through one face — the vanilla furnace's rules, so a
 * row of machines can be wired exactly like a row of furnaces:
 *
 * <ul>
 *   <li><b>top</b> — the input slot</li>
 *   <li><b>sides</b> — the fuel slot</li>
 *   <li><b>bottom</b> — the output, plus an empty bucket left in the fuel slot by a lava bucket
 *       (and nothing else from the fuel slot, so fuel is never pulled back out)</li>
 * </ul>
 *
 * <p>Inserting into the output is refused by the inventory itself ({@code isValid}), and into the
 * fuel slot unless the item burns. Before 1.1.2 the capability was the whole inventory on every
 * face, so a hopper underneath drained the input and the fuel along with the result.
 */
public final class MachineFaceHandler implements ResourceHandler<ItemResource> {

    private static final int[] UP = {MachineBlockEntity.SLOT_INPUT};
    private static final int[] DOWN = {MachineBlockEntity.SLOT_OUTPUT, MachineBlockEntity.SLOT_FUEL};
    private static final int[] SIDES = {MachineBlockEntity.SLOT_FUEL};

    private final ItemStacksResourceHandler inventory;
    private final int[] slots;
    private final boolean bottom;

    private MachineFaceHandler(ItemStacksResourceHandler inventory, int[] slots, boolean bottom) {
        this.inventory = inventory;
        this.slots = slots;
        this.bottom = bottom;
    }

    /** The handler for {@code side}. A null side (no particular face) gets the whole inventory. */
    public static ResourceHandler<ItemResource> of(ItemStacksResourceHandler inventory, @Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        return switch (side) {
            case UP -> new MachineFaceHandler(inventory, UP, false);
            case DOWN -> new MachineFaceHandler(inventory, DOWN, true);
            default -> new MachineFaceHandler(inventory, SIDES, false);
        };
    }

    private int slot(int index) {
        Objects.checkIndex(index, slots.length);
        return slots[index];
    }

    private boolean canExtract(int slot, ItemResource resource) {
        if (bottom && slot == MachineBlockEntity.SLOT_FUEL) {
            return resource.is(Items.BUCKET) || resource.is(Items.WATER_BUCKET);
        }
        return true;
    }

    @Override
    public int size() {
        return slots.length;
    }

    @Override
    public ItemResource getResource(int index) {
        return inventory.getResource(slot(index));
    }

    @Override
    public long getAmountAsLong(int index) {
        return inventory.getAmountAsLong(slot(index));
    }

    @Override
    public long getCapacityAsLong(int index, ItemResource resource) {
        return inventory.getCapacityAsLong(slot(index), resource);
    }

    @Override
    public boolean isValid(int index, ItemResource resource) {
        return inventory.isValid(slot(index), resource);
    }

    @Override
    public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
        return inventory.insert(slot(index), resource, amount, transaction);
    }

    @Override
    public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
        int slot = slot(index);
        return canExtract(slot, resource) ? inventory.extract(slot, resource, amount, transaction) : 0;
    }
}
