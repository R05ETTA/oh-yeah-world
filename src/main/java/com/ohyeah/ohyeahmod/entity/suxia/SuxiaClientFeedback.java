package com.ohyeah.ohyeahmod.entity.suxia;

import com.ohyeah.ohyeahmod.client.sound.ClientSoundManager;
import com.ohyeah.ohyeahmod.registry.ModSoundEvents;
import net.minecraft.sounds.SoundSource;

/** 素虾的客户端声音入口。 */
public final class SuxiaClientFeedback {
    public void tickClient(SuxiaEntity entity) {
        ClientSoundManager.playAmbient(entity, ModSoundEvents.SUXIA_AMBIENT.get(), SuxiaProfile.AMBIENT_INTERVAL);
    }

    public void handleClientEntityEvent(SuxiaEntity entity, byte status) {
        switch (status) {
            case SuxiaProfile.EVENT_HURT -> ClientSoundManager.playAction(
                    entity,
                    ModSoundEvents.SUXIA_HURT.get(),
                    SoundSource.NEUTRAL,
                    60,
                    false
            );
            case SuxiaProfile.EVENT_DEATH -> {
                ClientSoundManager.stopSound(entity);
                ClientSoundManager.playAction(
                        entity,
                        ModSoundEvents.SUXIA_DEATH.get(),
                        SoundSource.NEUTRAL,
                        100,
                        false
                );
            }
            case SuxiaProfile.EVENT_SQUIRT -> ClientSoundManager.playOverlay(
                    entity,
                    ModSoundEvents.SUXIA_SQUIRT.get(),
                    SoundSource.NEUTRAL
            );
            default -> {
            }
        }
    }
}
