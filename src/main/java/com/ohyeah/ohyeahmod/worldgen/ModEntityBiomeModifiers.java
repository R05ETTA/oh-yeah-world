package com.ohyeah.ohyeahmod.worldgen;

import com.ohyeah.ohyeahmod.entity.suxia.SuxiaProfile;
import com.ohyeah.ohyeahmod.entity.tiansuluobattleface.BattleFaceProfile;
import com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.PinkScarfProfile;
import java.util.List;

/**
 * 集中管理所有生物群系修改（如自然生成规则）。
 */
public final class ModEntityBiomeModifiers {
    private ModEntityBiomeModifiers() {}

    /**
     * 定义所有自然生成计划。
     * 格式：[计划唯一标识, 物种ID, 权重, 最小群组, 最大群组, 生物群系列表]
     */
    public static List<NaturalSpawnPlan> naturalSpawns() {
        return List.of(
                new NaturalSpawnPlan(
                        "tiansuluo_pink_scarf_spawn",
                        PinkScarfProfile.SPECIES_ID,
                        PinkScarfProfile.SPAWN_WEIGHT,
                        PinkScarfProfile.SPAWN_MIN_GROUP,
                        PinkScarfProfile.SPAWN_MAX_GROUP,
                        PinkScarfProfile.SPAWN_BIOMES
                ),
                new NaturalSpawnPlan(
                        "tiansuluo_battle_face_spawn",
                        BattleFaceProfile.SPECIES_ID,
                        BattleFaceProfile.SPAWN_WEIGHT,
                        BattleFaceProfile.SPAWN_MIN_GROUP,
                        BattleFaceProfile.SPAWN_MAX_GROUP,
                        BattleFaceProfile.SPAWN_BIOMES
                ),
                new NaturalSpawnPlan(
                        "suxia_spawn",
                        SuxiaProfile.SPECIES_ID,
                        SuxiaProfile.SPAWN_WEIGHT,
                        SuxiaProfile.SPAWN_MIN_GROUP,
                        SuxiaProfile.SPAWN_MAX_GROUP,
                        SuxiaProfile.DEFAULT_SPAWN_BIOMES
                )
        );
    }

    public static String summary() {
        return "已配置 " + naturalSpawns().size() + " 个生成计划";
    }

    /**
     * 内部记录类，用于 Datagen 将计划转换为 JSON 格式。
     */
    public record NaturalSpawnPlan(
            String planId,
            String speciesId,
            int weight,
            int minGroup,
            int maxGroup,
            List<String> biomes
    ) {
        @Override
        public String toString() {
            return "计划：" + planId + "（物种：" + speciesId 
                    + "，权重：" + weight 
                    + "，群组 " + minGroup + "-" + maxGroup 
                    + "，群系 " + String.join(", ", biomes) + "）";
        }
    }
}
