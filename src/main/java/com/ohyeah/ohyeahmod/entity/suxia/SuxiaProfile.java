package com.ohyeah.ohyeahmod.entity.suxia;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.List;

public final class SuxiaProfile {
    public static final String SPECIES_ID = "suxia";
    public static final float WIDTH = 0.6F;
    public static final float HEIGHT = 0.6F;
    public static final double BASE_MAX_HEALTH = 10.0D;
    public static final double BASE_MOVEMENT_SPEED = 0.25D;
    public static final double BASE_FOLLOW_RANGE = 16.0D;

    public static final int SPAWN_WEIGHT = 8;
    public static final int SPAWN_MIN_GROUP = 1;
    public static final int SPAWN_MAX_GROUP = 2;
    public static final List<String> DEFAULT_SPAWN_BIOMES = List.of("#minecraft:is_ocean", "#minecraft:is_river");

    /** 原版鱿鱼环境音为短音频；6 秒尝试间隔避免刷音。 */
    public static final int AMBIENT_INTERVAL = 120;

    public static final byte EVENT_HURT = 60;
    public static final byte EVENT_DEATH = 61;
    public static final byte EVENT_SQUIRT = 62;
    private SuxiaProfile() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, BASE_MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, BASE_FOLLOW_RANGE);
    }
}
