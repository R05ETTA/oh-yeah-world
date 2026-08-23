package com.ohyeah.ohyeahmod.entity.tiansuluobattleface;

import com.ohyeah.ohyeahmod.entity.tiansuluo.TiansuluoFoods;
import com.ohyeah.ohyeahmod.registry.ModBlocks;
import com.ohyeah.ohyeahmod.registry.ModItems;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

public final class BattleFaceProfile {
    public static final String SPECIES_ID = "tiansuluo_battle_face";
    public static final double BASE_MAX_HEALTH = 24.0D;
    public static final double BASE_MOVEMENT_SPEED = 0.28D;
    public static final double BASE_ATTACK_DAMAGE = 4.0D;
    public static final double BASE_FOLLOW_RANGE = 24.0D;

    public static final float TARGET_ADULT_WIDTH = 0.7F;
    public static final float TARGET_ADULT_HEIGHT = 1.2F;
    public static final float BABY_SCALE_FACTOR = 0.5F;

    public static final int HATCH_CHANCE_INV = 500;
    /** 环境音最长约 3.48 秒；6 秒尝试间隔保留播放余量。 */
    public static final int AMBIENT_INTERVAL = 120;
    /** 原生 Animal 繁殖后的父母冷却时间。 */
    public static final int PARENT_BREEDING_COOLDOWN_TICKS = 6000;
    public static final int FOOD_GROWTH_STEP = 6000;
    public static final float FOOD_HEAL_AMOUNT = 4.0F;
    /** 到达合法地点后停留半秒再放置。 */
    public static final int EGG_LAYING_TICKS = 10;
    /** 以战颜当前位置为中心的水平寻址半径。 */
    public static final int EGG_SEARCH_RADIUS = 8;
    /** 单次寻址/寻路最多持续 5 秒，失败后沿用原有掉落兜底。 */
    public static final int EGG_MAX_SEARCH_TICKS = 100;
    public static final int SPAWN_WEIGHT = 1;
    public static final int SPAWN_MIN_GROUP = 1;
    public static final int SPAWN_MAX_GROUP = 1;
    public static final List<String> SPAWN_BIOMES = List.of("minecraft:plains", "minecraft:meadow");

    public static final List<String> FOOD_LIKED = List.of("minecraft:wheat", "minecraft:carrot", "minecraft:beetroot", "minecraft:potato");
    public static final List<String> FOOD_FAVORITE = List.of("ohyeah:chips");
    public static final TiansuluoFoods FOODS = new TiansuluoFoods(FOOD_LIKED, FOOD_FAVORITE);

    /** 飞扑冷却时间。 */
    public static final int POUNCE_COOLDOWN = 40;
    /** 反击宣告时长。 */
    public static final int RETALIATION_DECLARE_TICKS = 40;
    /** 反击总时长。 */
    public static final int RETALIATION_TOTAL_TICKS = 100;
    /** 野生战颜每轮扑击后保留仇恨的时间；驯化战颜不使用连续模式。 */
    public static final int RETALIATION_MEMORY_TICKS = 1200;

    /** 飞扑水平最大初速度。 */
    public static final double POUNCE_HORIZONTAL_SPEED = 1.3D;
    /** 目标身体中心的低跃扑击初速度，避免 0.95 格以上的高抛。 */
    public static final double POUNCE_VERTICAL_SPEED = 0.45D;
    /** 飞扑最长飞行刻。 */
    public static final int POUNCE_MAX_FLIGHT_TICKS = 12;
    /** 受击后最多保留该距离内的攻击目标。 */
    public static final double RETALIATION_MAX_RANGE_SQUARED = 24.0D * 24.0D;
    /** 进入实际扑击的近身范围平方（4 格）。 */
    public static final double POUNCE_WINDOW_SQUARED = 16.0D;
    /** 预测目标两 tick 的水平位移，降低目标移动造成的扑空。 */
    public static final double POUNCE_TARGET_LEAD_TICKS = 2.0D;
    /** hitbox 现在与模型主体匹配，保留 0.30 格碰撞余量。 */
    public static final double POUNCE_HITBOX_PADDING = 0.3D;
    /** 进入攻击宣告动作。 */
    public static final byte EVENT_ATTACK_DECLARE = 60;
    /** 幼体成长为成体。 */
    public static final byte EVENT_GROW_UP = 61;
    /** 被剪毛后的反应。 */
    public static final byte EVENT_SHEAR_REACT = 62;
    /** 吃普通喜欢食物。 */
    public static final byte EVENT_EAT = 63;
    /** 吃最喜欢食物。 */
    public static final byte EVENT_EAT_FAVORITE = 64;
    /** 反击成功结束。 */
    public static final byte EVENT_ATTACK_END = 66;
    /** 受伤。 */
    public static final byte EVENT_HURT = 67;
    /** 死亡。 */
    public static final byte EVENT_DEATH = 68;
    /** 繁殖成功。 */
    public static final byte EVENT_BREED_SUCCESS = 69;

    private static final String CARRIED_MESSAGE_KEY = "message.ohyeah.tiansuluo_battle_face.luanluan_block_carried";

    private BattleFaceProfile() {
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
        return ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK.get();
    }

    public static Item getSpeciesEggItem() {
        return ModItems.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK.get();
    }
}
