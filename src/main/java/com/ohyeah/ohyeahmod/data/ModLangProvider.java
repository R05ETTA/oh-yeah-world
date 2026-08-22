package com.ohyeah.ohyeahmod.data;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.registry.ModBlocks;
import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import com.ohyeah.ohyeahmod.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * 语言文件生成器 - 自动化翻译系统
 * 确保中英文拥有绝对对等的词条。
 */
public abstract class ModLangProvider extends LanguageProvider {

    public ModLangProvider(PackOutput output, String locale) {
        super(output, OhYeah.MODID, locale);
    }

    /**
     * 英文翻译
     */
    public static class English extends ModLangProvider {
        public English(PackOutput output) {
            super(output, "en_us");
        }

        @Override
        protected void addTranslations() {
            // Entities
            addEntityType(ModEntityTypes.TIANSULUO_PINK_SCARF, "Pink Scarf Tiansuluo");
            addEntityType(ModEntityTypes.TIANSULUO_BATTLE_FACE, "Battle Face Tiansuluo");
            addEntityType(ModEntityTypes.SUXIA, "Suxia");

            // Items
            addItem(ModItems.TIANSULUO_PINK_SCARF_EGG, "Pink Scarf Luanluan");
            add(ModItems.TIANSULUO_PINK_SCARF_EGG.get().getDescriptionId() + ".desc", "A special Luanluan of Pink Scarf Tiansuluo.");
            add(ModItems.TIANSULUO_PINK_SCARF_EGG.get().getDescriptionId() + ".desc_2", "Dropped by adults. Right-click to spawn.");

            addItem(ModItems.TIANSULUO_BATTLE_FACE_EGG, "Battle Face Luanluan");
            add(ModItems.TIANSULUO_BATTLE_FACE_EGG.get().getDescriptionId() + ".desc", "A special Luanluan of Battle Face Tiansuluo.");
            add(ModItems.TIANSULUO_BATTLE_FACE_EGG.get().getDescriptionId() + ".desc_2", "Dropped by adults. Right-click to spawn.");

            addItem(ModItems.SUXIA_EGG, "Suxia Luanluan");
            add(ModItems.SUXIA_EGG.get().getDescriptionId() + ".desc", "A special Luanluan of Suxia.");
            add(ModItems.SUXIA_EGG.get().getDescriptionId() + ".desc_2", "Right-click to spawn a Suxia.");

            addItem(ModItems.XIAMI_HUHU, "Xiami Huhu");
            addItem(ModItems.CHIPS, "Potato Chips");

            // Blocks
            addBlock(ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK, "Pink Scarf Luanluan Block");
            addBlock(ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK, "Battle Face Luanluan Block");

            // Creative Tab
            add("itemGroup.ohyeah.main", "Oh Yeah!");

            // Messages - Pink Scarf
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_carried", "Pink Scarf Tiansuluo is carrying a Luanluan Block and looking for a place to put it...");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_placed", "Pink Scarf Tiansuluo placed a Luanluan Block. Total blocks: %s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatch_progress", "Pink Scarf Luanluan hatch progress: Stage %s / %s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatched", "Pink Scarf Luanluan hatched! Spawned babies: %s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_broken", "You crushed a Pink Scarf Luanluan Block!");

            // Messages - Battle Face
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_carried", "Battle Face Tiansuluo is now carrying a Luanluan Block!");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_placed", "Battle Face Tiansuluo placed a Luanluan Block. Total: %s");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_hatch_progress", "Battle Face Luanluan hatch progress: Stage %s / %s");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_hatched", "Battle Face Luanluan hatched! Spawned: %s");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_broken", "You crushed a Battle Face Luanluan Block!");

            // Subtitles - Tiansuluo (Battle Face)
            add("subtitles.ohyeah.tiansuluo.ambient", "Battle Face Tiansuluo murmurs softly");
            add("subtitles.ohyeah.tiansuluo.hurt", "Battle Face Tiansuluo cries out");
            add("subtitles.ohyeah.tiansuluo.death", "Battle Face Tiansuluo collapses");
            add("subtitles.ohyeah.tiansuluo.eat", "Battle Face Tiansuluo is chewing");
            add("subtitles.ohyeah.tiansuluo.eat_favorite", "Battle Face Tiansuluo happily chews favorite food");
            add("subtitles.ohyeah.tiansuluo.breed_success", "Battle Face Tiansuluo chirps happily");
            add("subtitles.ohyeah.tiansuluo.attack_shot", "Battle Face Tiansuluo pounces!");
            add("subtitles.ohyeah.tiansuluo.attack_end", "Battle Face Tiansuluo ends pounce");
            add("subtitles.ohyeah.tiansuluo.attack_declare", "Battle Face Tiansuluo roars for battle");
            add("subtitles.ohyeah.tiansuluo.grow_up", "Little Battle Face Tiansuluo grows up");
            add("subtitles.ohyeah.tiansuluo.shear_react", "Battle Face Tiansuluo protests being sheared");

            // Subtitles - Tiansuluo (Pink Scarf)
            add("subtitles.ohyeah.tiansuluo_ps.ambient", "Pink Scarf Tiansuluo murmurs softly");
            add("subtitles.ohyeah.tiansuluo_ps.hurt", "Pink Scarf Tiansuluo cries out");
            add("subtitles.ohyeah.tiansuluo_ps.death", "Pink Scarf Tiansuluo collapses");
            add("subtitles.ohyeah.tiansuluo_ps.eat", "Pink Scarf Tiansuluo is chewing");
            add("subtitles.ohyeah.tiansuluo_ps.eat_favorite", "Pink Scarf Tiansuluo happily chews favorite food");
            add("subtitles.ohyeah.tiansuluo_ps.breed_success", "Pink Scarf Tiansuluo chirps happily");
            add("subtitles.ohyeah.tiansuluo_ps.attack_shot", "Pink Scarf Tiansuluo fires a projectile!");
            add("subtitles.ohyeah.tiansuluo_ps.attack_end", "Pink Scarf Tiansuluo stops firing");
            add("subtitles.ohyeah.tiansuluo_ps.attack_declare", "Pink Scarf Tiansuluo prepares to shoot");
            add("subtitles.ohyeah.tiansuluo_ps.grow_up", "Little Pink Scarf Tiansuluo grows up");
            add("subtitles.ohyeah.tiansuluo_ps.shear_react", "Pink Scarf Tiansuluo protests being sheared");

            // Subtitles - Suxia
            add("subtitles.ohyeah.suxia.ambient", "Suxia chirps softly");
            add("subtitles.ohyeah.suxia.hurt", "Suxia is hurt");
            add("subtitles.ohyeah.suxia.death", "Suxia dies");
            add("subtitles.ohyeah.suxia.squirt", "Suxia sprays ink");
        }
    }

    /**
     * 中文翻译
     */
    public static class Chinese extends ModLangProvider {
        public Chinese(PackOutput output) {
            super(output, "zh_cn");
        }

        @Override
        protected void addTranslations() {
            // Entities
            addEntityType(ModEntityTypes.TIANSULUO_PINK_SCARF, "天素罗_粉围巾");
            addEntityType(ModEntityTypes.TIANSULUO_BATTLE_FACE, "天素罗_战斗脸");
            addEntityType(ModEntityTypes.SUXIA, "素虾");

            // Items
            addItem(ModItems.TIANSULUO_PINK_SCARF_EGG, "栾栾_粉围巾");
            add(ModItems.TIANSULUO_PINK_SCARF_EGG.get().getDescriptionId() + ".desc", "天素罗_粉围巾的特殊栾栾");
            add(ModItems.TIANSULUO_PINK_SCARF_EGG.get().getDescriptionId() + ".desc_2", "成年天素罗_粉围巾掉落，右键生成天素罗_粉围巾");

            addItem(ModItems.TIANSULUO_BATTLE_FACE_EGG, "栾栾_战斗脸");
            add(ModItems.TIANSULUO_BATTLE_FACE_EGG.get().getDescriptionId() + ".desc", "天素罗_战斗脸的特殊栾栾");
            add(ModItems.TIANSULUO_BATTLE_FACE_EGG.get().getDescriptionId() + ".desc_2", "成年天素罗_战斗脸掉落，右键生成天素罗_战斗脸");

            addItem(ModItems.SUXIA_EGG, "栾栾_素虾");
            add(ModItems.SUXIA_EGG.get().getDescriptionId() + ".desc", "素虾的特殊栾栾");
            add(ModItems.SUXIA_EGG.get().getDescriptionId() + ".desc_2", "右键生成一只素虾");

            addItem(ModItems.XIAMI_HUHU, "虾米糊糊");
            addItem(ModItems.CHIPS, "薯片");

            // Blocks
            addBlock(ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK, "天素罗_粉围巾栾栾块");
            addBlock(ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK, "天素罗_战斗脸栾栾块");

            // Creative Tab
            add("itemGroup.ohyeah.main", "哦耶！");

            // Messages - Pink Scarf
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_carried", "天素罗_粉围巾已进入带蛋状态，会在自身附近寻找最近的合法位置放置栾栾块");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_placed", "天素罗_粉围巾已放置栾栾块，当前块数：%s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatch_progress", "粉围巾栾栾块孵化进度：第 %s / %s 阶段");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_hatched", "粉围巾栾栾块已孵化，诞生幼体数量：%s");
            add("message.ohyeah.tiansuluo_pink_scarf.luanluan_block_broken", "你踩碎了一枚天素罗_粉围巾栾栾块");

            // Messages - Battle Face
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_carried", "天素罗_战斗脸已进入带蛋状态，准备放下栾栾块！");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_placed", "天素罗_战斗脸已放置栾栾块，当前块数：%s");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_hatch_progress", "战斗脸栾栾块孵化进度：第 %s / %s 阶段");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_hatched", "战斗脸栾栾块已孵化，诞生幼体数量：%s");
            add("message.ohyeah.tiansuluo_battle_face.luanluan_block_broken", "你踩碎了一枚天素罗_战斗脸栾栾块");

            // Subtitles - Tiansuluo (Battle Face)
            add("subtitles.ohyeah.tiansuluo.ambient", "天素罗_战斗脸轻声呢喃");
            add("subtitles.ohyeah.tiansuluo.hurt", "天素罗_战斗脸发出痛叫");
            add("subtitles.ohyeah.tiansuluo.death", "天素罗_战斗脸瘫软倒下");
            add("subtitles.ohyeah.tiansuluo.eat", "天素罗_战斗脸在咀嚼");
            add("subtitles.ohyeah.tiansuluo.eat_favorite", "天素罗_战斗脸开心地吃着最爱的食物");
            add("subtitles.ohyeah.tiansuluo.breed_success", "天素罗_战斗脸发出欢快叫声");
            add("subtitles.ohyeah.tiansuluo.attack_shot", "天素罗_战斗脸发起了飞扑！");
            add("subtitles.ohyeah.tiansuluo.attack_end", "天素罗_战斗脸结束了扑击");
            add("subtitles.ohyeah.tiansuluo.attack_declare", "天素罗_战斗脸发出战斗怒吼");
            add("subtitles.ohyeah.tiansuluo.grow_up", "小天素罗_战斗脸长大了");
            add("subtitles.ohyeah.tiansuluo.shear_react", "天素罗_战斗脸抗议剪毛");

            // Subtitles - Tiansuluo (Pink Scarf)
            add("subtitles.ohyeah.tiansuluo_ps.ambient", "天素罗_粉围巾轻声呢喃");
            add("subtitles.ohyeah.tiansuluo_ps.hurt", "天素罗_粉围巾发出痛叫");
            add("subtitles.ohyeah.tiansuluo_ps.death", "天素罗_粉围巾瘫软倒下");
            add("subtitles.ohyeah.tiansuluo_ps.eat", "天素罗_粉围巾在咀嚼");
            add("subtitles.ohyeah.tiansuluo_ps.eat_favorite", "天素罗_粉围巾开心地吃着最爱的食物");
            add("subtitles.ohyeah.tiansuluo_ps.breed_success", "天素罗_粉围巾发出欢快叫声");
            add("subtitles.ohyeah.tiansuluo_ps.attack_shot", "天素罗_粉围巾喷射了光波！");
            add("subtitles.ohyeah.tiansuluo_ps.attack_end", "天素罗_粉围巾停止了射击");
            add("subtitles.ohyeah.tiansuluo_ps.attack_declare", "天素罗_粉围巾准备发射弹幕");
            add("subtitles.ohyeah.tiansuluo_ps.grow_up", "小天素罗_粉围巾长大了");
            add("subtitles.ohyeah.tiansuluo_ps.shear_react", "天素罗_粉围巾抗议剪毛");

            // Subtitles - Suxia
            add("subtitles.ohyeah.suxia.ambient", "素虾轻轻漫游");
            add("subtitles.ohyeah.suxia.hurt", "素虾受伤了");
            add("subtitles.ohyeah.suxia.death", "素虾倒下了");
            add("subtitles.ohyeah.suxia.squirt", "素虾喷出墨汁");
        }
    }
}
