package com.ohyeah.ohyeahmod.entity.tiansuluopinkscarf;

import com.ohyeah.ohyeahmod.advancement.ModAdvancementIds;
import com.ohyeah.ohyeahmod.advancement.ModAdvancementTracker;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.level.GameRules;

/**
 * 仿照 Turtle.TurtleBreedGoal：求偶仍使用原生 BreedGoal，成功后只把蛋块交给一方父母，
 * 不直接生成幼体实体。
 */
public final class PinkScarfBreedGoal extends BreedGoal {
    private final TiansuluoPinkScarfEntity pinkScarf;

    public PinkScarfBreedGoal(TiansuluoPinkScarfEntity pinkScarf, double speedModifier) {
        super(pinkScarf, speedModifier);
        this.pinkScarf = pinkScarf;
    }

    @Override
    public boolean canUse() {
        return !this.pinkScarf.state().hasCarriedEggBlock(this.pinkScarf) && super.canUse();
    }

    @Override
    protected void breed() {
        if (!(this.partner instanceof TiansuluoPinkScarfEntity partner)) {
            return;
        }

        ServerLevel level = (ServerLevel) this.level;
        ServerPlayer player = this.animal.getLoveCause();
        if (player == null) {
            player = partner.getLoveCause();
        }

        if (player != null) {
            ModAdvancementTracker.award(player, ModAdvancementIds.BREED_TIANSULUO);
            player.awardStat(Stats.ANIMALS_BRED);
            CriteriaTriggers.BRED_ANIMALS.trigger(player, this.animal, partner, null);
        }

        TiansuluoPinkScarfEntity carrier = this.animal.getRandom().nextBoolean()
                ? (TiansuluoPinkScarfEntity) this.animal
                : partner;
        carrier.state().setHasCarriedEggBlock(carrier, true);
        carrier.state().setEggBlockTargetPos(null);
        carrier.state().setEggBlockPlacingCounter(0);
        carrier.state().setEggBlockAttractedPlayer(player);

        this.animal.setAge(PinkScarfProfile.PARENT_BREEDING_COOLDOWN_TICKS);
        partner.setAge(PinkScarfProfile.PARENT_BREEDING_COOLDOWN_TICKS);
        this.animal.resetLove();
        partner.resetLove();
        level.broadcastEntityEvent(this.animal, (byte) 18);
        level.broadcastEntityEvent(carrier, PinkScarfProfile.EVENT_BREED_SUCCESS);
        if (player != null) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable(PinkScarfProfile.getCarriedMessageKey()),
                    true
            );
        }

        if (level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            level.addFreshEntity(new ExperienceOrb(
                    level,
                    this.animal.getX(),
                    this.animal.getY(),
                    this.animal.getZ(),
                    this.animal.getRandom().nextInt(7) + 1
            ));
        }
    }
}
