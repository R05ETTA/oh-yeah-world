package com.ohyeah.ohyeahmod.entity.tiansuluobattleface;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.EnumSet;

/**
 * 战颜的快速产卵目标。
 *
 * <p>搜索以实体当前位置为中心，并按距离由近到远检查地表；不再把喂食玩家
 * 当作目的地，也不再每个 tick 做大量随机试探。超过短时限仍找不到位置时，
 * 沿用原有的当前位置放置/掉落兜底。</p>
 */
public final class BattleFaceLayEggGoal extends Goal {
    private static final int SEARCH_RETRY_TICKS = 5;

    private final TiansuluoBattleFaceEntity entity;
    private final int layingTicks;
    private int timeSearching;
    private int searchCooldown;

    public BattleFaceLayEggGoal(TiansuluoBattleFaceEntity entity, int layingTicks) {
        this.entity = entity;
        this.layingTicks = layingTicks;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return this.entity.state().hasCarriedEggBlock(this.entity) && !this.entity.isBaby();
    }

    @Override
    public void start() {
        this.timeSearching = 0;
        this.searchCooldown = 0;
        this.entity.state().setEggBlockPlacingCounter(0);
    }

    @Override
    public void stop() {
        this.entity.getNavigation().stop();
        this.entity.state().setEggBlockPlacingCounter(0);
    }

    @Override
    public void tick() {
        if (!this.entity.state().hasCarriedEggBlock(this.entity) || this.entity.isBaby()) {
            return;
        }

        this.timeSearching++;
        BlockPos target = this.entity.state().getEggBlockTargetPos();
        if (target == null || !this.canPlaceEggAt(target)) {
            if (this.searchCooldown > 0) {
                this.searchCooldown--;
            } else {
                target = this.findSuitableLayingSpot();
                this.entity.state().setEggBlockTargetPos(target);
                this.searchCooldown = SEARCH_RETRY_TICKS;
            }
        }

        if (target != null && this.timeSearching < BattleFaceProfile.EGG_MAX_SEARCH_TICKS) {
            double distSq = this.entity.distanceToSqr(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D);
            if (distSq > 2.25D) {
                this.entity.getNavigation().moveTo(target.getX() + 0.5D, target.getY() + 0.5D, target.getZ() + 0.5D, 1.0D);
            } else {
                this.performLayingTick();
            }
        } else if (this.timeSearching >= BattleFaceProfile.EGG_MAX_SEARCH_TICKS) {
            this.tryFinalLaying();
        }
    }

    private BlockPos findSuitableLayingSpot() {
        Level level = this.entity.level();
        BlockPos center = this.entity.blockPosition();

        // 由近到远扫描方形环，优先选择实体脚下或附近的位置。
        for (int radius = 0; radius <= BattleFaceProfile.EGG_SEARCH_RADIUS; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                BlockPos found = this.trySurfaceColumn(level, center.getX() + dx, center.getZ() - radius);
                if (found != null) return found;
                if (radius > 0) {
                    found = this.trySurfaceColumn(level, center.getX() + dx, center.getZ() + radius);
                    if (found != null) return found;
                }
            }
            for (int dz = -radius + 1; dz <= radius - 1; dz++) {
                BlockPos found = this.trySurfaceColumn(level, center.getX() - radius, center.getZ() + dz);
                if (found != null) return found;
                if (radius > 0) {
                    found = this.trySurfaceColumn(level, center.getX() + radius, center.getZ() + dz);
                    if (found != null) return found;
                }
            }
        }
        return null;
    }

    private BlockPos trySurfaceColumn(Level level, int x, int z) {
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1;
        for (int y = surface - 1; y <= surface + 1; y++) {
            BlockPos eggPos = new BlockPos(x, y + 1, z);
            if (this.canPlaceEggAt(eggPos) && this.entity.isWithinRestriction(eggPos)) {
                return eggPos;
            }
        }
        return null;
    }

    private boolean canPlaceEggAt(BlockPos eggPos) {
        Level level = this.entity.level();
        return level.isEmptyBlock(eggPos)
                && level.getFluidState(eggPos).isEmpty()
                && level.getBlockState(eggPos.below()).isFaceSturdy(level, eggPos.below(), net.minecraft.core.Direction.UP);
    }

    private void performLayingTick() {
        this.entity.getNavigation().stop();
        int counter = this.entity.state().getEggBlockPlacingCounter() + 1;
        this.entity.state().setEggBlockPlacingCounter(counter);
        if (counter >= this.layingTicks) {
            this.placeEggBlock(this.entity.state().getEggBlockTargetPos());
        }
    }

    private void tryFinalLaying() {
        BlockPos currentPos = this.entity.blockPosition();
        if (this.canPlaceEggAt(currentPos)) {
            this.placeEggBlock(currentPos);
        } else {
            if (!this.entity.level().isClientSide) {
                this.entity.spawnAtLocation(BattleFaceProfile.getSpeciesEggItem());
            }
            this.finishLaying();
        }
    }

    private void placeEggBlock(BlockPos pos) {
        if (pos == null || this.entity.level().isClientSide || !this.canPlaceEggAt(pos)) {
            return;
        }

        BlockState eggState = BattleFaceProfile.getSpeciesEggBlock().defaultBlockState();
        Level level = this.entity.level();
        if (!level.setBlock(pos, eggState, 3)) {
            return;
        }
        level.playSound(null, pos, SoundEvents.TURTLE_LAY_EGG, SoundSource.BLOCKS, 0.3F, 0.9F + level.random.nextFloat() * 0.2F);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(this.entity, eggState));
        this.finishLaying();
    }

    private void finishLaying() {
        this.entity.state().setHasCarriedEggBlock(this.entity, false);
        this.entity.setInLoveTime(600);
        this.entity.getNavigation().stop();
    }
}
