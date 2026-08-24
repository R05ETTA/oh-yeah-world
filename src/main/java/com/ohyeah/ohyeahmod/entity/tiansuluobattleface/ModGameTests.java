package com.ohyeah.ohyeahmod.entity.tiansuluobattleface;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.advancement.ModAdvancementIds;
import com.ohyeah.ohyeahmod.advancement.ModAdvancementTracker;
import com.ohyeah.ohyeahmod.block.LuanluanEggBlock;
import com.ohyeah.ohyeahmod.registry.ModBlocks;
import com.ohyeah.ohyeahmod.entity.suxia.SuxiaEntity;
import com.ohyeah.ohyeahmod.entity.suxia.SuxiaLuanluanProjectileEntity;
import com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.TiansuluoPinkScarfEntity;
import com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf.spawn.TiansuluoPinkScarfBedWakeSpawner;
import com.ohyeah.ohyeahmod.registry.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(OhYeah.MODID)
@PrefixGameTestTemplate(false)
public final class ModGameTests {
    private static final String TEMPLATE = "empty";

    private ModGameTests() {
    }

    @SuppressWarnings("removal")
    @GameTest(template = TEMPLATE, batch = "ohyeah_sitting")
    public static void ownerCanToggleSitting(GameTestHelper helper) {
        var owner = helper.makeMockServerPlayerInLevel();
        owner.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);

        TiansuluoPinkScarfEntity pinkScarf = helper.spawn(
                ModEntityTypes.TIANSULUO_PINK_SCARF.get(),
                new BlockPos(2, 1, 2)
        );
        pinkScarf.tame(owner);
        helper.assertFalse(pinkScarf.shouldTryTeleportToOwner(), "Scarf Luo should never use automatic owner teleport");
        helper.assertTrue(
                pinkScarf.mobInteract(owner, InteractionHand.MAIN_HAND).consumesAction(),
                "Owner empty-hand interaction should be consumed for Scarf Luo"
        );
        helper.assertTrue(pinkScarf.isOrderedToSit(), "Scarf Luo should be ordered to sit");
        pinkScarf.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertFalse(pinkScarf.isOrderedToSit(), "Second interaction should order Scarf Luo to stand");

        TiansuluoBattleFaceEntity battleFace = helper.spawn(
                ModEntityTypes.TIANSULUO_BATTLE_FACE.get(),
                new BlockPos(4, 1, 2)
        );
        battleFace.tame(owner);
        helper.assertFalse(battleFace.shouldTryTeleportToOwner(), "Battle Face should never use automatic owner teleport");
        helper.assertTrue(
                battleFace.mobInteract(owner, InteractionHand.MAIN_HAND).consumesAction(),
                "Owner empty-hand interaction should be consumed for Battle Face"
        );
        helper.assertTrue(battleFace.isOrderedToSit(), "Battle Face should be ordered to sit");
        battleFace.mobInteract(owner, InteractionHand.MAIN_HAND);
        helper.assertFalse(battleFace.isOrderedToSit(), "Second interaction should order Battle Face to stand");

        helper.succeed();
    }

    @SuppressWarnings("removal")
    @GameTest(template = TEMPLATE, batch = "ohyeah_notice", timeoutTicks = 40)
    public static void eachTiansuluoNoticesNearbyPlayerOnce(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();

        TiansuluoPinkScarfEntity pinkScarf = helper.spawn(
                ModEntityTypes.TIANSULUO_PINK_SCARF.get(),
                new BlockPos(2, 1, 2)
        );
        TiansuluoBattleFaceEntity battleFace = helper.spawn(
                ModEntityTypes.TIANSULUO_BATTLE_FACE.get(),
                new BlockPos(4, 1, 2)
        );
        player.teleportTo(pinkScarf.getX() + 2.0D, pinkScarf.getY(), pinkScarf.getZ());

        helper.runAtTickTime(20, () -> {
            helper.assertTrue(pinkScarf.state().hasNoticedPlayer(), "Scarf Luo should notice the nearby player");
            helper.assertTrue(battleFace.state().hasNoticedPlayer(), "Battle Face should notice the nearby player");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, batch = "ohyeah_breeding")
    public static void anyPlayerCanBreedTamedBattleFacesWithLikedFood(GameTestHelper helper) {
        var feeder = helper.makeMockPlayer(GameType.SURVIVAL);
        UUID ownerUuid = UUID.randomUUID();

        TiansuluoBattleFaceEntity first = helper.spawn(
                ModEntityTypes.TIANSULUO_BATTLE_FACE.get(),
                new BlockPos(2, 1, 2)
        );
        TiansuluoBattleFaceEntity second = helper.spawn(
                ModEntityTypes.TIANSULUO_BATTLE_FACE.get(),
                new BlockPos(4, 1, 2)
        );
        for (TiansuluoBattleFaceEntity entity : new TiansuluoBattleFaceEntity[]{first, second}) {
            entity.setTame(true, true);
            entity.setOwnerUUID(ownerUuid);
            entity.setAge(0);
            entity.setHealth(entity.getMaxHealth());
            feeder.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WHEAT));
            helper.assertTrue(
                    entity.mobInteract(feeder, InteractionHand.MAIN_HAND).consumesAction(),
                    "Liked food interaction should be consumed"
            );
            helper.assertTrue(entity.isInLove(), "Tamed Battle Face should enter love mode");
            helper.assertFalse(entity.isOwnedBy(feeder), "The feeding player must not need to be the owner");
        }

        helper.assertTrue(first.canMate(second), "Two fed, tamed Battle Faces should be able to mate");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "ohyeah_egg", timeoutTicks = 700)
    public static void luanluanBlockHatchesOnSchedule(GameTestHelper helper) {
        BlockPos eggPos = new BlockPos(2, 1, 2);
        var eggBlock = ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK.get();
        helper.setBlock(eggPos, eggBlock.defaultBlockState()
                .setValue(LuanluanEggBlock.EGGS, 4));

        helper.runAtTickTime(210, () ->
                helper.assertBlockProperty(eggPos, LuanluanEggBlock.HATCH, 1));
        helper.runAtTickTime(410, () ->
                helper.assertBlockProperty(eggPos, LuanluanEggBlock.HATCH, 2));
        helper.runAtTickTime(610, () -> {
            helper.assertBlockNotPresent(eggBlock, eggPos);
            int hatchedCount = helper.getLevel().getEntitiesOfClass(
                    TiansuluoBattleFaceEntity.class,
                    new AABB(helper.absolutePos(eggPos)).inflate(8.0D)
            ).size();
            helper.assertValueEqual(hatchedCount, 4, "A four-egg block should hatch four Battle Faces");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, batch = "ohyeah_suxia", timeoutTicks = 40)
    public static void suxiaFiresOnDeathWhenMobLootIsDisabled(GameTestHelper helper) {
        BlockPos suxiaPos = new BlockPos(2, 1, 2);
        BlockPos absolutePos = helper.absolutePos(suxiaPos);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = 0; dy <= 2; dy++) {
                for (int dz = -1; dz <= 1; dz++) {
                    helper.getLevel().setBlock(
                            absolutePos.offset(dx, dy, dz),
                            Blocks.WATER.defaultBlockState(),
                            3
                    );
                }
            }
        }

        SuxiaEntity suxia = helper.spawn(ModEntityTypes.SUXIA.get(), suxiaPos);
        suxia.setNoAi(true);
        suxia.setPos(absolutePos.getX() + 0.5D, absolutePos.getY() + 0.25D, absolutePos.getZ() + 0.5D);
        var attacker = helper.spawn(EntityType.COW, new BlockPos(7, 1, 2));
        helper.getLevel().getGameRules().getRule(GameRules.RULE_DOMOBLOOT)
                .set(false, helper.getLevel().getServer());
        helper.runAtTickTime(1, () -> {
            helper.assertTrue(suxia.isInWaterOrBubble(), "Suxia should be in water for the projectile test");
            suxia.hurt(suxia.damageSources().mobAttack(attacker), 1000.0F);
        });
        helper.runAtTickTime(2, () -> {
            int projectileCount = helper.getLevel().getEntitiesOfClass(
                    SuxiaLuanluanProjectileEntity.class,
                    new AABB(absolutePos).inflate(12.0D)
            ).size();
            helper.assertValueEqual(projectileCount, 1, "Suxia should fire one Luanluan on death");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE)
    public static void collectingAllSpeciesCompletesFinalAdvancement(GameTestHelper helper) {
        var player = helper.makeMockServerPlayerInLevel();
        ModAdvancementTracker.awardEncounter(player, ModAdvancementIds.MEET_SCARF_LUO, "scarf_luo");
        ModAdvancementTracker.awardEncounter(player, ModAdvancementIds.MEET_BATTLE_FACE, "battle_face");
        ModAdvancementTracker.awardEncounter(player, ModAdvancementIds.MEET_SUXIA, "suxia");

        var finalAdvancement = player.getServer().getAdvancements()
                .get(ModAdvancementIds.id(ModAdvancementIds.COLLECT_SPECIES));
        helper.assertTrue(
                player.getAdvancements().getOrStartProgress(finalAdvancement).isDone(),
                "Collecting all species should complete the final advancement"
        );
        helper.succeed();
    }

    @SuppressWarnings("removal")
    @GameTest(template = TEMPLATE, batch = "ohyeah_sleep_wake")
    public static void nearbyPinkScarfBlocksBedWakeSpawn(GameTestHelper helper) {
        BlockPos relativeBedPos = new BlockPos(3, 1, 3);
        helper.setBlock(relativeBedPos, Blocks.RED_BED);
        BlockPos bedPos = helper.absolutePos(relativeBedPos);
        var player = helper.makeMockServerPlayerInLevel();

        helper.assertTrue(
                TiansuluoPinkScarfBedWakeSpawner.INSTANCE.canSpawnAt(player, bedPos),
                "A bed without nearby Scarf Luo should allow the wake-up pair"
        );

        TiansuluoPinkScarfEntity nearby = helper.spawn(
                ModEntityTypes.TIANSULUO_PINK_SCARF.get(),
                new BlockPos(4, 1, 3)
        );
        helper.assertFalse(
                TiansuluoPinkScarfBedWakeSpawner.INSTANCE.canSpawnAt(player, bedPos),
                "Any living Scarf Luo within 16 blocks should block the wake-up pair"
        );

        nearby.discard();
        helper.assertTrue(
                TiansuluoPinkScarfBedWakeSpawner.INSTANCE.canSpawnAt(player, bedPos),
                "Removed Scarf Luo should no longer block the wake-up pair"
        );
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "ohyeah_sitting_combat", timeoutTicks = 160)
    public static void sittingBattleFaceTemporarilyStandsForCombat(GameTestHelper helper) {
        TiansuluoBattleFaceEntity battleFace = helper.spawn(
                ModEntityTypes.TIANSULUO_BATTLE_FACE.get(),
                new BlockPos(2, 1, 3)
        );
        var target = helper.spawn(EntityType.COW, new BlockPos(5, 1, 3));

        battleFace.setTame(true, true);
        battleFace.setOwnerUUID(UUID.randomUUID());
        battleFace.setOrderedToSit(true);

        helper.startSequence()
                .thenExecuteAfter(20, () -> {
                    helper.assertTrue(battleFace.isTame(), "Battle Face should be tamed for the sitting goal");
                    helper.assertTrue(battleFace.isOrderedToSit(), "Battle Face should retain the sitting order");
                    helper.assertTrue(battleFace.onGround(), "Battle Face should be on the structure floor");
                    helper.assertTrue(battleFace.isInSittingPose(), "Battle Face should initially obey the sitting order");
                    battleFace.beginOwnerRetaliation(target);
                })
                .thenExecuteAfter(10, () -> {
                    helper.assertTrue(battleFace.isOrderedToSit(), "Combat must preserve the sitting order");
                    helper.assertFalse(battleFace.isInSittingPose(), "Battle Face should stand while retaliating");
                })
                .thenExecuteAfter(80, () -> {
                    helper.assertTrue(battleFace.isOrderedToSit(), "The sitting order should still be preserved");
                    helper.assertTrue(battleFace.isInSittingPose(), "Battle Face should sit again after combat");
                })
                .thenSucceed();
    }
}
