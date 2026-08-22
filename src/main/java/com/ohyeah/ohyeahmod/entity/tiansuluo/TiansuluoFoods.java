package com.ohyeah.ohyeahmod.entity.tiansuluo;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record TiansuluoFoods(List<String> liked, List<String> favorite) {

    public boolean isFood(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) return false;
        return liked.stream().anyMatch(id -> ResourceLocation.parse(id).equals(key))
                || favorite.stream().anyMatch(id -> ResourceLocation.parse(id).equals(key));
    }

    public boolean isFavorite(ItemStack stack) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key == null) return false;
        return favorite.stream().anyMatch(id -> ResourceLocation.parse(id).equals(key));
    }
}
