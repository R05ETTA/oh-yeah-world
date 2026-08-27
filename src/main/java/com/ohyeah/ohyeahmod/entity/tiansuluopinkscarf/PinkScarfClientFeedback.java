package com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf;

import com.ohyeah.ohyeahmod.client.sound.ClientSoundManager;
import com.ohyeah.ohyeahmod.client.sound.PinkScarfAmbientCatalog;
import com.ohyeah.ohyeahmod.registry.ModSoundEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;

/** 粉围巾的客户端粒子和自定义物种声音反馈。 */
public final class PinkScarfClientFeedback {
    public void tickClient(TiansuluoPinkScarfEntity entity) {
        if (entity.state().hasCarriedEggBlock(entity) && !entity.isBaby() && entity.tickCount % 10 == 0) {
            entity.level().addParticle(
                    ParticleTypes.HEART,
                    entity.getRandomX(0.6D),
                    entity.getRandomY() + 0.5D,
                    entity.getRandomZ(0.6D),
                    0.0D,
                    0.02D,
                    0.0D
            );
        }
        ClientSoundManager.playAmbient(entity, PinkScarfAmbientCatalog.get());
    }

    /** 服务端实体事件统一进入物种声音管理器。 */
    public void handleClientEntityEvent(TiansuluoPinkScarfEntity entity, byte status) {
        if (entity.state().isSilenced(entity)
                && status != PinkScarfProfile.EVENT_SHEAR_REACT
                && status != PinkScarfProfile.EVENT_EAT
                && status != PinkScarfProfile.EVENT_EAT_FAVORITE) {
            return;
        }
        switch (status) {
            case PinkScarfProfile.EVENT_NOTICE_PLAYER -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_NOTICE_PLAYER.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_NOTICE_PLAYER, false);
            case PinkScarfProfile.EVENT_ATTACK_DECLARE -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_PS_ATTACK_DECLARE.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_ATTACK_DECLARE, false);
            case PinkScarfProfile.EVENT_GROW_UP -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_PS_GROW_UP.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_GROW_UP, false);
            case PinkScarfProfile.EVENT_SHEAR_REACT -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_PS_SHEAR_REACT.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_SHEAR, true);
            case PinkScarfProfile.EVENT_EAT -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_PS_EAT.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_EAT, true);
            case PinkScarfProfile.EVENT_EAT_FAVORITE -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_PS_EAT_FAVORITE.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_EAT_FAVORITE, true);
            case PinkScarfProfile.EVENT_ATTACK_SHOT -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_PS_ATTACK_SHOT.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_ATTACK_SHOT, false);
            case PinkScarfProfile.EVENT_ATTACK_END -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_PS_ATTACK_END.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_ATTACK_END, false);
            case PinkScarfProfile.EVENT_HURT -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_PS_HURT.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_HURT, false, true);
            case PinkScarfProfile.EVENT_DEATH -> ClientSoundManager.playDetachedAction(
                    entity,
                    ModSoundEvents.TIANSULUO_PS_DEATH.get(),
                    SoundSource.NEUTRAL,
                    ClientSoundManager.PRIORITY_DEATH,
                    false
            );
            case PinkScarfProfile.EVENT_LUANLUAN_BLOCK_BROKEN -> ClientSoundManager.playDetachedAction(
                    entity,
                    ModSoundEvents.TIANSULUO_PS_LUANLUAN_BLOCK_BROKEN.get(),
                    SoundSource.NEUTRAL,
                    ClientSoundManager.PRIORITY_DEATH,
                    false
            );
            case PinkScarfProfile.EVENT_BREED_SUCCESS -> ClientSoundManager.playAction(entity, ModSoundEvents.TIANSULUO_PS_BREED_SUCCESS.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_BREED_SUCCESS, false);
            default -> {
            }
        }
    }
}
