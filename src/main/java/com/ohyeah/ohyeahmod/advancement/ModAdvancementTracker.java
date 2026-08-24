package com.ohyeah.ohyeahmod.advancement;

import com.ohyeah.ohyeahmod.OhYeah;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.chat.Component;
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
        if (player == null) {
            return;
        }

        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }

        AdvancementHolder collection = server.getAdvancements().get(ModAdvancementIds.id(ModAdvancementIds.COLLECT_SPECIES));
        if (collection == null) {
            return;
        }

        boolean wasComplete = player.getAdvancements().getOrStartProgress(collection).isDone();
        award(player, ModAdvancementIds.COLLECT_SPECIES, collectionCriterion);
        if (!wasComplete && player.getAdvancements().getOrStartProgress(collection).isDone()) {
            player.displayClientMessage(
                    Component.translatable("message.ohyeah.collect_species_complete"),
                    true
            );
        }
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

        awardParentChain(player, server, advancement);
        player.getAdvancements().award(advancement, criterion);
    }

    private static void awardParentChain(ServerPlayer player, MinecraftServer server, AdvancementHolder advancement) {
        AdvancementHolder current = advancement;
        while (current.value().parent().isPresent()) {
            AdvancementHolder parent = server.getAdvancements().get(current.value().parent().get());
            if (parent == null) {
                return;
            }
            String criterion = parent.value().criteria().containsKey(ModAdvancementIds.ROOT)
                    ? ModAdvancementIds.ROOT
                    : "complete";
            if (parent.value().criteria().containsKey(criterion)) {
                player.getAdvancements().award(parent, criterion);
            }
            current = parent;
        }
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
