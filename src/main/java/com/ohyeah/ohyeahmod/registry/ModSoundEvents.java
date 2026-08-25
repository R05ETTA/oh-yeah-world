package com.ohyeah.ohyeahmod.registry;

import com.ohyeah.ohyeahmod.OhYeah;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;
public final class ModSoundEvents {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, OhYeah.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_AMBIENT = register("tiansuluo.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_HURT = register("tiansuluo.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_DEATH = register("tiansuluo.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_LUANLUAN_BLOCK_BROKEN = register("tiansuluo.luanluan_block_broken");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_EAT = register("tiansuluo.eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_EAT_FAVORITE = register("tiansuluo.eat_favorite");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_BREED_SUCCESS = register("tiansuluo.breed_success");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_ATTACK_SHOT = register("tiansuluo.attack_shot");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_ATTACK_END = register("tiansuluo.attack_end");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_ATTACK_DECLARE = register("tiansuluo.attack_declare");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_GROW_UP = register("tiansuluo.grow_up");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_SHEAR_REACT = register("tiansuluo.shear_react");
    /** Shared by both Tiansuluo subspecies when they notice a player for the first time. */
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_NOTICE_PLAYER = register("tiansuluo.notice_player");

    public static final int TIANSULUO_PS_AMBIENT_VARIANT_COUNT = 52;
    public static final List<DeferredHolder<SoundEvent, SoundEvent>> TIANSULUO_PS_AMBIENT_VARIANTS =
            IntStream.rangeClosed(1, TIANSULUO_PS_AMBIENT_VARIANT_COUNT)
                    .mapToObj(index -> register(String.format(Locale.ROOT, "tiansuluo_ps.ambient_%02d", index)))
                    .toList();
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_PS_HURT = register("tiansuluo_ps.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_PS_DEATH = register("tiansuluo_ps.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_PS_LUANLUAN_BLOCK_BROKEN = register("tiansuluo_ps.luanluan_block_broken");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_PS_EAT = register("tiansuluo_ps.eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_PS_EAT_FAVORITE = register("tiansuluo_ps.eat_favorite");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_PS_BREED_SUCCESS = register("tiansuluo_ps.breed_success");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_PS_ATTACK_SHOT = register("tiansuluo_ps.attack_shot");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_PS_ATTACK_END = register("tiansuluo_ps.attack_end");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_PS_ATTACK_DECLARE = register("tiansuluo_ps.attack_declare");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_PS_GROW_UP = register("tiansuluo_ps.grow_up");
    public static final DeferredHolder<SoundEvent, SoundEvent> TIANSULUO_PS_SHEAR_REACT = register("tiansuluo_ps.shear_react");

    public static final DeferredHolder<SoundEvent, SoundEvent> SUXIA_AMBIENT = register("suxia.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUXIA_HURT = register("suxia.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUXIA_DEATH = register("suxia.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SUXIA_LUANLUAN_SHOT = register("suxia.luanluan_shot");
    private ModSoundEvents() {
    }

    public static void register(IEventBus modEventBus) {
        SOUND_EVENTS.register(modEventBus);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> register(String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(OhYeah.MODID, path);
        return SOUND_EVENTS.register(path, () -> SoundEvent.createVariableRangeEvent(id));
    }
}
