package com.ohyeah.ohyeahmod.entity.logic;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 全局睡眠联动协调器。
 * <p>
 * 已重构为“注册机模式 (Registry Pattern)”，彻底移除了硬编码的实体列表。
 * 新的唤醒生成器只需调用 register() 即可接入系统。
 */
public final class SleepWakeGameplayCoordinator {
    
    // --- 全局睡眠联动常量 ---
    public static final boolean SLEEP_WAKE_ENABLED = true;
    public static final int SLEEP_WAKE_RADIUS = 6;

    // 存储所有注册的睡眠唤醒处理器
    private static final Map<String, SleepWakeSpeciesHandler> HANDLERS = new LinkedHashMap<>();

    static {
        // [注册机] 在此注册所有的睡眠唤醒 Spawner
        register(com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.spawn.TiansuluoPinkScarfBedWakeSpawner.INSTANCE);
    }

    private SleepWakeGameplayCoordinator() {
    }

    /**
     * 注册一个新的睡眠唤醒处理器。
     */
    public static void register(SleepWakeSpeciesHandler handler) {
        HANDLERS.put(handler.speciesId(), handler);
    }

    public static boolean shouldQueueSpawn(ServerPlayer player) {
        if (player == null || !SLEEP_WAKE_ENABLED) {
            return false;
        }
        for (SleepWakeSpeciesHandler handler : getConfiguredHandlers()) {
            if (handler.shouldQueueSpawn(player)) {
                return true;
            }
        }
        return false;
    }

    public static void trySpawnAfterWake(ServerPlayer player, BlockPos bedPos) {
        if (player == null || bedPos == null || !SLEEP_WAKE_ENABLED) {
            return;
        }

        List<SleepWakeSpeciesHandler> candidates = new ArrayList<>();
        for (SleepWakeSpeciesHandler handler : getConfiguredHandlers()) {
            if (handler.canSpawnAt(player, bedPos)) {
                candidates.add(handler);
            }
        }
        if (candidates.isEmpty()) {
            return;
        }

        // 如果有多个满足条件的生成器，随机选择一个执行
        SleepWakeSpeciesHandler selected = candidates.get(player.getRandom().nextInt(candidates.size()));
        selected.trySpawn(player, bedPos);
    }

    private static List<SleepWakeSpeciesHandler> getConfiguredHandlers() {
        List<SleepWakeSpeciesHandler> handlers = new ArrayList<>();
        for (SleepWakeSpeciesHandler handler : HANDLERS.values()) {
            if (handler.isEnabled()) {
                handlers.add(handler);
            }
        }
        return handlers;
    }
}
