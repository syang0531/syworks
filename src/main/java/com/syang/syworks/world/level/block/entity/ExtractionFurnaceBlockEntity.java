package com.syang.syworks.world.level.block.entity;

import com.syang.syworks.registry.ModBlockEntities;
import com.syang.syworks.registry.ModRecipes;
import com.syang.syworks.world.inventory.ExtractionFurnaceMenu;
import com.syang.syworks.world.item.crafting.ExtractionRecipe;
import com.syang.syworks.world.level.block.ExtractionFurnaceBlock;
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
import net.minecraft.world.item.crafting.SingleRecipeInput;
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
 * Drives the Extraction Furnace: consumes fuel to turn a source item (slot 0) into a metal
 * (slot 2) according to an {@link ExtractionRecipe}. Furnace-style burn + progress.
 *
 * <p>The inventory is a NeoForge {@link ItemStacksResourceHandler} (the 1.21.9+ transfer API):
 * it is what hoppers see through the item capability and what the menu's slots wrap.
 */
public class ExtractionFurnaceBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

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

    private int litTime;       // ticks of fuel remaining
    private int litDuration;   // total ticks the current fuel unit burns
    private int progress;      // ticks progressed on the current item
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

    public ExtractionFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXTRACTION_FURNACE.get(), pos, state);
    }

    public ItemStacksResourceHandler getInventory() {
        return inventory;
    }

    /** A copy of the stack in {@code slot} (the handler hands out immutable resources). */
    private ItemStack stackIn(int slot) {
        return inventory.getResource(slot).toStack(inventory.getAmountAsInt(slot));
    }

    /** Removes up to {@code amount} items from {@code slot}, committing immediately. */
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

        ExtractionRecipe recipe = getCurrentRecipe();
        boolean canProcess = canProcess(recipe);
        ItemStack fuel = stackIn(SLOT_FUEL);

        // Light the burner if we have work to do and fuel available.
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
            state = state.setValue(ExtractionFurnaceBlock.LIT, isLit());
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
        if (changed) {
            setChanged(level, pos, state);
        }
    }

    @Nullable
    private ExtractionRecipe getCurrentRecipe() {
        if (!(level instanceof ServerLevel server)) {
            return null;
        }
        return server.recipeAccess()
                .getRecipeFor(ModRecipes.EXTRACTION_TYPE.get(), new SingleRecipeInput(stackIn(SLOT_INPUT)), server)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    private boolean canProcess(@Nullable ExtractionRecipe recipe) {
        if (recipe == null || stackIn(SLOT_INPUT).isEmpty()) {
            return false;
        }
        return FurnaceSupport.outputAccepts(stackIn(SLOT_OUTPUT), recipe.result().create());
    }

    private void craft(ExtractionRecipe recipe) {
        ItemStack result = recipe.assemble(new SingleRecipeInput(stackIn(SLOT_INPUT)));
        take(SLOT_INPUT, 1);
        FurnaceSupport.addToOutput(inventory, SLOT_OUTPUT, result);
    }

    /** Called by the chunk right before this block entity is removed (block broken / replaced). */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null) {
            Containers.dropContents(level, pos, inventory.copyToList());
        }
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.syworks.extraction_furnace");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ExtractionFurnaceMenu(id, playerInventory, this, dataAccess);
    }

    // --- persistence (ValueIO since 1.21.6) ---

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
