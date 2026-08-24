package com.ohyeah.ohyeahmod.advancement;

import com.ohyeah.ohyeahmod.OhYeah;
import net.minecraft.resources.ResourceLocation;

/** 当前版本进度 ID 的集中定义。 */
public final class ModAdvancementIds {
    public static final String ROOT = "root";
    public static final String MEET_SCARF_LUO = "meet_scarf_luo";
    public static final String MEET_BATTLE_FACE = "meet_battle_face";
    public static final String MEET_SUXIA = "meet_suxia";
    public static final String FEED_GROW = "feed_grow";
    public static final String TAME_SCARF_LUO = "tame_scarf_luo";
    public static final String TAME_BATTLE_FACE = "tame_battle_face";
    public static final String OWNER_COMBAT = "owner_combat";
    public static final String SHEAR = "shear";
    public static final String RESTORE_VOICE = "restore_voice";
    public static final String BREED_TIANSULUO = "breed_tiansuluo";
    public static final String HATCH_LUANLUAN = "hatch_luanluan";
    public static final String BEDSIDE_PAIR = "bedside_pair";
    public static final String COLLECT_SPECIES = "collect_species";
    public static final String SIT_COMPANION = "sit_companion";
    public static final String SMOKE_LUANLUAN = "smoke_luanluan";
    public static final String EAT_XIAMI_HUHU = "eat_xiami_huhu";
    public static final String FULL_LUANLUAN_NEST = "full_luanluan_nest";
    public static final String SUXIA_LUANLUAN_SHOT = "suxia_luanluan_shot";

    private ModAdvancementIds() {
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(OhYeah.MODID, path);
    }
}
