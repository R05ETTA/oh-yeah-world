package com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf;

import com.ohyeah.ohyeahmod.block.LuanluanEggBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.MoveToBlockGoal;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * 粉围巾的快速产卵目标。
 *
 * <p>产卵地点以携蛋实体当前位置为中心，而不是以喂食玩家为中心；先找最近的
 * 合法方块，再交给原生 MoveToBlockGoal 寻路。找不到或寻路失败时快速放弃本轮，
 * 保留携蛋状态，下一轮重新选择地点。</p>
 */
public final class PinkScarfLayEggGoal extends MoveToBlockGoal {
    private final TiansuluoPinkScarfEntity entity;
    private final int layingTicks;
    private int travelTicks;

    public PinkScarfLayEggGoal(TiansuluoPinkScarfEntity entity, int layingTicks) {
        super(entity, 1.0D, PinkScarfProfile.EGG_SEARCH_RADIUS, 3);
        this.entity = entity;
        this.layingTicks = layingTicks;
        this.verticalSearchStart = -1;
    }

    @Override
    protected int nextStartTick(net.minecraft.world.entity.PathfinderMob creature) {
        return this.reducedTickDelay(PinkScarfProfile.EGG_SEARCH_RETRY_TICKS);
    }

    @Override
    public boolean canUse() {
        if (!this.entity.state().hasCarriedEggBlock(this.entity) || this.entity.isBaby()) {
            return false;
        }

        BlockPos currentBase = this.entity.blockPosition().below();
        if (this.canPlaceEggAt(currentBase)) {
            this.blockPos = currentBase;
            return true;
        }

        BlockPos savedTarget = this.entity.state().getEggBlockTargetPos();
        if (savedTarget != null
                && this.isNearTarget(savedTarget)
                && this.canPlaceEggAt(savedTarget)
                && this.canReachEggTarget(savedTarget.above())) {
            this.blockPos = savedTarget;
            return true;
        }

        // 不继续追逐旧的远距离/失效目标；携蛋状态本身仍然保留。
        if (savedTarget != null) {
            this.entity.state().setEggBlockTargetPos(null);
        }
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        return this.entity.state().hasCarriedEggBlock(this.entity)
                && !this.entity.isBaby()
                && this.travelTicks < PinkScarfProfile.EGG_MAX_TRAVEL_TICKS
                && super.canContinueToUse();
    }

    @Override
    public void start() {
        super.start();
        this.travelTicks = 0;
        this.entity.state().setEggBlockPlacingCounter(0);
        this.entity.state().setEggBlockTargetPos(this.blockPos);
    }

    @Override
    public void stop() {
        boolean carrying = this.entity.state().hasCarriedEggBlock(this.entity);
        boolean reached = this.isReachedTarget();
        super.stop();
        this.entity.getNavigation().stop();
        this.entity.state().setEggBlockPlacingCounter(0);

        if (carrying && !reached) {
            this.entity.state().setEggBlockTargetPos(null);
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.travelTicks++;

        if (!this.entity.state().hasCarriedEggBlock(this.entity) || !this.isReachedTarget()) {
            return;
        }

        this.entity.getNavigation().stop();
        int counter = this.entity.state().getEggBlockPlacingCounter() + 1;
        this.entity.state().setEggBlockPlacingCounter(counter);
        if (counter >= this.layingTicks) {
            this.placeEggBlock();
        }
    }

    @Override
    protected boolean findNearestBlock() {
        Level level = this.entity.level();
        BlockPos center = this.entity.blockPosition();

        // 由近到远扫描方形环，避免随机抽样连续错过身边的合法位置。
        for (int radius = 0; radius <= PinkScarfProfile.EGG_SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (this.trySurfaceColumn(level, center.getX() + dx, center.getZ() - radius)) {
                    return true;
                }
                if (radius > 0 && this.trySurfaceColumn(level, center.getX() + dx, center.getZ() + radius)) {
                    return true;
                }
            }
            for (int dz = -radius + 1; dz <= radius - 1; dz++) {
                if (this.trySurfaceColumn(level, center.getX() - radius, center.getZ() + dz)) {
                    return true;
                }
                if (radius > 0 && this.trySurfaceColumn(level, center.getX() + radius, center.getZ() + dz)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean trySurfaceColumn(Level level, int x, int z) {
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        for (int y = surface - 1; y <= surface + 1; y++) {
            BlockPos candidate = new BlockPos(x, y, z);
            if (this.entity.isWithinRestriction(candidate)
                    && this.canPlaceEggAt(candidate)
                    && this.canReachEggTarget(candidate.above())) {
                this.blockPos = candidate;
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockPos eggPos = pos.above();
        BlockState baseState = level.getBlockState(pos);
        BlockState eggState = PinkScarfProfile.getSpeciesEggBlock().defaultBlockState();
        return baseState.isFaceSturdy(level, pos, Direction.UP)
                && level.isEmptyBlock(eggPos)
                && level.getFluidState(eggPos).isEmpty()
                && eggState.canSurvive(level, eggPos);
    }

    private boolean canReachEggTarget(BlockPos eggPos) {
        if (eggPos.closerToCenterThan(this.entity.position(), 1.5D)) {
            return true;
        }
        var path = this.entity.getNavigation().createPath(eggPos, 0);
        return path != null && path.canReach();
    }

    private boolean canPlaceEggAt(BlockPos basePos) {
        return this.isValidTarget(this.entity.level(), basePos);
    }

    private boolean isNearTarget(BlockPos basePos) {
        return basePos.above().closerToCenterThan(
                this.entity.position(),
                PinkScarfProfile.EGG_MAX_TARGET_DISTANCE
        );
    }

    private void placeEggBlock() {
        if (this.entity.level().isClientSide) {
            return;
        }

        BlockPos basePos = this.blockPos;
        if (basePos == null || !this.canPlaceEggAt(basePos)) {
            this.entity.state().setEggBlockTargetPos(null);
            this.entity.state().setEggBlockPlacingCounter(0);
            return;
        }

        BlockPos eggPos = basePos.above();
        BlockState eggState = PinkScarfProfile.getSpeciesEggBlock()
                .defaultBlockState()
                .setValue(LuanluanEggBlock.EGGS, this.entity.state().getCarriedEggCount());
        Level level = this.entity.level();
        if (!level.setBlock(eggPos, eggState, 3)) {
            this.entity.state().setEggBlockTargetPos(null);
            this.entity.state().setEggBlockPlacingCounter(0);
            return;
        }

        level.playSound(null, eggPos, SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + level.random.nextFloat() * 0.2F);
        level.gameEvent(GameEvent.BLOCK_PLACE, eggPos, GameEvent.Context.of(this.entity, eggState));

        if (level instanceof ServerLevel serverLevel) {
            double radiusSquared = PinkScarfProfile.EGG_PLACED_NOTIFICATION_RADIUS
                    * PinkScarfProfile.EGG_PLACED_NOTIFICATION_RADIUS;
            Component message = Component.translatable(PinkScarfProfile.getPlacedMessageKey());
            for (ServerPlayer player : serverLevel.players()) {
                if (player.distanceToSqr(
                        eggPos.getX() + 0.5D,
                        eggPos.getY() + 0.5D,
                        eggPos.getZ() + 0.5D
                ) <= radiusSquared) {
                    player.sendSystemMessage(message);
                }
            }
        }

        this.entity.state().setHasCarriedEggBlock(this.entity, false);
        this.entity.setInLoveTime(600);
    }
}
