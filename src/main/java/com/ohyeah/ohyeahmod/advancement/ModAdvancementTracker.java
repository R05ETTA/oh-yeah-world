package com.ohyeah.ohyeahmod.advancement;

import com.ohyeah.ohyeahmod.OhYeah;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

/** 服务端统一发放当前版本的非线性进度。 */
public final class ModAdvancementTracker {
    private static final double ENCOUNTER_RANGE = 12.0D;
    private static final double NEARBY_REWARD_RANGE = 16.0D;

    private ModAdvancementTracker() {
    }

    public static void award(ServerPlayer player, String advancementId) {
        award(player, advancementId, "complete");
    }

    public static void awardEncounter(ServerPlayer player, String advancementId, String collectionCriterion) {
        award(player, advancementId);
        award(player, ModAdvancementIds.COLLECT_SPECIES, collectionCriterion);
    }

    public static void award(ServerPlayer player, String advancementId, String criterion) {
        if (player == null) {
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        AdvancementHolder advancement = server.getAdvancements().get(ModAdvancementIds.id(advancementId));
        if (advancement == null) {
            return;
        }

        if (!advancement.value().criteria().containsKey(criterion)) {
            OhYeah.LOGGER.warn("Unknown criterion '{}' for advancement '{}'", criterion, advancementId);
            return;
        }

        if (!ModAdvancementIds.ROOT.equals(advancementId)) {
            AdvancementHolder root = server.getAdvancements().get(ModAdvancementIds.id(ModAdvancementIds.ROOT));
            if (root != null) {
                player.getAdvancements().award(root, "root");
            }
        }
        player.getAdvancements().award(advancement, criterion);
    }

    public static void checkEncounter(LivingEntity entity, String advancementId, String collectionCriterion) {
        if (!(entity.level() instanceof ServerLevel level) || entity.tickCount % 20 != 0) {
            return;
        }

        double rangeSquared = ENCOUNTER_RANGE * ENCOUNTER_RANGE;
        for (ServerPlayer serverPlayer : level.players()) {
            if (serverPlayer.distanceToSqr(entity) <= rangeSquared) {
                awardEncounter(serverPlayer, advancementId, collectionCriterion);
            }
        }
    }

    public static void awardNearby(ServerLevel level, BlockPos pos, String advancementId) {
        for (ServerPlayer player : level.players()) {
            double distance = player.distanceToSqr(
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D
            );
            if (distance <= NEARBY_REWARD_RANGE * NEARBY_REWARD_RANGE) {
                award(player, advancementId);
            }
        }
    }
}
