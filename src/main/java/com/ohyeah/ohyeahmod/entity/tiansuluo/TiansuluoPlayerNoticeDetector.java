package com.ohyeah.ohyeahmod.entity.tiansuluo;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

/** Detects the first nearby non-spectator player for Tiansuluo one-time reactions. */
public final class TiansuluoPlayerNoticeDetector {
    public static final double NOTICE_RANGE = 12.0D;

    private TiansuluoPlayerNoticeDetector() {
    }

    public static boolean seesPlayer(Mob entity) {
        AABB searchArea = entity.getBoundingBox().inflate(NOTICE_RANGE);
        return !entity.level().getEntitiesOfClass(
                Player.class,
                searchArea,
                player -> player.isAlive() && !player.isSpectator()
        ).isEmpty();
    }
}
