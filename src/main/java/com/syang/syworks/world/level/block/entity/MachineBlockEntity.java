package com.syang.syworks.world.level.block.entity;

import com.syang.syworks.registry.ModBlockEntities;
import com.syang.syworks.registry.ModRecipes;
import com.syang.syworks.world.inventory.MachineMenu;
import com.syang.syworks.world.item.crafting.ProcessingRecipe;
import com.syang.syworks.world.level.block.MachineBlock;
import com.syang.syworks.world.level.block.ModMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.ExperienceOrb;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

/**
 * Drives every machine: burns fuel to turn the input (slot 0) into the output (slot 2) according to
 * a {@link ProcessingRecipe} of its own {@link ModMachine}'s type. Furnace-style burn + progress.
 *
 * <p>The inventory is a NeoForge {@link ItemStacksResourceHandler} (the 1.21.9+ transfer API):
 * it is what hoppers see through the item capability and what the menu's slots wrap.
 *
 * <p>Experience accumulates as runs finish and is paid out when a player takes the output, exactly
 * as a furnace does — so a hopper can drain the machine, but only a person collects the XP.
 */
public class MachineBlockEntity extends BlockEntity implements MenuProvider {

    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

    private static final int DEFAULT_PROCESS_TIME = 200;

    private final ModMachine machine;

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
    private float storedExperience;

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

    public MachineBlockEntity(ModMachine machine, BlockPos pos, BlockState state) {
        super(ModBlockEntities.MACHINES.get(machine).get(), pos, state);
        this.machine = machine;
    }

    public ModMachine machine() {
        return machine;
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

        ProcessingRecipe recipe = getCurrentRecipe();
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
            state = state.setValue(MachineBlock.LIT, isLit());
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
        if (changed) {
            setChanged(level, pos, state);
        }
    }

    @Nullable
    private ProcessingRecipe getCurrentRecipe() {
        if (!(level instanceof ServerLevel server)) {
            return null;
        }
        return server.recipeAccess()
                .getRecipeFor(ModRecipes.TYPES.get(machine).get(), new SingleRecipeInput(stackIn(SLOT_INPUT)), server)
                .map(RecipeHolder::value)
                .orElse(null);
    }

    private boolean canProcess(@Nullable ProcessingRecipe recipe) {
        if (recipe == null) {
            return false;
        }
        // matches() already checked the count, but the stack can shrink between ticks.
        if (stackIn(SLOT_INPUT).getCount() < recipe.inputCount()) {
            return false;
        }
        return FurnaceSupport.outputAccepts(stackIn(SLOT_OUTPUT), recipe.result().create());
    }

    private void craft(ProcessingRecipe recipe) {
        ItemStack result = recipe.assemble(new SingleRecipeInput(stackIn(SLOT_INPUT)));
        take(SLOT_INPUT, recipe.inputCount());
        FurnaceSupport.addToOutput(inventory, SLOT_OUTPUT, result);
        storedExperience += recipe.experience();
    }

    /**
     * Pays out everything earned since the last collection, rounding the fractional remainder by
     * chance so small per-run values still add up honestly over many runs (vanilla does the same).
     * Called by the output slot when a player takes from it.
     */
    public void awardExperience(Player player) {
        if (!(level instanceof ServerLevel server) || storedExperience <= 0.0F) {
            return;
        }
        int whole = Mth.floor(storedExperience);
        float fraction = storedExperience - whole;
        if (fraction > 0.0F && server.getRandom().nextFloat() < fraction) {
            whole++;
        }
        storedExperience = 0.0F;
        setChanged();
        if (whole > 0) {
            ExperienceOrb.award(server, player.position(), whole);
        }
    }

    /** Called by the chunk right before this block entity is removed (block broken / replaced). */
    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel server) {
            Containers.dropContents(level, pos, inventory.copyToList());
            // Don't silently eat what the machine already earned.
            int whole = Mth.floor(storedExperience);
            if (whole > 0) {
                ExperienceOrb.award(server, Vec3.atCenterOf(pos), whole);
            }
            storedExperience = 0.0F;
        } else if (level != null) {
            Containers.dropContents(level, pos, inventory.copyToList());
        }
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return Component.translatable(machine.translationKey());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new MachineMenu(machine, id, playerInventory, this, dataAccess);
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
        output.putFloat("Experience", storedExperience);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.deserialize(input.childOrEmpty("Inventory"));
        litTime = input.getIntOr("LitTime", 0);
        litDuration = input.getIntOr("LitDuration", 0);
        progress = input.getIntOr("Progress", 0);
        maxProgress = input.getIntOr("MaxProgress", DEFAULT_PROCESS_TIME);
        storedExperience = input.getFloatOr("Experience", 0.0F);
    }
}
