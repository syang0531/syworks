package com.syang.syworks.world.level.block;

import com.mojang.serialization.MapCodec;
import com.syang.syworks.registry.ModBlockEntities;
import com.syang.syworks.world.level.block.entity.IncineratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * The incinerator's block. Shaped like the machines — {@link #FACING} toward the placer and a
 * {@link #LIT} front — so it sits in a row with them, but it is not one: it is not a
 * {@link ModMachine} row, burns no fuel and has no recipes. See {@link IncineratorBlockEntity}.
 */
public class IncineratorBlock extends Block implements EntityBlock {

    public static final MapCodec<IncineratorBlock> CODEC = simpleCodec(IncineratorBlock::new);

    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public IncineratorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, Boolean.FALSE));
    }

    @Override
    protected MapCodec<? extends IncineratorBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /** Orange flame and smoke out of the top while something is burning. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!state.getValue(LIT)) {
            return;
        }
        double x = pos.getX() + 0.25D + random.nextDouble() * 0.5D;
        double y = pos.getY() + 1.0D;
        double z = pos.getZ() + 0.25D + random.nextDouble() * 0.5D;
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.02D, 0.0D);
        level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0.0D, 0.03D, 0.0D);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IncineratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModBlockEntities.INCINERATOR.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> ((IncineratorBlockEntity) be).tick(lvl, pos, st);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        if (player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof IncineratorBlockEntity incinerator) {
            serverPlayer.openMenu(incinerator, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.SUCCESS;
    }
}
