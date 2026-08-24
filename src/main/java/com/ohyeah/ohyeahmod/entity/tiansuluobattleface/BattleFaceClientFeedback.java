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
            case BattleFaceProfile.EVENT_NOTICE_PLAYER -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_NOTICE_PLAYER.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_NOTICE_PLAYER, false);
            case BattleFaceProfile.EVENT_ATTACK_DECLARE -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_ATTACK_DECLARE.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_ATTACK_DECLARE, false);
            case BattleFaceProfile.EVENT_GROW_UP -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_GROW_UP.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_GROW_UP, false);
            case BattleFaceProfile.EVENT_SHEAR_REACT -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_SHEAR_REACT.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_SHEAR, true);
            case BattleFaceProfile.EVENT_EAT -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_EAT.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_EAT, true);
            case BattleFaceProfile.EVENT_EAT_FAVORITE -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_EAT_FAVORITE.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_EAT_FAVORITE, true);
            case BattleFaceProfile.EVENT_ATTACK_END -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_ATTACK_END.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_ATTACK_END, false);
            case BattleFaceProfile.EVENT_HURT -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_HURT.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_HURT, false);
            case BattleFaceProfile.EVENT_DEATH -> {
                ClientSoundManager.stopSound(entity);
                ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_DEATH.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_DEATH, false);
            }
            case BattleFaceProfile.EVENT_BREED_SUCCESS -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_BREED_SUCCESS.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_BREED_SUCCESS, false);
        }
    }
}
