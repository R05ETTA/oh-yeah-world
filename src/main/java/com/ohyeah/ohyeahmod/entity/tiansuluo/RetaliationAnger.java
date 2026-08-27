package com.ohyeah.ohyeahmod.entity.tiansuluo;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * 服务端权威的受击怒气计量器。
 *
 * <p>怒气只参与“是否开始反击宣言”的判定，不依赖客户端声音播放状态，也不写入实体存档。
 * 受击次数和累计伤害都会增加怒气，长时间没有继续受击后逐渐衰减。</p>
 */
public final class RetaliationAnger {
    public static final int MAX_ANGER = 100;
    public static final int ROLL_THRESHOLD = 25;
    public static final int GUARANTEE_THRESHOLD = 75;
    public static final int GUARANTEE_HITS = 5;
    public static final float GUARANTEE_DAMAGE_RATIO = 0.50F;
    public static final float DAMAGE_DECAY_RATIO = 0.10F;
    public static final int DECAY_DELAY_TICKS = 100;
    public static final int DECAY_INTERVAL_TICKS = 20;
    public static final int DECAY_AMOUNT = 5;
    public static final float BASE_TRIGGER_CHANCE = 0.08F;
    public static final float ANGER_TRIGGER_CHANCE = 0.005F;
    public static final float LOW_HEALTH_TRIGGER_BONUS = 0.20F;
    public static final float LOW_HEALTH_ROLL_THRESHOLD = 0.25F;
    public static final float MAX_TRIGGER_CHANCE = 0.75F;

    private int anger;
    private int acceptedHits;
    private float damageSinceRetaliation;
    private long lastHurtTick = Long.MIN_VALUE;
    private long lastDecayTick = Long.MIN_VALUE;

    /** 记录一次已经被实体接受的伤害。 */
    public void recordHit(float damage, float maxHealth, long gameTime) {
        float safeMaxHealth = Math.max(1.0F, maxHealth);
        float safeDamage = Math.max(0.0F, damage);
        int damageGain = Math.min(25, Mth.ceil(safeDamage / safeMaxHealth * 40.0F));

        this.anger = Math.min(MAX_ANGER, this.anger + 10 + damageGain);
        this.acceptedHits = Math.min(GUARANTEE_HITS, this.acceptedHits + 1);
        this.damageSinceRetaliation = Math.min(
                safeMaxHealth,
                this.damageSinceRetaliation + safeDamage
        );
        this.lastHurtTick = gameTime;
        this.lastDecayTick = Long.MIN_VALUE;
    }

    /**
     * 按怒气、生命值和保底条件判定本次受击是否立即进入反击宣言。
     */
    public boolean shouldTrigger(float currentHealth, float maxHealth, RandomSource random) {
        float safeMaxHealth = Math.max(1.0F, maxHealth);
        if (this.anger >= GUARANTEE_THRESHOLD
                || this.acceptedHits >= GUARANTEE_HITS
                || this.damageSinceRetaliation >= safeMaxHealth * GUARANTEE_DAMAGE_RATIO) {
            return true;
        }
        float healthRatio = Mth.clamp(currentHealth / safeMaxHealth, 0.0F, 1.0F);
        if (this.anger < ROLL_THRESHOLD && healthRatio > LOW_HEALTH_ROLL_THRESHOLD) {
            return false;
        }

        float chance = Math.min(
                MAX_TRIGGER_CHANCE,
                BASE_TRIGGER_CHANCE
                        + this.anger * ANGER_TRIGGER_CHANCE
                        + (1.0F - healthRatio) * LOW_HEALTH_TRIGGER_BONUS
        );
        return random.nextFloat() < chance;
    }

    /** 怒气在一段时间没有继续受击后逐步下降。 */
    public void tick(long gameTime, float maxHealth) {
        if ((this.anger <= 0 && this.acceptedHits <= 0 && this.damageSinceRetaliation <= 0.0F)
                || this.lastHurtTick == Long.MIN_VALUE) {
            return;
        }

        long decayStartTick = this.lastHurtTick + DECAY_DELAY_TICKS;
        if (gameTime < decayStartTick) {
            return;
        }

        if (this.lastDecayTick == Long.MIN_VALUE) {
            this.applyDecay(1, maxHealth);
            this.lastDecayTick = decayStartTick;
        }

        long elapsed = gameTime - this.lastDecayTick;
        if (elapsed >= DECAY_INTERVAL_TICKS) {
            int steps = (int) Math.min(Integer.MAX_VALUE, elapsed / DECAY_INTERVAL_TICKS);
            this.applyDecay(steps, maxHealth);
            this.lastDecayTick += (long) steps * DECAY_INTERVAL_TICKS;
        }
    }

    public int anger() {
        return this.anger;
    }

    public int acceptedHits() {
        return this.acceptedHits;
    }

    public float damageSinceRetaliation() {
        return this.damageSinceRetaliation;
    }

    public void reset() {
        this.anger = 0;
        this.acceptedHits = 0;
        this.damageSinceRetaliation = 0.0F;
        this.lastHurtTick = Long.MIN_VALUE;
        this.lastDecayTick = Long.MIN_VALUE;
    }

    private void applyDecay(int steps, float maxHealth) {
        this.anger = Math.max(0, this.anger - steps * DECAY_AMOUNT);
        this.acceptedHits = Math.max(0, this.acceptedHits - steps);
        this.damageSinceRetaliation = Math.max(
                0.0F,
                this.damageSinceRetaliation - Math.max(1.0F, maxHealth) * DAMAGE_DECAY_RATIO * steps
        );
    }
}
