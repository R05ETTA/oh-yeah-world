package com.ohyeah.ohyeahmod.data;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.registry.ModSoundEvents;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.common.data.SoundDefinition;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/**
 * 声音定义文件生成器 (sounds.json)
 */
public final class ModSoundDefinitionsProvider extends SoundDefinitionsProvider {
    public ModSoundDefinitionsProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, OhYeah.MODID, existingFileHelper);
    }

    @Override
    public void registerSounds() {
        // --- Battle Face Tiansuluo ---
        add(ModSoundEvents.TIANSULUO_AMBIENT, definition().subtitle("subtitles.ohyeah.tiansuluo.ambient").with(sound("ohyeah:entity/tiansuluo/ambient_01")));
        add(ModSoundEvents.TIANSULUO_HURT, definition().subtitle("subtitles.ohyeah.tiansuluo.hurt").with(sound("ohyeah:entity/tiansuluo/hurt_01"), sound("ohyeah:entity/tiansuluo/hurt_02")));
        add(ModSoundEvents.TIANSULUO_DEATH, definition().subtitle("subtitles.ohyeah.tiansuluo.death").with(sound("ohyeah:entity/tiansuluo/death_01")));
        add(ModSoundEvents.TIANSULUO_EAT, definition().subtitle("subtitles.ohyeah.tiansuluo.eat").with(sound("ohyeah:entity/tiansuluo/eat_01")));
        add(ModSoundEvents.TIANSULUO_EAT_FAVORITE, definition().subtitle("subtitles.ohyeah.tiansuluo.eat_favorite").with(sound("ohyeah:entity/tiansuluo/eat_favorite_01")));
        add(ModSoundEvents.TIANSULUO_BREED_SUCCESS, definition().subtitle("subtitles.ohyeah.tiansuluo.breed_success").with(sound("ohyeah:entity/tiansuluo/breed_success_01")));
        add(ModSoundEvents.TIANSULUO_ATTACK_SHOT, definition().subtitle("subtitles.ohyeah.tiansuluo.attack_shot").with(sound("ohyeah:entity/tiansuluo/attack_shot_01")));
        add(ModSoundEvents.TIANSULUO_ATTACK_END, definition().subtitle("subtitles.ohyeah.tiansuluo.attack_end").with(sound("ohyeah:entity/tiansuluo/attack_end_01")));
        add(ModSoundEvents.TIANSULUO_ATTACK_DECLARE, definition().subtitle("subtitles.ohyeah.tiansuluo.attack_declare").with(sound("ohyeah:entity/tiansuluo/attack_declare_01")));
        add(ModSoundEvents.TIANSULUO_GROW_UP, definition().subtitle("subtitles.ohyeah.tiansuluo.grow_up").with(sound("ohyeah:entity/tiansuluo/breed_success_01")));
        add(ModSoundEvents.TIANSULUO_SHEAR_REACT, definition().subtitle("subtitles.ohyeah.tiansuluo.shear_react").with(sound("ohyeah:entity/tiansuluo/shear_react_01")));
        add(ModSoundEvents.TIANSULUO_NOTICE_PLAYER, definition().subtitle("subtitles.ohyeah.tiansuluo.notice_player").with(sound("ohyeah:entity/tiansuluo/notice_player_01")));

        // --- Pink Scarf Tiansuluo ---
        add(ModSoundEvents.TIANSULUO_PS_AMBIENT, definition().subtitle("subtitles.ohyeah.tiansuluo_ps.ambient").with(
                sound("ohyeah:entity/tiansuluo_pink_scarf/ambient_01"), sound("ohyeah:entity/tiansuluo_pink_scarf/ambient_02"), sound("ohyeah:entity/tiansuluo_pink_scarf/ambient_03"),
                sound("ohyeah:entity/tiansuluo_pink_scarf/ambient_04"), sound("ohyeah:entity/tiansuluo_pink_scarf/ambient_05"), sound("ohyeah:entity/tiansuluo_pink_scarf/ambient_06"),
                sound("ohyeah:entity/tiansuluo_pink_scarf/ambient_07"), sound("ohyeah:entity/tiansuluo_pink_scarf/ambient_08"), sound("ohyeah:entity/tiansuluo_pink_scarf/ambient_09"),
                sound("ohyeah:entity/tiansuluo_pink_scarf/ambient_10"), sound("ohyeah:entity/tiansuluo_pink_scarf/ambient_11"), sound("ohyeah:entity/tiansuluo_pink_scarf/ambient_12"),
                sound("ohyeah:entity/tiansuluo_pink_scarf/ambient_13")));
        add(ModSoundEvents.TIANSULUO_PS_HURT, definition().subtitle("subtitles.ohyeah.tiansuluo_ps.hurt").with(
                sound("ohyeah:entity/tiansuluo_pink_scarf/hurt_01"), sound("ohyeah:entity/tiansuluo_pink_scarf/hurt_02"), sound("ohyeah:entity/tiansuluo_pink_scarf/hurt_03"),
                sound("ohyeah:entity/tiansuluo_pink_scarf/hurt_04"), sound("ohyeah:entity/tiansuluo_pink_scarf/hurt_05"), sound("ohyeah:entity/tiansuluo_pink_scarf/hurt_06"),
                sound("ohyeah:entity/tiansuluo_pink_scarf/hurt_07"), sound("ohyeah:entity/tiansuluo_pink_scarf/hurt_08"), sound("ohyeah:entity/tiansuluo_pink_scarf/hurt_09"),
                sound("ohyeah:entity/tiansuluo_pink_scarf/hurt_10"), sound("ohyeah:entity/tiansuluo_pink_scarf/hurt_11"), sound("ohyeah:entity/tiansuluo_pink_scarf/hurt_12")));
        add(ModSoundEvents.TIANSULUO_PS_DEATH, definition().subtitle("subtitles.ohyeah.tiansuluo_ps.death").with(sound("ohyeah:entity/tiansuluo_pink_scarf/death_01")));
        add(ModSoundEvents.TIANSULUO_PS_EAT, definition().subtitle("subtitles.ohyeah.tiansuluo_ps.eat").with(sound("ohyeah:entity/tiansuluo_pink_scarf/eat_01")));
        add(ModSoundEvents.TIANSULUO_PS_EAT_FAVORITE, definition().subtitle("subtitles.ohyeah.tiansuluo_ps.eat_favorite").with(sound("ohyeah:entity/tiansuluo/eat_favorite_01"))); // 复用
        add(ModSoundEvents.TIANSULUO_PS_BREED_SUCCESS, definition().subtitle("subtitles.ohyeah.tiansuluo_ps.breed_success").with(sound("ohyeah:entity/tiansuluo_pink_scarf/breed_success_01")));
        add(ModSoundEvents.TIANSULUO_PS_ATTACK_SHOT, definition().subtitle("subtitles.ohyeah.tiansuluo_ps.attack_shot").with(sound("ohyeah:entity/tiansuluo/attack_shot_01"))); // 复用
        add(ModSoundEvents.TIANSULUO_PS_ATTACK_END, definition().subtitle("subtitles.ohyeah.tiansuluo_ps.attack_end").with(sound("ohyeah:entity/tiansuluo/attack_end_01"))); // 复用
        add(ModSoundEvents.TIANSULUO_PS_ATTACK_DECLARE, definition().subtitle("subtitles.ohyeah.tiansuluo_ps.attack_declare").with(sound("ohyeah:entity/tiansuluo/attack_declare_01"))); // 复用
        add(ModSoundEvents.TIANSULUO_PS_GROW_UP, definition().subtitle("subtitles.ohyeah.tiansuluo_ps.grow_up").with(sound("ohyeah:entity/tiansuluo_pink_scarf/grow_up_01")));
        add(ModSoundEvents.TIANSULUO_PS_SHEAR_REACT, definition().subtitle("subtitles.ohyeah.tiansuluo_ps.shear_react").with(sound("ohyeah:entity/tiansuluo/shear_react_01"))); // 复用

        // --- Suxia ---
        add(ModSoundEvents.SUXIA_AMBIENT, definition().subtitle("subtitles.ohyeah.suxia.ambient").with(sound("minecraft:entity.squid.ambient", SoundDefinition.SoundType.EVENT)));
        add(ModSoundEvents.SUXIA_HURT, definition().subtitle("subtitles.ohyeah.suxia.hurt").with(sound("minecraft:entity.squid.hurt", SoundDefinition.SoundType.EVENT)));
        add(ModSoundEvents.SUXIA_DEATH, definition().subtitle("subtitles.ohyeah.suxia.death").with(sound("minecraft:entity.squid.death", SoundDefinition.SoundType.EVENT)));
        add(ModSoundEvents.SUXIA_SQUIRT, definition().subtitle("subtitles.ohyeah.suxia.squirt").with(sound("minecraft:entity.squid.squirt", SoundDefinition.SoundType.EVENT)));
    }
}
