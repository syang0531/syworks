package com.syang.mcextraction.world.level.block.entity;

import com.syang.mcextraction.registry.ModBlockEntities;
import com.syang.mcextraction.registry.ModRecipes;
import com.syang.mcextraction.world.inventory.ExtractorMenu;
import com.syang.mcextraction.world.item.crafting.ExtractionRecipe;
import com.syang.mcextraction.world.level.block.ExtractorBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Drives the Extractor: consumes fuel to turn a source item (slot 0) into a metal
 * (slot 2) according to an {@link ExtractionRecipe}. Furnace-style burn + progress.
 */
public class ExtractorBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

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
                case SLOT_FUEL -> stack.getBurnTime(ModRecipes.EXTRACTION_TYPE.get()) > 0;
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

    public ExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EXTRACTOR.get(), pos, state);
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

        ExtractionRecipe recipe = getCurrentRecipe();
        boolean canProcess = canProcess(recipe);
        ItemStack fuel = inventory.getStackInSlot(SLOT_FUEL);

        // Light the burner if we have work to do and fuel available.
        if (!isLit() && canProcess && !fuel.isEmpty()) {
            litTime = fuel.getBurnTime(ModRecipes.EXTRACTION_TYPE.get());
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
                craft(recipe, level);
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
            state = state.setValue(ExtractorBlock.LIT, isLit());
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
        if (changed) {
            setChanged(level, pos, state);
        }
    }

    @Nullable
    private ExtractionRecipe getCurrentRecipe() {
        if (level == null) {
            return null;
        }
        return level.getRecipeManager()
                .getRecipeFor(ModRecipes.EXTRACTION_TYPE.get(),
                        new SingleRecipeInput(inventory.getStackInSlot(SLOT_INPUT)), level)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    private boolean canProcess(@Nullable ExtractionRecipe recipe) {
        if (recipe == null || inventory.getStackInSlot(SLOT_INPUT).isEmpty()) {
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

    private void craft(ExtractionRecipe recipe, Level level) {
        ItemStack result = recipe.assemble(
                new SingleRecipeInput(inventory.getStackInSlot(SLOT_INPUT)), level.registryAccess());
        inventory.extractItem(SLOT_INPUT, 1, false);
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

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mcextraction.extractor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ExtractorMenu(id, playerInventory, this, dataAccess);
    }

    // --- NBT ---

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
