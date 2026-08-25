package com.ohyeah.ohyeahmod.block;

import com.ohyeah.ohyeahmod.advancement.ModAdvancementIds;
import com.ohyeah.ohyeahmod.advancement.ModAdvancementTracker;
import com.ohyeah.ohyeahmod.entity.tiansuluobattleface.BattleFaceProfile;
import com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.PinkScarfProfile;
import com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.TiansuluoPinkScarfEntity;
import com.ohyeah.ohyeahmod.entity.tiansuluobattleface.TiansuluoBattleFaceEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * 栾栾块。
 */
public final class LuanluanEggBlock extends Block implements EntityBlock {
    public static final IntegerProperty HATCH = IntegerProperty.create("hatch", 0, 2);
    public static final IntegerProperty EGGS = IntegerProperty.create("eggs", 1, 4);

    private static final VoxelShape ONE_EGG_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 12.0D, 7.0D, 12.0D);
    private static final VoxelShape MULTI_EGG_SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 7.0D, 15.0D);

    private final String speciesId;
    private final Supplier<? extends EntityType<?>> entityTypeSupplier;

    public LuanluanEggBlock(String speciesId, Supplier<? extends EntityType<?>> entityTypeSupplier, Properties properties) {
        super(properties);
        this.speciesId = speciesId;
        this.entityTypeSupplier = entityTypeSupplier;
        this.registerDefaultState(this.stateDefinition.any().setValue(HATCH, 0).setValue(EGGS, 1));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LuanluanEggBlockEntity(pos, state);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockState result = super.playerWillDestroy(level, pos, state, player);
        if (level instanceof ServerLevel serverLevel
                && level.getBlockEntity(pos) instanceof LuanluanEggBlockEntity eggBlockEntity) {
            this.notifyParentOfBreak(serverLevel, eggBlockEntity.getParentUuid());
        }
        return result;
    }

    private void notifyParentOfBreak(ServerLevel level, @Nullable UUID parentUuid) {
        if (parentUuid == null) {
            return;
        }

        Entity parent = level.getEntity(parentUuid);
        if (parent == null || !parent.isAlive()) {
            return;
        }

        if (PinkScarfProfile.SPECIES_ID.equals(this.speciesId)
                && parent instanceof TiansuluoPinkScarfEntity) {
            level.broadcastEntityEvent(parent, PinkScarfProfile.EVENT_LUANLUAN_BLOCK_BROKEN);
        } else if (BattleFaceProfile.SPECIES_ID.equals(this.speciesId)
                && parent instanceof TiansuluoBattleFaceEntity) {
            level.broadcastEntityEvent(parent, BattleFaceProfile.EVENT_LUANLUAN_BLOCK_BROKEN);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && oldState.getBlock() != this) {
            this.scheduleNextHatch((ServerLevel) level, pos);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        if (level.getBlockState(pos).getBlock() != this) {
            return;
        }
        this.advanceHatch(level, pos, state, random);
    }

    /**
     * 旧存档中的方块可能没有保存计划刻；随机刻只负责重新安排定时孵化，避免继续依赖极低概率。
     */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, net.minecraft.util.RandomSource random) {
        this.scheduleNextHatch(level, pos);
    }

    private void scheduleNextHatch(ServerLevel level, BlockPos pos) {
        level.scheduleTick(pos, this, this.hatchIntervalTicks());
    }

    private int hatchIntervalTicks() {
        if (PinkScarfProfile.SPECIES_ID.equals(this.speciesId)) {
            return PinkScarfProfile.HATCH_INTERVAL_TICKS;
        }
        return BattleFaceProfile.HATCH_INTERVAL_TICKS;
    }

    private void advanceHatch(ServerLevel level, BlockPos pos, BlockState state, net.minecraft.util.RandomSource random) {
        int hatch = state.getValue(HATCH);
        if (hatch < 2) {
            BlockState nextState = state.setValue(HATCH, hatch + 1);
            level.setBlock(pos, nextState, Block.UPDATE_ALL);
            level.playSound(null, pos, net.minecraft.sounds.SoundEvents.TURTLE_EGG_CRACK,
                    net.minecraft.sounds.SoundSource.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
            level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.BLOCK_CHANGE, pos,
                    net.minecraft.world.level.gameevent.GameEvent.Context.of(nextState));
            this.scheduleNextHatch(level, pos);
            return;
        }

        this.hatch(level, pos, state, random);
    }

    private void hatch(ServerLevel level, BlockPos pos, BlockState state, net.minecraft.util.RandomSource random) {
        int eggs = state.getValue(EGGS);
        level.playSound(null, pos, net.minecraft.sounds.SoundEvents.TURTLE_EGG_HATCH,
                net.minecraft.sounds.SoundSource.BLOCKS, 0.7F, 0.9F + random.nextFloat() * 0.2F);
        level.removeBlock(pos, false);
        level.gameEvent(net.minecraft.world.level.gameevent.GameEvent.BLOCK_DESTROY, pos,
                net.minecraft.world.level.gameevent.GameEvent.Context.of(state));
        level.levelEvent(2001, pos, Block.getId(state));

        int hatchedCount = 0;
        for (int i = 0; i < eggs; i++) {
            var entity = this.entityTypeSupplier.get().create(level);
            if (entity instanceof AgeableMob ageable) {
                ageable.setAge(AgeableMob.BABY_START_AGE);
                ageable.moveTo(pos.getX() + 0.3D + i * 0.2D, pos.getY(), pos.getZ() + 0.3D,
                        random.nextFloat() * 360.0F, 0.0F);
                level.addFreshEntity(ageable);
                hatchedCount++;
            }
        }
        if (hatchedCount > 0) {
            ModAdvancementTracker.awardNearby(level, pos, ModAdvancementIds.HATCH_LUANLUAN);
            if (eggs >= 4 && hatchedCount == eggs) {
                ModAdvancementTracker.awardNearby(level, pos, ModAdvancementIds.FULL_LUANLUAN_NEST);
            }
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HATCH, EGGS);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(EGGS) > 1 ? MULTI_EGG_SHAPE : ONE_EGG_SHAPE;
    }
}
