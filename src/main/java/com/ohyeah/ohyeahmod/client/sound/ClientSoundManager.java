package com.ohyeah.ohyeahmod.client.sound;

import com.ohyeah.ohyeahmod.entity.tiansuluobattleface.TiansuluoBattleFaceEntity;
import com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.TiansuluoPinkScarfEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * Oh Yeah 自定义物种声音管理器。
 *
 * <p>环境音使用每实体随机时钟、局部发言权、时长权重和短期记忆；动作音仍使用
 * 单实体主槽位和优先级抢占。只有严格更高优先级的声音可以打断当前声音。</p>
 */
@OnlyIn(Dist.CLIENT)
public final class ClientSoundManager {
    private static final Map<Integer, ActiveSound> ACTIVE_SOUNDS = new HashMap<>();
    private static final Map<Integer, Set<SoundInstance>> OVERLAY_SOUNDS = new HashMap<>();
    private static final Map<Integer, VoiceState> VOICE_STATES = new HashMap<>();
    private static final Map<ResourceLocation, AmbientCatalog> SINGLE_AMBIENT_CATALOGS = new HashMap<>();

    private static final int AMBIENT_CHECK_INTERVAL_TICKS = 10;
    private static final double LOCAL_AMBIENT_RADIUS_SQUARED = 16.0D * 16.0D;
    private static final int INITIAL_AMBIENT_DELAY_MIN = 100;
    private static final int INITIAL_AMBIENT_DELAY_MAX = 500;
    private static final int BLOCKED_AMBIENT_DELAY_MIN = 60;
    private static final int BLOCKED_AMBIENT_DELAY_MAX = 180;
    private static final int AMBIENT_REST_MIN = 360;
    private static final int AMBIENT_REST_MAX = 900;
    private static final int ACTION_REST_MIN = 160;
    private static final int ACTION_REST_MAX = 400;
    private static final int HURT_COOLDOWN_TICKS = 60;
    private static final int LOCAL_RECENT_MEMORY_TICKS = 1200;
    private static final int RECENT_AMBIENT_COUNT = 3;

    /** 声音优先级按交互重要性分层。 */
    public static final int PRIORITY_AMBIENT = 10;
    public static final int PRIORITY_HURT = 25;
    public static final int PRIORITY_ATTACK_DECLARE = 35;
    public static final int PRIORITY_NOTICE_PLAYER = 45;
    public static final int PRIORITY_GROW_UP = 40;
    public static final int PRIORITY_EAT = 60;
    public static final int PRIORITY_EAT_FAVORITE = 65;
    public static final int PRIORITY_ATTACK_SHOT = 70;
    public static final int PRIORITY_BREED_SUCCESS = 70;
    public static final int PRIORITY_ATTACK_END = 80;
    public static final int PRIORITY_SHEAR = 90;
    public static final int PRIORITY_DEATH = 100;

    /** 每一帧清理播放完毕的声音，并从实际结束时间开始安排下一次环境音。 */
    public static void update() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            stopAll();
            return;
        }

        long gameTime = mc.level.getGameTime();
        Iterator<Map.Entry<Integer, ActiveSound>> iterator = ACTIVE_SOUNDS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Integer, ActiveSound> entry = iterator.next();
            ActiveSound active = entry.getValue();
            if (!mc.getSoundManager().isActive(active.instance())) {
                iterator.remove();
                VoiceState state = VOICE_STATES.get(entry.getKey());
                if (state != null) {
                    state.nextAmbientTick = gameTime + (active.kind() == SoundKind.AMBIENT
                            ? state.scaledDelay(AMBIENT_REST_MIN, AMBIENT_REST_MAX)
                            : state.scaledDelay(ACTION_REST_MIN, ACTION_REST_MAX));
                }
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

        VOICE_STATES.entrySet().removeIf(entry ->
                !ACTIVE_SOUNDS.containsKey(entry.getKey()) && mc.level.getEntity(entry.getKey()) == null);
    }

    /** 使用单一声音事件构造自然环境音目录，适合战斗脸和素虾。 */
    public static void playAmbient(LivingEntity entity, SoundEvent sound) {
        if (sound == null) {
            return;
        }
        AmbientCatalog catalog = SINGLE_AMBIENT_CATALOGS.computeIfAbsent(
                sound.getLocation(),
                ignored -> new AmbientCatalog(
                        sound.getLocation().toString(),
                        List.of(new AmbientVoice(sound.getLocation().toString(), sound, 1.0D, false))
                )
        );
        playAmbient(entity, catalog);
    }

    /**
     * 自然环境音调度入口：随机时钟、16 格局部发言权、最近三条排除和 UUID 个性。
     */
    public static void playAmbient(LivingEntity entity, AmbientCatalog catalog) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || catalog == null || catalog.voices().isEmpty()) {
            return;
        }

        long gameTime = mc.level.getGameTime();
        VoiceState state = stateFor(entity, gameTime);
        if ((entity.tickCount + state.checkOffset) % AMBIENT_CHECK_INTERVAL_TICKS != 0
                || gameTime < state.nextAmbientTick) {
            return;
        }

        if (!entity.isAlive() || entity.isSilent() || isSilenced(entity)) {
            state.deferAmbient(gameTime);
            return;
        }

        ActiveSound current = ACTIVE_SOUNDS.get(entity.getId());
        if (current != null) {
            return;
        }

        if (hasNearbyActiveAmbient(entity, mc)) {
            state.deferAmbient(gameTime);
            return;
        }

        String nearbyRecent = findNearbyRecentAmbient(entity, catalog.id(), gameTime, mc);
        AmbientVoice selected = selectAmbientVoice(catalog, state, nearbyRecent);
        if (selected == null) {
            state.deferAmbient(gameTime);
            return;
        }

        EntityBoundSoundInstance instance = new EntityBoundSoundInstance(
                selected.sound(),
                SoundSource.NEUTRAL,
                1.0F,
                1.0F,
                entity,
                state.random.nextLong()
        );
        ACTIVE_SOUNDS.put(entity.getId(), new ActiveSound(
                instance,
                PRIORITY_AMBIENT,
                SoundKind.AMBIENT,
                catalog.id(),
                selected.repeatKey()
        ));
        state.rememberAmbient(catalog.id(), selected.repeatKey(), gameTime);
        state.nextAmbientTick = Long.MAX_VALUE;
        mc.getSoundManager().play(instance);
    }

    /** 播放普通物种动作音。 */
    public static void playAction(LivingEntity entity, SoundEvent sound, SoundSource source) {
        playAction(entity, sound, source, 0, false);
    }

    /**
     * 播放物种动作音。低或同优先级声音不会打断当前声音；受伤语音额外有 3 秒防抖。
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
        long gameTime = mc.level == null ? 0L : mc.level.getGameTime();
        VoiceState state = stateFor(entity, gameTime);
        if (priority == PRIORITY_HURT && gameTime - state.lastHurtTick < HURT_COOLDOWN_TICKS) {
            return;
        }

        ActiveSound active = ACTIVE_SOUNDS.get(entity.getId());
        if (active != null && mc.getSoundManager().isActive(active.instance())) {
            if (active.priority() >= priority) {
                return;
            }
            stopPrimarySound(entity, mc);
        }

        if (priority == PRIORITY_HURT) {
            state.lastHurtTick = gameTime;
        }
        EntityBoundSoundInstance instance = new EntityBoundSoundInstance(
                sound,
                source,
                1.0F,
                1.0F,
                entity,
                state.random.nextLong()
        );
        ACTIVE_SOUNDS.put(entity.getId(), new ActiveSound(instance, priority, SoundKind.ACTION, null, null));
        state.nextAmbientTick = Long.MAX_VALUE;
        mc.getSoundManager().play(instance);
    }

    /**
     * 播放不会随实体移除而中断的一次性声音，供死亡语音和栾栾块被破坏语音使用。
     */
    public static void playDetachedAction(
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
        long gameTime = mc.level == null ? 0L : mc.level.getGameTime();
        VoiceState state = stateFor(entity, gameTime);
        ActiveSound active = ACTIVE_SOUNDS.get(entity.getId());
        if (active != null && mc.getSoundManager().isActive(active.instance())) {
            if (active.priority() >= priority) {
                return;
            }
            stopPrimarySound(entity, mc);
        }

        SimpleSoundInstance instance = new SimpleSoundInstance(
                sound,
                source,
                1.0F,
                1.0F,
                SoundInstance.createUnseededRandom(),
                entity.getX(),
                entity.getY(),
                entity.getZ()
        );
        ACTIVE_SOUNDS.put(entity.getId(), new ActiveSound(instance, priority, SoundKind.ACTION, null, null));
        state.nextAmbientTick = Long.MAX_VALUE;
        mc.getSoundManager().play(instance);
    }

    /** 播放不占用实体主声音槽位的短特效音。 */
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
        stopPrimarySound(entity, mc);
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
        VOICE_STATES.clear();
        SINGLE_AMBIENT_CATALOGS.clear();
    }

    private static void stopPrimarySound(LivingEntity entity, Minecraft mc) {
        ActiveSound active = ACTIVE_SOUNDS.remove(entity.getId());
        if (active != null) {
            mc.getSoundManager().stop(active.instance());
        }
    }

    private static VoiceState stateFor(LivingEntity entity, long gameTime) {
        VoiceState state = VOICE_STATES.get(entity.getId());
        if (state == null || !state.entityUuid.equals(entity.getUUID())) {
            state = new VoiceState(entity.getUUID(), gameTime);
            VOICE_STATES.put(entity.getId(), state);
        }
        return state;
    }

    private static boolean hasNearbyActiveAmbient(LivingEntity entity, Minecraft mc) {
        for (Map.Entry<Integer, ActiveSound> entry : ACTIVE_SOUNDS.entrySet()) {
            ActiveSound active = entry.getValue();
            if (entry.getKey() == entity.getId() || active.kind() != SoundKind.AMBIENT) {
                continue;
            }
            Entity other = mc.level == null ? null : mc.level.getEntity(entry.getKey());
            if (other != null && entity.distanceToSqr(other) <= LOCAL_AMBIENT_RADIUS_SQUARED) {
                return true;
            }
        }
        return false;
    }

    private static String findNearbyRecentAmbient(
            LivingEntity entity,
            String catalogId,
            long gameTime,
            Minecraft mc
    ) {
        String recentKey = null;
        long recentTick = Long.MIN_VALUE;
        for (Map.Entry<Integer, VoiceState> entry : VOICE_STATES.entrySet()) {
            if (entry.getKey() == entity.getId()) {
                continue;
            }
            VoiceState state = entry.getValue();
            if (!catalogId.equals(state.lastAmbientCatalog)
                    || state.lastAmbientKey == null
                    || gameTime - state.lastAmbientTick > LOCAL_RECENT_MEMORY_TICKS
                    || state.lastAmbientTick <= recentTick) {
                continue;
            }
            Entity other = mc.level == null ? null : mc.level.getEntity(entry.getKey());
            if (other != null && entity.distanceToSqr(other) <= LOCAL_AMBIENT_RADIUS_SQUARED) {
                recentKey = state.lastAmbientKey;
                recentTick = state.lastAmbientTick;
            }
        }
        return recentKey;
    }

    private static AmbientVoice selectAmbientVoice(
            AmbientCatalog catalog,
            VoiceState state,
            String nearbyRecent
    ) {
        AmbientVoice selected = weightedSelect(catalog.voices(), state, nearbyRecent, true);
        if (selected == null) {
            selected = weightedSelect(catalog.voices(), state, nearbyRecent, false);
        }
        return selected;
    }

    private static AmbientVoice weightedSelect(
            List<AmbientVoice> voices,
            VoiceState state,
            String nearbyRecent,
            boolean excludeRecent
    ) {
        double totalWeight = 0.0D;
        for (AmbientVoice voice : voices) {
            if (excludeRecent && (state.recentAmbient.contains(voice.repeatKey())
                    || voice.repeatKey().equals(nearbyRecent))) {
                continue;
            }
            totalWeight += voice.effectiveWeight(state.longBias);
        }
        if (totalWeight <= 0.0D) {
            return null;
        }

        double choice = state.random.nextDouble() * totalWeight;
        for (AmbientVoice voice : voices) {
            if (excludeRecent && (state.recentAmbient.contains(voice.repeatKey())
                    || voice.repeatKey().equals(nearbyRecent))) {
                continue;
            }
            choice -= voice.effectiveWeight(state.longBias);
            if (choice <= 0.0D) {
                return voice;
            }
        }
        return voices.getLast();
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

    /** 单一逻辑环境音池中的一条候选音频。 */
    public record AmbientVoice(
            String repeatKey,
            SoundEvent sound,
            double baseWeight,
            boolean longVoice
    ) {
        public AmbientVoice {
            if (repeatKey == null || repeatKey.isBlank() || sound == null || baseWeight <= 0.0D) {
                throw new IllegalArgumentException("Invalid ambient voice entry");
            }
        }

        private double effectiveWeight(double longBias) {
            return this.baseWeight * (this.longVoice ? longBias : 1.0D);
        }
    }

    /** 一个逻辑环境音池；不会按台词语义拆成多个池。 */
    public record AmbientCatalog(String id, List<AmbientVoice> voices) {
        public AmbientCatalog {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Ambient catalog id cannot be blank");
            }
            voices = List.copyOf(voices);
        }
    }

    private enum SoundKind {
        AMBIENT,
        ACTION
    }

    private record ActiveSound(
            SoundInstance instance,
            int priority,
            SoundKind kind,
            String ambientCatalog,
            String ambientKey
    ) {
    }

    private static final class VoiceState {
        private final UUID entityUuid;
        private final Random random;
        private final double gapScale;
        private final double longBias;
        private final int checkOffset;
        private final Deque<String> recentAmbient = new ArrayDeque<>();
        private long nextAmbientTick;
        private long lastHurtTick = Long.MIN_VALUE / 2L;
        private String lastAmbientCatalog;
        private String lastAmbientKey;
        private long lastAmbientTick = Long.MIN_VALUE;

        private VoiceState(UUID uuid, long gameTime) {
            this.entityUuid = uuid;
            long seed = mix64(uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits());
            this.random = new Random(seed);
            this.gapScale = 0.75D + unitDouble(mix64(seed + 0x9E3779B97F4A7C15L)) * 0.75D;
            this.longBias = 0.70D + unitDouble(mix64(seed + 0xD1B54A32D192ED03L)) * 0.60D;
            this.checkOffset = Math.floorMod((int) seed, AMBIENT_CHECK_INTERVAL_TICKS);
            this.nextAmbientTick = gameTime + this.scaledDelay(
                    INITIAL_AMBIENT_DELAY_MIN,
                    INITIAL_AMBIENT_DELAY_MAX
            );
        }

        private int scaledDelay(int minInclusive, int maxInclusive) {
            int base = minInclusive + this.random.nextInt(maxInclusive - minInclusive + 1);
            return Math.max(1, (int) Math.round(base * this.gapScale));
        }

        private void deferAmbient(long gameTime) {
            this.nextAmbientTick = gameTime + this.scaledDelay(
                    BLOCKED_AMBIENT_DELAY_MIN,
                    BLOCKED_AMBIENT_DELAY_MAX
            );
        }

        private void rememberAmbient(String catalogId, String repeatKey, long gameTime) {
            this.recentAmbient.remove(repeatKey);
            this.recentAmbient.addFirst(repeatKey);
            while (this.recentAmbient.size() > RECENT_AMBIENT_COUNT) {
                this.recentAmbient.removeLast();
            }
            this.lastAmbientCatalog = catalogId;
            this.lastAmbientKey = repeatKey;
            this.lastAmbientTick = gameTime;
        }
    }

    private static long mix64(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static double unitDouble(long value) {
        return (double) (value >>> 11) * 0x1.0p-53;
    }

    private ClientSoundManager() {
    }
}
