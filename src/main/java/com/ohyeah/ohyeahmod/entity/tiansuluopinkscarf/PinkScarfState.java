package com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf;

import com.ohyeah.ohyeahmod.entity.tiansuluo.RetaliationAnger;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import org.jetbrains.annotations.Nullable;

/**
 * 粉围巾真正需要跨客户端/存档保存的状态。
 *
 * <p>怒气和攻击计时属于短期服务端运行时，不写入这里；携蛋和剪后禁声
 * 才是会影响玩家后续操作、必须保存的状态。</p>
 */
public final class PinkScarfState {
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
    private int carriedEggCount = PinkScarfProfile.LUANLUAN_MIN_COUNT;
    private final RetaliationAnger retaliationAnger = new RetaliationAnger();

    public static void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(TiansuluoPinkScarfEntity.HAS_CARRIED_EGG_BLOCK, false);
        builder.define(TiansuluoPinkScarfEntity.IS_SILENCED, false);
    }

    public void addAdditionalSaveData(TiansuluoPinkScarfEntity entity, CompoundTag nbt) {
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

    public void readAdditionalSaveData(TiansuluoPinkScarfEntity entity, CompoundTag nbt) {
        this.setSilenced(entity, nbt.getBoolean(TAG_SILENCED));
        boolean carrying = nbt.getBoolean(TAG_HAS_CARRIED_EGG_BLOCK);
        this.setHasCarriedEggBlock(entity, carrying);
        if (carrying && nbt.contains(TAG_EGG_BLOCK_TARGET_X)) {
            this.eggBlockTargetPos = new BlockPos(
                    nbt.getInt(TAG_EGG_BLOCK_TARGET_X),
                    nbt.getInt(TAG_EGG_BLOCK_TARGET_Y),
                    nbt.getInt(TAG_EGG_BLOCK_TARGET_Z)
            );
        }
        this.eggBlockPlacingCounter = carrying ? Math.max(0, nbt.getInt(TAG_EGG_BLOCK_PLACING_COUNTER)) : 0;
        this.noticedPlayer = nbt.getBoolean(TAG_NOTICED_PLAYER);
        this.carriedEggCount = carrying
                ? this.clampEggCount(nbt.getInt(TAG_CARRIED_EGG_COUNT))
                : PinkScarfProfile.LUANLUAN_MIN_COUNT;
    }
    public RetaliationAnger retaliationAnger() {
        return this.retaliationAnger;
    }

    public void setSilenced(TiansuluoPinkScarfEntity entity, boolean silenced) {
        entity.getEntityData().set(TiansuluoPinkScarfEntity.IS_SILENCED, silenced);
    }

    public boolean isSilenced(TiansuluoPinkScarfEntity entity) {
        return entity.getEntityData().get(TiansuluoPinkScarfEntity.IS_SILENCED);
    }

    public boolean hasCarriedEggBlock(TiansuluoPinkScarfEntity entity) {
        return entity.getEntityData().get(TiansuluoPinkScarfEntity.HAS_CARRIED_EGG_BLOCK);
    }

    public void setHasCarriedEggBlock(TiansuluoPinkScarfEntity entity, boolean carrying) {
        entity.getEntityData().set(TiansuluoPinkScarfEntity.HAS_CARRIED_EGG_BLOCK, carrying);
        if (!carrying) {
            this.eggBlockTargetPos = null;
            this.eggBlockPlacingCounter = 0;
            this.carriedEggCount = PinkScarfProfile.LUANLUAN_MIN_COUNT;
        }
    }

    public @Nullable BlockPos getEggBlockTargetPos() {
        return this.eggBlockTargetPos;
    }

    public void setEggBlockTargetPos(@Nullable BlockPos targetPos) {
        this.eggBlockTargetPos = targetPos;
    }

    public int getEggBlockPlacingCounter() {
        return this.eggBlockPlacingCounter;
    }

    public void setEggBlockPlacingCounter(int counter) {
        this.eggBlockPlacingCounter = Math.max(0, counter);
    }
    public int getCarriedEggCount() {
        return this.carriedEggCount;
    }

    public void setCarriedEggCount(int count) {
        this.carriedEggCount = this.clampEggCount(count);
    }

    private int clampEggCount(int count) {
        return Math.max(PinkScarfProfile.LUANLUAN_MIN_COUNT,
                Math.min(PinkScarfProfile.LUANLUAN_MAX_COUNT, count));
    }

    public boolean hasNoticedPlayer() {
        return this.noticedPlayer;
    }

    public void setNoticedPlayer(boolean noticedPlayer) {
        this.noticedPlayer = noticedPlayer;
    }
}
