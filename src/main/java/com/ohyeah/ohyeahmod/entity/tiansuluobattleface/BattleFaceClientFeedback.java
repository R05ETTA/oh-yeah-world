package com.ohyeah.ohyeahmod.entity.tiansuluobattleface;

import com.ohyeah.ohyeahmod.client.sound.ClientSoundManager;
import com.ohyeah.ohyeahmod.registry.ModSoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;

/**
 * 负责 Battle Face 的客户端视觉与听觉反馈。
 */
public final class BattleFaceClientFeedback {
    public void tickClient(TiansuluoBattleFaceEntity entity) {
        if (entity.state().hasCarriedEggBlock(entity) && !entity.isBaby() && entity.tickCount % 10 == 0) {
            entity.level().addParticle(ParticleTypes.HEART, entity.getRandomX(0.6D), entity.getRandomY() + 0.5D, entity.getRandomZ(0.6D), 0.0D, 0.02D, 0.0D);
        }
        ClientSoundManager.playAmbient(entity, ModSoundEvents.TIANSULUO_AMBIENT.get(), BattleFaceProfile.AMBIENT_INTERVAL);
    }

    public void handleClientEntityEvent(TiansuluoBattleFaceEntity entity, byte status) {
        if (entity.state().isSilenced(entity)
                && status != BattleFaceProfile.EVENT_SHEAR_REACT
                && status != BattleFaceProfile.EVENT_EAT
                && status != BattleFaceProfile.EVENT_EAT_FAVORITE) {
            return;
        }
        switch (status) {
            case BattleFaceProfile.EVENT_ATTACK_DECLARE -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_ATTACK_DECLARE.get(), SoundSource.NEUTRAL, 70, false);
            case BattleFaceProfile.EVENT_GROW_UP -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_GROW_UP.get(), SoundSource.NEUTRAL, 50, false);
            case BattleFaceProfile.EVENT_SHEAR_REACT -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_SHEAR_REACT.get(), SoundSource.NEUTRAL, 90, true);
            case BattleFaceProfile.EVENT_EAT -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_EAT.get(), SoundSource.NEUTRAL, 30, true);
            case BattleFaceProfile.EVENT_EAT_FAVORITE -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_EAT_FAVORITE.get(), SoundSource.NEUTRAL, 35, true);
            case BattleFaceProfile.EVENT_ATTACK_END -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_ATTACK_END.get(), SoundSource.NEUTRAL, 65, false);
            case BattleFaceProfile.EVENT_HURT -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_HURT.get(), SoundSource.NEUTRAL, 60, false);
            case BattleFaceProfile.EVENT_DEATH -> {
                ClientSoundManager.stopSound(entity);
                ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_DEATH.get(), SoundSource.NEUTRAL, 100, false);
            }
            case BattleFaceProfile.EVENT_BREED_SUCCESS -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_BREED_SUCCESS.get(), SoundSource.NEUTRAL, 50, false);
        }
    }
}
