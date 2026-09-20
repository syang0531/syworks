package com.syang.syalchemy.world.level.block.entity;

import com.syang.syalchemy.registry.ModBlockEntities;
import com.syang.syalchemy.registry.ModRecipes;
import com.syang.syalchemy.world.inventory.AlloyFurnaceMenu;
import com.syang.syalchemy.world.item.crafting.AlloyRecipe;
import com.syang.syalchemy.world.item.crafting.AlloyRecipeInput;
import com.syang.syalchemy.world.level.block.AlloyFurnaceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
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

    private final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(SLOT_COUNT) {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return switch (slot) {
                case SLOT_OUTPUT -> false;
                case SLOT_FUEL -> level != null && level.fuelValues().burnDuration(resource.toStack()) > 0;
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

    private boolean isLit() {
        return litTime > 0;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) {
            return;
        }

        boolean wasLit = isLit();
        boolean changed = false;

        if (isLit()) {
            litTime--;
        }

        AlloyRecipe recipe = getCurrentRecipe();
        boolean canProcess = canProcess(recipe);
        ItemStack fuel = stackIn(SLOT_FUEL);

        if (!isLit() && canProcess && !fuel.isEmpty()) {
            litTime = level.fuelValues().burnDuration(fuel);
            litDuration = litTime;
            if (isLit()) {
                changed = true;
                ItemStack remainder = FurnaceSupport.craftingRemainder(fuel);
                take(SLOT_FUEL, 1);
                if (stackIn(SLOT_FUEL).isEmpty() && !remainder.isEmpty()) {
                    inventory.set(SLOT_FUEL, ItemResource.of(remainder), remainder.getCount());
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
        if (!(level instanceof ServerLevel server)) {
            return null;
        }
        return server.recipeAccess()
                .getRecipeFor(ModRecipes.ALLOY_TYPE.get(), currentInput(), server)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    private AlloyRecipeInput currentInput() {
        List<ItemStack> items = new ArrayList<>(INPUT_COUNT);
        for (int i = 0; i < INPUT_COUNT; i++) {
            items.add(stackIn(i));
        }
        return new AlloyRecipeInput(items);
    }

    private boolean canProcess(@Nullable AlloyRecipe recipe) {
        if (recipe == null) {
            return false;
        }
        return FurnaceSupport.outputAccepts(stackIn(SLOT_OUTPUT), recipe.result().create());
    }

    private void craft(AlloyRecipe recipe) {
        // Consume each ingredient from the first input slot that satisfies it (matches
        // the order-independent logic in AlloyRecipe#matches).
        List<SizedIngredient> remaining = new ArrayList<>(recipe.inputs());
        for (int slot = 0; slot < INPUT_COUNT; slot++) {
            ItemStack stack = stackIn(slot);
            if (stack.isEmpty()) {
                continue;
            }
            Iterator<SizedIngredient> it = remaining.iterator();
            while (it.hasNext()) {
                SizedIngredient ingredient = it.next();
                if (ingredient.test(stack)) {
                    take(slot, ingredient.count());
                    it.remove();
                    break;
                }
            }
        }

        FurnaceSupport.addToOutput(inventory, SLOT_OUTPUT, recipe.result().create());
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
        return Component.translatable("block.syalchemy.alloy_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new AlloyFurnaceMenu(id, playerInventory, this, dataAccess);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output.child("Inventory"));
        output.putInt("LitTime", litTime);
        output.putInt("LitDuration", litDuration);
        output.putInt("Progress", progress);
        output.putInt("MaxProgress", maxProgress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.deserialize(input.childOrEmpty("Inventory"));
        litTime = input.getIntOr("LitTime", 0);
        litDuration = input.getIntOr("LitDuration", 0);
        progress = input.getIntOr("Progress", 0);
        maxProgress = input.getIntOr("MaxProgress", DEFAULT_PROCESS_TIME);
    }
}
