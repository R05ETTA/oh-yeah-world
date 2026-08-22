package com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf;

import com.ohyeah.ohyeahmod.entity.tiansuluo.TiansuluoFoods;
import com.ohyeah.ohyeahmod.registry.ModBlocks;
import com.ohyeah.ohyeahmod.registry.ModItems;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

/**
 * 粉围巾的物种参数和资源入口。
 *
 * <p>这里放不会改变实体流程的物种常量；行为本身尽量交给 Minecraft 原生的
 * Animal、TamableAnimal、BreedGoal 和 RangedAttackGoal 处理。</p>
 */
public final class PinkScarfProfile {
    public static final String SPECIES_ID = "tiansuluo_pink_scarf";

    /** 成年实体尺寸；幼体尺寸由 AgeableMob 的原生缩放处理。 */
    public static final float WIDTH = 0.6F;
    public static final float HEIGHT = 1.8F;

    public static final double BASE_MAX_HEALTH = 16.0D;
    public static final double BASE_MOVEMENT_SPEED = 0.25D;
    public static final double BASE_ATTACK_DAMAGE = 3.0D;
    public static final double BASE_FOLLOW_RANGE = 24.0D;
    public static final int SPAWN_WEIGHT = 10;
    public static final int SPAWN_MIN_GROUP = 1;
    public static final int SPAWN_MAX_GROUP = 3;
    public static final List<String> SPAWN_BIOMES = List.of("minecraft:plains", "minecraft:meadow");

    /** Minecraft AgeableMob 使用负 tick 表示幼体。 */
    public static final int BABY_AGE_TICKS = -24000;
    /** 原生 Animal 在繁殖后使用的父母冷却时间。 */
    public static final int PARENT_BREEDING_COOLDOWN_TICKS = 6000;
    /** 喜欢食物一次推进的成长 tick 数；换算为 ageUp 时需要除以 20。 */
    public static final int LIKED_FOOD_GROWTH_TICKS = 6000;
    /** 到达合法地点后停留半秒再放置，避免移动中瞬间穿插。 */
    public static final int EGG_LAYING_TICKS = 10;
    /** 以携蛋实体为中心的水平寻址半径。 */
    public static final int EGG_SEARCH_RADIUS = 8;
    /** 失败寻址后的重试间隔。 */
    public static final int EGG_SEARCH_RETRY_TICKS = 5;
    /** 单次寻址/寻路最多持续 10 秒，失败后保留携蛋状态并换目标。 */
    public static final int EGG_MAX_TRAVEL_TICKS = 200;
    /** 已保存目标超过该距离时不再追逐。 */
    public static final double EGG_MAX_TARGET_DISTANCE = 10.0D;
    /** 每次繁殖只产一个幼体，避免一块栾栾块批量生成过多实体。 */
    public static final int EGGS_PER_BREEDING = 1;
    public static final int HATCH_CHANCE_INV = 500;
    public static final int AMBIENT_INTERVAL = 6000;
    public static final float FOOD_HEAL_AMOUNT = 4.0F;

    public static final List<String> FOOD_LIKED = List.of(
            "minecraft:wheat",
            "minecraft:carrot",
            "minecraft:beetroot",
            "minecraft:potato"
    );
    public static final List<String> FOOD_FAVORITE = List.of(
            "minecraft:cake",
            "ohyeah:chips"
    );
    public static final TiansuluoFoods FOODS = new TiansuluoFoods(FOOD_LIKED, FOOD_FAVORITE);

    /** 原生 RangedAttackGoal 的发射节奏；每次 performRangedAttack 发射一枚。 */
    public static final int RETALIATION_MEMORY_TICKS = 1200;
    public static final int ATTACK_DECLARE_TICKS = 40;
    public static final int BURST_SHOTS = 6;
    public static final int BURST_INTERVAL_TICKS = 10;
    public static final int BURST_COOLDOWN_TICKS = 60;
    public static final float RETALIATION_RANGE = 16.0F;
    public static final double RETALIATION_GOAL_SPEED = 1.25D;
    public static final double PROJECTILE_DAMAGE = 1.0D;
    public static final float PROJECTILE_SPEED = 1.6F;
    /** 远程反击的轻微散射角，保证连射仍然有命中反馈。 */
    public static final float PROJECTILE_INACCURACY = 3.0F;
    public static final double PROJECTILE_TARGET_EYE_OFFSET = 1.1D;
    public static final double PROJECTILE_MUZZLE_HEIGHT_RATIO = 0.25D;
    public static final double PROJECTILE_FRONT_OFFSET = 0.12D;

    /** 这些事件只用于客户端动作反馈，服务端行为不依赖事件本身。 */
    public static final byte EVENT_ATTACK_DECLARE = 60;
    public static final byte EVENT_GROW_UP = 61;
    public static final byte EVENT_SHEAR_REACT = 62;
    public static final byte EVENT_EAT = 63;
    public static final byte EVENT_EAT_FAVORITE = 64;
    public static final byte EVENT_ATTACK_SHOT = 65;
    public static final byte EVENT_ATTACK_END = 66;
    public static final byte EVENT_HURT = 67;
    public static final byte EVENT_DEATH = 68;
    public static final byte EVENT_BREED_SUCCESS = 69;

    private static final String CARRIED_MESSAGE_KEY = "message.ohyeah.tiansuluo_pink_scarf.luanluan_block_carried";

    private PinkScarfProfile() {
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, BASE_MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, BASE_MOVEMENT_SPEED)
                .add(Attributes.FOLLOW_RANGE, BASE_FOLLOW_RANGE)
                .add(Attributes.ATTACK_DAMAGE, BASE_ATTACK_DAMAGE);
    }


    public static String getCarriedMessageKey() {
        return CARRIED_MESSAGE_KEY;
    }

    public static Block getSpeciesEggBlock() {
        return ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK.get();
    }

    public static Item getSpeciesEggItem() {
        return ModItems.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK.get();
    }
}
