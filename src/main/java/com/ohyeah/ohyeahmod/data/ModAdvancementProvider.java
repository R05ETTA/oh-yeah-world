package com.ohyeah.ohyeahmod.data;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.advancement.ModAdvancementIds;
import com.ohyeah.ohyeahmod.registry.ModBlocks;
import com.ohyeah.ohyeahmod.registry.ModItems;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.function.Consumer;

/** 生成非线性的欧耶世界进度网络。 */
public final class ModAdvancementProvider implements AdvancementProvider.AdvancementGenerator {
    private static final ResourceLocation BACKGROUND = ResourceLocation.withDefaultNamespace(
            "textures/gui/advancements/backgrounds/adventure.png"
    );

    @Override
    public void generate(
            HolderLookup.Provider registries,
            Consumer<AdvancementHolder> saver,
            ExistingFileHelper existingFileHelper
    ) {
        AdvancementHolder root = Advancement.Builder.advancement()
                .display(
                        ModItems.CHIPS.get(),
                        title(ModAdvancementIds.ROOT),
                        description(ModAdvancementIds.ROOT),
                        BACKGROUND,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("root", impossible())
                .save(saver, ModAdvancementIds.id(ModAdvancementIds.ROOT), existingFileHelper);

        saveTask(saver, existingFileHelper, root, ModAdvancementIds.MEET_SCARF_LUO, ModItems.TIANSULUO_PINK_SCARF_EGG.get());
        saveTask(saver, existingFileHelper, root, ModAdvancementIds.MEET_BATTLE_FACE, ModItems.TIANSULUO_BATTLE_FACE_EGG.get());
        saveTask(saver, existingFileHelper, root, ModAdvancementIds.MEET_SUXIA, ModItems.SUXIA_EGG.get());
        saveTask(saver, existingFileHelper, root, ModAdvancementIds.FEED_GROW, Items.WHEAT);
        saveTask(saver, existingFileHelper, root, ModAdvancementIds.TAME_SCARF_LUO, ModItems.CHIPS.get());
        saveTask(saver, existingFileHelper, root, ModAdvancementIds.TAME_BATTLE_FACE, ModItems.CHIPS.get());
        saveTask(saver, existingFileHelper, root, ModAdvancementIds.OWNER_COMBAT, ModItems.CHIPS.get());
        saveTask(saver, existingFileHelper, root, ModAdvancementIds.SHEAR, Items.SHEARS);
        saveTask(saver, existingFileHelper, root, ModAdvancementIds.RESTORE_VOICE, Items.WHEAT);
        saveTask(saver, existingFileHelper, root, ModAdvancementIds.BREED_TIANSULUO, ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK.get());
        saveTask(saver, existingFileHelper, root, ModAdvancementIds.HATCH_LUANLUAN, ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK.get());
        saveTask(saver, existingFileHelper, root, ModAdvancementIds.BEDSIDE_PAIR, Items.RED_BED);

        Advancement.Builder.advancement()
                .parent(root)
                .display(
                        ModItems.SUXIA_EGG.get(),
                        title(ModAdvancementIds.COLLECT_SPECIES),
                        description(ModAdvancementIds.COLLECT_SPECIES),
                        null,
                        AdvancementType.CHALLENGE,
                        true,
                        true,
                        false
                )
                .addCriterion("scarf_luo", impossible())
                .addCriterion("battle_face", impossible())
                .addCriterion("suxia", impossible())
                .save(saver, ModAdvancementIds.id(ModAdvancementIds.COLLECT_SPECIES), existingFileHelper);
    }

    private static void saveTask(
            Consumer<AdvancementHolder> saver,
            ExistingFileHelper existingFileHelper,
            AdvancementHolder root,
            String id,
            ItemLike icon
    ) {
        Advancement.Builder.advancement()
                .parent(root)
                .display(
                        icon,
                        title(id),
                        description(id),
                        null,
                        AdvancementType.TASK,
                        true,
                        true,
                        false
                )
                .addCriterion("complete", impossible())
                .save(saver, ModAdvancementIds.id(id), existingFileHelper);
    }

    private static Component title(String id) {
        return Component.translatable("advancements." + OhYeah.MODID + "." + id + ".title");
    }

    private static Component description(String id) {
        return Component.translatable("advancements." + OhYeah.MODID + "." + id + ".description");
    }

    private static Criterion<ImpossibleTrigger.TriggerInstance> impossible() {
        return CriteriaTriggers.IMPOSSIBLE.createCriterion(new ImpossibleTrigger.TriggerInstance());
    }
}
