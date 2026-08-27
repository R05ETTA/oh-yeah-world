package com.ohyeah.ohyeahmod.entity.tiansuluobattleface;

import com.ohyeah.ohyeahmod.entity.tiansuluo.RetaliationAnger;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import org.jetbrains.annotations.Nullable;

/**
 * 战颜真正需要跨 tick、客户端同步或存档保存的状态。
 *
 * <p>怒气和反击计时只属于短期服务端运行时，不写入 NBT；携蛋、禁声和幼体成长检测
 * 才是实体生命周期的一部分。</p>
 */
public final class BattleFaceState {
    private static final String TAG_SILENCED = "SilencedByShears";
    private static final String TAG_HAS_CARRIED_EGG_BLOCK = "HasLuanluanBlock";
    private static final String TAG_EGG_BLOCK_TARGET_X = "LuanluanBlockTargetX";
    private static final String TAG_EGG_BLOCK_TARGET_Y = "LuanluanBlockTargetY";
    private static final String TAG_EGG_BLOCK_TARGET_Z = "LuanluanBlockTargetZ";
    private static final String TAG_EGG_BLOCK_PLACING_COUNTER = "LuanluanBlockPlacingCounter";
    private static final String TAG_NOTICED_PLAYER = "NoticedPlayer";
    private static final String TAG_CARRIED_EGG_COUNT = "CarriedEggCount";

    private @Nullable BlockPos eggBlockTargetPos;
    private int eggBlockPlacingCounter;
    private boolean noticedPlayer;
    private int carriedEggCount = BattleFaceProfile.LUANLUAN_MIN_COUNT;
    private int pounceCooldownTicks;
    private final RetaliationAnger retaliationAnger = new RetaliationAnger();
    private int retaliationDeclareTicksRemaining;
    private int retaliationTicksRemaining;
    private boolean retaliationDeclareStarted;
    private boolean wasBabyLastTick;

    public static void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TiansuluoBattleFaceEntity.HAS_CARRIED_EGG_BLOCK, false);
        builder.define(TiansuluoBattleFaceEntity.IS_SILENCED, false);
    }

    public void addAdditionalSaveData(TiansuluoBattleFaceEntity entity, CompoundTag nbt) {
        nbt.putBoolean(TAG_SILENCED, this.isSilenced(entity));
        nbt.putBoolean(TAG_HAS_CARRIED_EGG_BLOCK, this.hasCarriedEggBlock(entity));
        if (this.eggBlockTargetPos != null) {
            nbt.putInt(TAG_EGG_BLOCK_TARGET_X, this.eggBlockTargetPos.getX());
            nbt.putInt(TAG_EGG_BLOCK_TARGET_Y, this.eggBlockTargetPos.getY());
            nbt.putInt(TAG_EGG_BLOCK_TARGET_Z, this.eggBlockTargetPos.getZ());
        }
        nbt.putInt(TAG_EGG_BLOCK_PLACING_COUNTER, this.eggBlockPlacingCounter);
        nbt.putBoolean(TAG_NOTICED_PLAYER, this.noticedPlayer);
        nbt.putInt(TAG_CARRIED_EGG_COUNT, this.carriedEggCount);
    }

    public void readAdditionalSaveData(TiansuluoBattleFaceEntity entity, CompoundTag nbt) {
        this.setSilenced(entity, nbt.getBoolean(TAG_SILENCED));
        boolean carrying = nbt.getBoolean(TAG_HAS_CARRIED_EGG_BLOCK);
        this.setHasCarriedEggBlock(entity, carrying);
        if (carrying && nbt.contains(TAG_EGG_BLOCK_TARGET_X)
                && nbt.contains(TAG_EGG_BLOCK_TARGET_Y)
                && nbt.contains(TAG_EGG_BLOCK_TARGET_Z)) {
            this.eggBlockTargetPos = new BlockPos(
                    nbt.getInt(TAG_EGG_BLOCK_TARGET_X),
                    nbt.getInt(TAG_EGG_BLOCK_TARGET_Y),
                    nbt.getInt(TAG_EGG_BLOCK_TARGET_Z)
            );
        }
        this.eggBlockPlacingCounter = carrying
                ? Math.max(0, nbt.getInt(TAG_EGG_BLOCK_PLACING_COUNTER))
                : 0;
        this.noticedPlayer = nbt.getBoolean(TAG_NOTICED_PLAYER);
        this.carriedEggCount = carrying
                ? this.clampEggCount(nbt.getInt(TAG_CARRIED_EGG_COUNT))
                : BattleFaceProfile.LUANLUAN_MIN_COUNT;
    }

    public void setSilenced(TiansuluoBattleFaceEntity entity, boolean silenced) {
        entity.getEntityData().set(TiansuluoBattleFaceEntity.IS_SILENCED, silenced);
    }

    public boolean isSilenced(TiansuluoBattleFaceEntity entity) {
        return entity.getEntityData().get(TiansuluoBattleFaceEntity.IS_SILENCED);
    }

    public boolean hasCarriedEggBlock(TiansuluoBattleFaceEntity entity) {
        return entity.getEntityData().get(TiansuluoBattleFaceEntity.HAS_CARRIED_EGG_BLOCK);
    }

    public void setHasCarriedEggBlock(TiansuluoBattleFaceEntity entity, boolean has) {
        entity.getEntityData().set(TiansuluoBattleFaceEntity.HAS_CARRIED_EGG_BLOCK, has);
        if (!has) {
            this.eggBlockTargetPos = null;
            this.eggBlockPlacingCounter = 0;
            this.carriedEggCount = BattleFaceProfile.LUANLUAN_MIN_COUNT;
        }
    }

    public @Nullable BlockPos getEggBlockTargetPos() {
        return this.eggBlockTargetPos;
    }

    public void setEggBlockTargetPos(@Nullable BlockPos pos) {
        this.eggBlockTargetPos = pos;
    }

    public int getEggBlockPlacingCounter() {
        return this.eggBlockPlacingCounter;
    }

    public void setEggBlockPlacingCounter(int counter) {
        this.eggBlockPlacingCounter = Math.max(0, counter);
    }

    public int getPounceCooldownTicks() {
        return this.pounceCooldownTicks;
    }

    public void setPounceCooldownTicks(int ticks) {
        this.pounceCooldownTicks = Math.max(0, ticks);
    }

    public void decrementPounceCooldown() {
        if (this.pounceCooldownTicks > 0) this.pounceCooldownTicks--;
    }

    public RetaliationAnger retaliationAnger() {
        return this.retaliationAnger;
    }


    public boolean isRetaliationDeclareStarted() {
        return this.retaliationDeclareStarted;
    }

    public void setRetaliationDeclareStarted(boolean started) {
        this.retaliationDeclareStarted = started;
    }

    public int getRetaliationDeclareTicksRemaining() {
        return this.retaliationDeclareTicksRemaining;
    }

    public void setRetaliationDeclareTicksRemaining(int ticks) {
        this.retaliationDeclareTicksRemaining = Math.max(0, ticks);
    }

    public void decrementRetaliationDeclareTicks() {
        if (this.retaliationDeclareTicksRemaining > 0) this.retaliationDeclareTicksRemaining--;
    }

    public int getRetaliationTicksRemaining() {
        return this.retaliationTicksRemaining;
    }

    public void setRetaliationTicksRemaining(int ticks) {
        this.retaliationTicksRemaining = Math.max(0, ticks);
    }

    public void decrementRetaliationTicks() {
        if (this.retaliationTicksRemaining > 0) this.retaliationTicksRemaining--;
    }

    public boolean wasBabyLastTick() {
        return this.wasBabyLastTick;
    }

    public void setWasBabyLastTick(boolean wasBabyLastTick) {
        this.wasBabyLastTick = wasBabyLastTick;
    }
    public int getCarriedEggCount() {
        return this.carriedEggCount;
    }

    public void setCarriedEggCount(int count) {
        this.carriedEggCount = this.clampEggCount(count);
    }

    private int clampEggCount(int count) {
        return Math.max(BattleFaceProfile.LUANLUAN_MIN_COUNT,
                Math.min(BattleFaceProfile.LUANLUAN_MAX_COUNT, count));
    }

    public boolean hasNoticedPlayer() {
        return this.noticedPlayer;
    }

    public void setNoticedPlayer(boolean noticedPlayer) {
        this.noticedPlayer = noticedPlayer;
    }
}
