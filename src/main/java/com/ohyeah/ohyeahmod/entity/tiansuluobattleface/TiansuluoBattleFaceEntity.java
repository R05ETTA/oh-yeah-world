package com.ohyeah.ohyeahmod.entity.tiansuluobattleface;

import com.ohyeah.ohyeahmod.advancement.ModAdvancementIds;
import com.ohyeah.ohyeahmod.advancement.ModAdvancementTracker;
import com.ohyeah.ohyeahmod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.Nullable;

/**
 * 战颜实体。
 *
 * <p>战斗规则只有一条：被有效攻击或响应主人直接攻击的目标后，在短暂宣言后靠近并扑击；不主动
 * 追击，也不会自行寻找未被主人或自身攻击的目标。</p>
 */
public class TiansuluoBattleFaceEntity extends TamableAnimal {
    public static final EntityDataAccessor<Boolean> HAS_CARRIED_EGG_BLOCK =
            SynchedEntityData.defineId(TiansuluoBattleFaceEntity.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_SILENCED =
            SynchedEntityData.defineId(TiansuluoBattleFaceEntity.class, EntityDataSerializers.BOOLEAN);

    private final BattleFaceState state = new BattleFaceState();
    private final BattleFaceBehavior behavior = new BattleFaceBehavior();
    private final BattleFaceClientFeedback feedback = new BattleFaceClientFeedback();
    private final BattleFaceRetaliationController retaliation = new BattleFaceRetaliationController();
    private final BattleFaceShearInteraction shear = new BattleFaceShearInteraction();

    public TiansuluoBattleFaceEntity(EntityType<? extends TiansuluoBattleFaceEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static boolean canSpawn(
            EntityType<? extends Animal> type,
            LevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random
    ) {
        return Animal.checkAnimalSpawnRules(type, level, spawnType, pos, random);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return BattleFaceProfile.createAttributes();
    }

    @Override
    protected void registerGoals() {
        new BattleFaceGoalRegistrar().registerGoals(this);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        BattleFaceState.defineSynchedData(builder);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        this.state.addAdditionalSaveData(this, nbt);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag nbt) {
        super.readAdditionalSaveData(nbt);
        this.state.readAdditionalSaveData(this, nbt);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            this.feedback.tickClient(this);
            return;
        }
        ModAdvancementTracker.checkEncounter(this, ModAdvancementIds.MEET_BATTLE_FACE, "battle_face");

        if (this.state.wasBabyLastTick() && !this.isBaby()) {
            this.level().broadcastEntityEvent(this, BattleFaceProfile.EVENT_GROW_UP);
        }
        this.state.setWasBabyLastTick(this.isBaby());

        this.state.decrementPounceCooldown();
        this.retaliation.tick(this);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && !this.level().isClientSide && this.isAlive()) {
            this.level().broadcastEntityEvent(this, BattleFaceProfile.EVENT_HURT);
        }
        this.retaliation.onHurt(this, hurt, source);
        return hurt;
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    @Nullable
    protected SoundEvent getHurtSound(DamageSource source) {
        return null;
    }

    @Override
    @Nullable
    protected SoundEvent getDeathSound() {
        return null;
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!this.level().isClientSide && this.isDeadOrDying()) {
            this.level().broadcastEntityEvent(this, BattleFaceProfile.EVENT_DEATH);
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (this.isBaby()) return;
        this.spawnAtLocation(ModItems.CHIPS.get());
        this.spawnAtLocation(ModItems.TIANSULUO_BATTLE_FACE_EGG.get());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        InteractionResult shearResult = this.shear.handleShear(this, player, hand, stack);
        if (shearResult.consumesAction()) {
            this.damageShearsIfNeeded(player, hand, stack);
            return shearResult;
        }

        InteractionResult behaviorResult = this.behavior.handleMobInteract(this, player, hand);
        if (behaviorResult.consumesAction()) return behaviorResult;

        return super.mobInteract(player, hand);
    }

    @Override
    public void handleEntityEvent(byte status) {
        if (this.level().isClientSide) {
            this.feedback.handleClientEntityEvent(this, status);
        }
        super.handleEntityEvent(status);
    }

    @Override
    public void spawnChildFromBreeding(ServerLevel level, Animal partner) {
        this.behavior.spawnChildFromBreeding(this, level, partner);
    }

    @Override
    public boolean canMate(Animal other) {
        return this.behavior.canMate(this, other);
    }

    /** 产卵块繁殖不直接生成子代；Animal.spawnChildFromBreeding 对空子代结果安全处理。 */
    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob other) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return BattleFaceProfile.FOODS.isFood(stack);
    }

    public BattleFaceState state() {
        return this.state;
    }

    void beginOwnerRetaliation(LivingEntity target) {
        this.retaliation.onOwnerHurtTarget(this, target);
    }

    public void consumeInteractionItem(Player player, InteractionHand hand, ItemStack stack) {
        this.usePlayerItem(player, hand, stack);
    }

    private void damageShearsIfNeeded(Player player, InteractionHand hand, ItemStack stack) {
        if (!this.level().isClientSide) {
            stack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND
                    ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                    : net.minecraft.world.entity.EquipmentSlot.OFFHAND);
        }
    }
}
