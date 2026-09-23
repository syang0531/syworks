package com.syang.syworks.world.level.block.entity;

import com.syang.syworks.registry.ModBlockEntities;
import com.syang.syworks.world.inventory.IncineratorMenu;
import com.syang.syworks.world.level.block.IncineratorBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.transfer.VoidingResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.RootCommitJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

/**
 * The incinerator: a trash can with a fire in it. Everything put in is destroyed on the spot —
 * no fuel, no output, no experience. Paying experience would turn it into a machine that competes
 * with the other three for the same junk, and it would always win because it takes anything.
 *
 * <p>The only state is how long the fire keeps showing after the last thing burned, which lights
 * the block ({@link IncineratorBlock#LIT}) and the flame in the GUI. It is not saved: a reloaded
 * incinerator is simply cold.
 */
public class IncineratorBlockEntity extends BlockEntity implements MenuProvider {

    /** How long the fire shows after the last item burned. */
    private static final int BURN_DISPLAY_TICKS = 40;
    /** Minimum gap between burn sounds, so a hopper feeding every 8 ticks does not drone. */
    private static final int SOUND_COOLDOWN_TICKS = 10;

    private int burnTicks;
    private long lastSoundTick = Long.MIN_VALUE;

    /**
     * What both hoppers (through the item capability) and the GUI slot insert into. An insert
     * inside a transaction that is later rolled back — a hopper's simulation, say — must not light
     * the fire, so the effect is deferred to the root commit.
     */
    private final VoidingResourceHandler<ItemResource> handler = new VoidingResourceHandler<>(ItemResource.EMPTY) {
        private final RootCommitJournal journal = new RootCommitJournal(IncineratorBlockEntity.this::burn);

        @Override
        public int insert(int index, ItemResource resource, int amount, TransactionContext transaction) {
            int inserted = super.insert(index, resource, amount, transaction);
            journal.updateSnapshots(transaction);
            return inserted;
        }
    };

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return burnTicks;
        }

        @Override
        public void set(int index, int value) {
            burnTicks = value;
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public IncineratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INCINERATOR.get(), pos, state);
    }

    public VoidingResourceHandler<ItemResource> getHandler() {
        return handler;
    }

    /** Something just went in. Lights the fire and crackles; the item itself is already gone. */
    public void burn() {
        if (level == null || level.isClientSide()) {
            return;
        }
        burnTicks = BURN_DISPLAY_TICKS;
        BlockState state = getBlockState();
        if (!state.getValue(IncineratorBlock.LIT)) {
            level.setBlock(worldPosition, state.setValue(IncineratorBlock.LIT, Boolean.TRUE), Block.UPDATE_ALL);
        }
        long now = level.getGameTime();
        if (now - lastSoundTick >= SOUND_COOLDOWN_TICKS) {
            lastSoundTick = now;
            // The same hiss an item makes when it falls into lava — the thing this replaces.
            level.playSound(null, worldPosition, SoundEvents.GENERIC_BURN, SoundSource.BLOCKS,
                    0.4F, 2.0F + level.getRandom().nextFloat() * 0.4F);
        }
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (burnTicks > 0) {
            burnTicks--;
        }
        // Also catches a block saved while lit: burnTicks is not persisted, so it goes out on load.
        if (burnTicks == 0 && state.getValue(IncineratorBlock.LIT)) {
            level.setBlock(pos, state.setValue(IncineratorBlock.LIT, Boolean.FALSE), Block.UPDATE_ALL);
        }
    }

    // --- MenuProvider ---

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new IncineratorMenu(id, playerInventory, this, dataAccess);
    }
}
