package com.ohyeah.ohyeahmod.entity.suxia;

import com.ohyeah.ohyeahmod.client.sound.ClientSoundManager;
import com.ohyeah.ohyeahmod.registry.ModSoundEvents;
import net.minecraft.sounds.SoundSource;

/** 素虾的客户端声音入口。 */
public final class SuxiaClientFeedback {
    public void tickClient(SuxiaEntity entity) {
        ClientSoundManager.playAmbient(entity, ModSoundEvents.SUXIA_AMBIENT.get());
    }

    public void handleClientEntityEvent(SuxiaEntity entity, byte status) {
        switch (status) {
            case SuxiaProfile.EVENT_HURT -> ClientSoundManager.playAction(entity, ModSoundEvents.SUXIA_HURT.get(), SoundSource.NEUTRAL, ClientSoundManager.PRIORITY_HURT, false, true);
            case SuxiaProfile.EVENT_DEATH -> ClientSoundManager.playDetachedAction(
                    entity,
                    ModSoundEvents.SUXIA_DEATH.get(),
                    SoundSource.NEUTRAL,
                    ClientSoundManager.PRIORITY_DEATH,
                    false
            );
            case SuxiaProfile.EVENT_LUANLUAN_SHOT -> ClientSoundManager.playAction(
                    entity,
                    ModSoundEvents.SUXIA_LUANLUAN_SHOT.get(),
                    SoundSource.NEUTRAL,
                    ClientSoundManager.PRIORITY_ATTACK_SHOT,
                    false
            );
            default -> {
            }
        }
    }
}
