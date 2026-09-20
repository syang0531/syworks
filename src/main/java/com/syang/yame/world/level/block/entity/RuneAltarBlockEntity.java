package com.syang.yame.world.level.block.entity;

import com.syang.yame.registry.ModBlockEntities;
import com.syang.yame.registry.ModRecipes;
import com.syang.yame.world.inventory.RuneAltarMenu;
import com.syang.yame.world.item.crafting.ImbueRecipe;
import com.syang.yame.world.item.crafting.ImbueRecipeInput;
import com.syang.yame.world.level.block.RuneAltarBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

/**
 * Drives the Rune Altar: combines the two input slots into a result per an {@link ImbueRecipe}.
 *
 * <p>Anvil-style, <b>not</b> furnace-style: there is no cast time or progress bar. The result is
 * shown immediately as a preview in the menu's output slot (computed by {@link #assembleResult})
 * and the inputs are consumed only when the player takes it (via {@link #consumeInputs}). The two
 * input slots are interchangeable — recipe matching and consumption both try either order.
 *
 * <p>The block-entity tick only keeps the {@link RuneAltarBlock#LIT} "ready" glow in sync with
 * whether a valid recipe is currently present.
 */
public class RuneAltarBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_BASE = 0;
    public static final int SLOT_CATALYST = 1;
    public static final int SLOT_COUNT = 2;

    private final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    };

    public RuneAltarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RUNE_ALTAR.get(), pos, state);
    }

    public ItemStacksResourceHandler getInventory() {
        return inventory;
    }

    private ItemStack stackIn(int slot) {
        return inventory.getResource(slot).toStack(inventory.getAmountAsInt(slot));
    }

    private void take(int slot, int amount) {
        ItemResource resource = inventory.getResource(slot);
        if (resource.isEmpty()) {
            return;
        }
        try (Transaction tx = Transaction.openRoot()) {
            inventory.extract(slot, resource, amount, tx);
            tx.commit();
        }
    }

    /** Server tick: keep the LIT (glowing "ready") state in sync with recipe validity. */
    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) {
            return;
        }
        boolean ready = getCurrentRecipe() != null;
        if (state.getValue(RuneAltarBlock.LIT) != ready) {
            level.setBlock(pos, state.setValue(RuneAltarBlock.LIT, ready), Block.UPDATE_ALL);
        }
    }

    @Nullable
    public ImbueRecipe getCurrentRecipe() {
        if (!(level instanceof ServerLevel server)) {
            return null;
        }
        ImbueRecipeInput input = new ImbueRecipeInput(stackIn(SLOT_BASE), stackIn(SLOT_CATALYST));
        return server.recipeAccess()
                .getRecipeFor(ModRecipes.IMBUE_TYPE.get(), input, server)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    /** The result preview for the current inputs, or EMPTY. No side effects. */
    public ItemStack assembleResult() {
        ImbueRecipe recipe = getCurrentRecipe();
        return recipe == null ? ItemStack.EMPTY : recipe.result().create();
    }

    /** Consume the required base + catalyst amounts, from whichever slot holds each. */
    public void consumeInputs() {
        ImbueRecipe recipe = getCurrentRecipe();
        if (recipe == null) {
            return;
        }
        if (recipe.baseMatches(stackIn(SLOT_BASE))) {
            take(SLOT_BASE, recipe.base().count());
            take(SLOT_CATALYST, recipe.catalyst().count());
        } else {
            take(SLOT_CATALYST, recipe.base().count());
            take(SLOT_BASE, recipe.catalyst().count());
        }
    }

    /** Called by the chunk right before this block entity is removed (block broken / replaced). */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            Containers.dropContents(level, pos, inventory.copyToList());
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.yame.rune_altar");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new RuneAltarMenu(id, playerInventory, this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output.child("Inventory"));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.deserialize(input.childOrEmpty("Inventory"));
    }
}
