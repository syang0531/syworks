package com.syang.yame.world.level.block.entity;

import com.syang.yame.registry.ModBlockEntities;
import com.syang.yame.registry.ModRecipes;
import com.syang.yame.world.inventory.AlloyFurnaceMenu;
import com.syang.yame.world.item.crafting.AlloyRecipe;
import com.syang.yame.world.item.crafting.AlloyRecipeInput;
import com.syang.yame.world.level.block.AlloyFurnaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Drives the Alloy Furnace: consumes fuel to combine 2–3 metals (slots 0–2) into an
 * alloy (slot 4) per an {@link AlloyRecipe}. Same burn/progress model as the Extraction Furnace.
 */
public class AlloyFurnaceBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_INPUT_0 = 0;
    public static final int SLOT_INPUT_1 = 1;
    public static final int SLOT_INPUT_2 = 2;
    public static final int SLOT_FUEL = 3;
    public static final int SLOT_OUTPUT = 4;
    public static final int INPUT_COUNT = 3;
    public static final int SLOT_COUNT = 5;

    private static final int DEFAULT_PROCESS_TIME = 200;

    private final ItemStackHandler inventory = new ItemStackHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_OUTPUT -> false;
                case SLOT_FUEL -> stack.getBurnTime(ModRecipes.ALLOY_TYPE.get()) > 0;
                default -> true;
            };
        }
    };

    private int litTime;
    private int litDuration;
    private int progress;
    private int maxProgress = DEFAULT_PROCESS_TIME;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> litTime;
                case 1 -> litDuration;
                case 2 -> progress;
                case 3 -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> litTime = value;
                case 1 -> litDuration = value;
                case 2 -> progress = value;
                case 3 -> maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public AlloyFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALLOY_FURNACE.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    private boolean isLit() {
        return litTime > 0;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) {
            return;
        }

        boolean wasLit = isLit();
        boolean changed = false;

        if (isLit()) {
            litTime--;
        }

        AlloyRecipe recipe = getCurrentRecipe();
        boolean canProcess = canProcess(recipe);
        ItemStack fuel = inventory.getStackInSlot(SLOT_FUEL);

        if (!isLit() && canProcess && !fuel.isEmpty()) {
            litTime = fuel.getBurnTime(ModRecipes.ALLOY_TYPE.get());
            litDuration = litTime;
            if (isLit()) {
                changed = true;
                ItemStack remainder = fuel.getCraftingRemainingItem();
                fuel.shrink(1);
                if (fuel.isEmpty() && !remainder.isEmpty()) {
                    inventory.setStackInSlot(SLOT_FUEL, remainder);
                }
            }
        }

        if (isLit() && canProcess) {
            maxProgress = recipe.processTime();
            progress++;
            if (progress >= maxProgress) {
                craft(recipe);
                progress = 0;
                changed = true;
            }
        } else if (isLit()) {
            // Burning but can't process (input removed / output full) — reset at once, like a furnace.
            if (progress != 0) {
                progress = 0;
                changed = true;
            }
        } else if (progress > 0) {
            // Out of fuel — wind progress down gradually.
            progress = Math.max(0, progress - 2);
        }

        if (wasLit != isLit()) {
            changed = true;
            state = state.setValue(AlloyFurnaceBlock.LIT, isLit());
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
        if (changed) {
            setChanged(level, pos, state);
        }
    }

    @Nullable
    private AlloyRecipe getCurrentRecipe() {
        if (level == null) {
            return null;
        }
        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.ALLOY_TYPE.get(), currentInput(), level)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    private AlloyRecipeInput currentInput() {
        List<ItemStack> items = new ArrayList<>(INPUT_COUNT);
        for (int i = 0; i < INPUT_COUNT; i++) {
            items.add(inventory.getStackInSlot(i));
        }
        return new AlloyRecipeInput(items);
    }

    private boolean canProcess(@Nullable AlloyRecipe recipe) {
        if (recipe == null) {
            return false;
        }
        ItemStack result = recipe.result();
        ItemStack out = inventory.getStackInSlot(SLOT_OUTPUT);
        if (out.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(out, result)) {
            return false;
        }
        return out.getCount() + result.getCount() <= out.getMaxStackSize();
    }

    private void craft(AlloyRecipe recipe) {
        // Consume each ingredient from the first input slot that satisfies it (matches
        // the order-independent logic in AlloyRecipe#matches).
        List<SizedIngredient> remaining = new ArrayList<>(recipe.inputs());
        for (int slot = 0; slot < INPUT_COUNT; slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            Iterator<SizedIngredient> it = remaining.iterator();
            while (it.hasNext()) {
                SizedIngredient ingredient = it.next();
                if (ingredient.test(stack)) {
                    inventory.extractItem(slot, ingredient.count(), false);
                    it.remove();
                    break;
                }
            }
        }

        ItemStack result = recipe.result();
        ItemStack out = inventory.getStackInSlot(SLOT_OUTPUT);
        if (out.isEmpty()) {
            inventory.setStackInSlot(SLOT_OUTPUT, result.copy());
        } else {
            out.grow(result.getCount());
        }
    }

    public void drops(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            container.setItem(i, inventory.getStackInSlot(i));
        }
        Containers.dropContents(level, pos, container);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.yame.alloy_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new AlloyFurnaceMenu(id, playerInventory, this, dataAccess);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("LitTime", litTime);
        tag.putInt("LitDuration", litDuration);
        tag.putInt("Progress", progress);
        tag.putInt("MaxProgress", maxProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        litTime = tag.getInt("LitTime");
        litDuration = tag.getInt("LitDuration");
        progress = tag.getInt("Progress");
        maxProgress = tag.contains("MaxProgress") ? tag.getInt("MaxProgress") : DEFAULT_PROCESS_TIME;
    }
}
