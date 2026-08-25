package com.ohyeah.ohyeahmod.registry;

import com.ohyeah.ohyeahmod.OhYeah;
import com.ohyeah.ohyeahmod.block.LuanluanEggBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** 模组方块实体注册中心。 */
public final class ModBlockEntities {
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, OhYeah.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<LuanluanEggBlockEntity>> LUANLUAN_EGG =
            BLOCK_ENTITIES.register(
                    "luanluan_egg",
                    () -> BlockEntityType.Builder.of(
                            LuanluanEggBlockEntity::new,
                            ModBlocks.TIANSULUO_PINK_SCARF_LUANLUAN_BLOCK.get(),
                            ModBlocks.TIANSULUO_BATTLE_FACE_LUANLUAN_BLOCK.get()
                    ).build(null)
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITIES.register(modEventBus);
    }
}
