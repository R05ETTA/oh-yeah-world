package com.ohyeah.ohyeahmod.client.sound;

import com.ohyeah.ohyeahmod.entity.tiansuluobattleface.TiansuluoBattleFaceEntity;
import com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.TiansuluoPinkScarfEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Oh Yeah 自定义物种声音管理器。
 *
 * <p>这里只管理由 Oh Yeah 创建的物种声音实例，不接管 Minecraft 的世界声音。
 * 同一实体同时只保留一个自定义声音实例；高优先级声音可以打断低优先级声音。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ClientSoundManager {
    private static final Map<Integer, ActiveSound> ACTIVE_SOUNDS = new HashMap<>();
    private static final Map<Integer, Set<SoundInstance>> OVERLAY_SOUNDS = new HashMap<>();

    /** 最长环境音约 9.24 秒，环境声槽位保留 10 秒全局间隔。 */
    private static final long AMBIENT_GLOBAL_COOLDOWN_TICKS = 200L;
    private static long lastAmbientStartTick = Long.MIN_VALUE;

    /** 声音优先级按音频长度和交互重要性分层，避免长受伤音压住攻击链。 */
    public static final int PRIORITY_AMBIENT = 10;
    public static final int PRIORITY_HURT = 25;
    public static final int PRIORITY_ATTACK_DECLARE = 35;
    public static final int PRIORITY_GROW_UP = 40;
    public static final int PRIORITY_EAT = 60;
    public static final int PRIORITY_EAT_FAVORITE = 65;
    public static final int PRIORITY_ATTACK_SHOT = 70;
    public static final int PRIORITY_BREED_SUCCESS = 70;
    public static final int PRIORITY_ATTACK_END = 80;
    public static final int PRIORITY_SHEAR = 90;
    public static final int PRIORITY_DEATH = 100;

    /** 每一帧清理已经播放完毕的物种声音。 */
    public static void update() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            stopAll();
            return;
        }

        Iterator<Map.Entry<Integer, ActiveSound>> iterator = ACTIVE_SOUNDS.entrySet().iterator();
        while (iterator.hasNext()) {
            ActiveSound active = iterator.next().getValue();
            if (!mc.getSoundManager().isActive(active.instance())) {
                iterator.remove();
            }
        }

        Iterator<Map.Entry<Integer, Set<SoundInstance>>> overlayEntities = OVERLAY_SOUNDS.entrySet().iterator();
        while (overlayEntities.hasNext()) {
            Set<SoundInstance> overlays = overlayEntities.next().getValue();
            overlays.removeIf(instance -> !mc.getSoundManager().isActive(instance));
            if (overlays.isEmpty()) {
                overlayEntities.remove();
            }
        }
    }

    /**
     * 按物种环境音间隔尝试播放一次环境音。
     * 环境音由客户端本地调度，避免服务端为低价值声音持续广播事件。
     */
    public static void playAmbient(LivingEntity entity, SoundEvent sound, int interval) {
        if (interval <= 0 || entity.tickCount % interval != 0 || entity.getRandom().nextInt(3) != 0) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || isSilenced(entity)) {
            return;
        }

        long gameTime = mc.level.getGameTime();
        if (lastAmbientStartTick != Long.MIN_VALUE
                && gameTime - lastAmbientStartTick < AMBIENT_GLOBAL_COOLDOWN_TICKS) {
            return;
        }

        ActiveSound active = ACTIVE_SOUNDS.get(entity.getId());
        if (active != null
                && mc.getSoundManager().isActive(active.instance())
                && active.priority() > PRIORITY_AMBIENT) {
            return;
        }

        lastAmbientStartTick = gameTime;
        playAction(entity, sound, SoundSource.NEUTRAL, PRIORITY_AMBIENT, false);
    }

    /** 播放普通物种动作音。 */
    public static void playAction(LivingEntity entity, SoundEvent sound, SoundSource source) {
        playAction(entity, sound, source, 0, false);
    }

    /**
     * 播放物种声音。
     *
     * @param priority 越大越优先；低优先级声音不会打断正在播放的高优先级声音
     * @param allowWhenSilenced 禁声状态下是否仍允许播放
     */
    public static void playAction(
            LivingEntity entity,
            SoundEvent sound,
            SoundSource source,
            int priority,
            boolean allowWhenSilenced
    ) {
        if (sound == null || isSilenced(entity) && !allowWhenSilenced) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        ActiveSound active = ACTIVE_SOUNDS.get(entity.getId());
        if (active != null) {
            if (mc.getSoundManager().isActive(active.instance()) && active.priority() > priority) {
                return;
            }
            stopSound(entity);
        }

        EntityBoundSoundInstance instance = new EntityBoundSoundInstance(
                sound,
                source,
                1.0F,
                1.0F,
                entity,
                entity.level().getRandom().nextLong()
        );
        ACTIVE_SOUNDS.put(entity.getId(), new ActiveSound(instance, priority));
        mc.getSoundManager().play(instance);
    }

    /** 播放不占用实体主声音槽位的短特效音，例如素虾喷墨。 */
    public static void playOverlay(LivingEntity entity, SoundEvent sound, SoundSource source) {
        if (sound == null || isSilenced(entity)) {
            return;
        }
        EntityBoundSoundInstance instance = new EntityBoundSoundInstance(
                sound,
                source,
                1.0F,
                1.0F,
                entity,
                entity.level().getRandom().nextLong()
        );
        OVERLAY_SOUNDS.computeIfAbsent(entity.getId(), ignored -> new HashSet<>()).add(instance);
        Minecraft.getInstance().getSoundManager().play(instance);
    }

    public static void stopSound(LivingEntity entity) {
        Minecraft mc = Minecraft.getInstance();
        ActiveSound active = ACTIVE_SOUNDS.remove(entity.getId());
        if (active != null) {
            mc.getSoundManager().stop(active.instance());
        }
        Set<SoundInstance> overlays = OVERLAY_SOUNDS.remove(entity.getId());
        if (overlays != null) {
            for (SoundInstance overlay : overlays) {
                mc.getSoundManager().stop(overlay);
            }
        }
    }

    /** 清理世界切换或客户端退出时残留的所有物种声音。 */
    public static void stopAll() {
        Minecraft mc = Minecraft.getInstance();
        for (ActiveSound active : ACTIVE_SOUNDS.values()) {
            mc.getSoundManager().stop(active.instance());
        }
        for (Set<SoundInstance> overlays : OVERLAY_SOUNDS.values()) {
            for (SoundInstance overlay : overlays) {
                mc.getSoundManager().stop(overlay);
            }
        }
        ACTIVE_SOUNDS.clear();
        OVERLAY_SOUNDS.clear();
        lastAmbientStartTick = Long.MIN_VALUE;
    }

    private static boolean isSilenced(LivingEntity entity) {
        if (entity instanceof TiansuluoPinkScarfEntity pinkScarf) {
            return pinkScarf.state().isSilenced(pinkScarf);
        }
        if (entity instanceof TiansuluoBattleFaceEntity battleFace) {
            return battleFace.state().isSilenced(battleFace);
        }
        return false;
    }

    private record ActiveSound(SoundInstance instance, int priority) {
    }

    private ClientSoundManager() {
    }
}
