package com.syang.mcextraction.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ImbueRecipeSerializer implements RecipeSerializer<ImbueRecipe> {

    public static final MapCodec<ImbueRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ItemStack.CODEC.fieldOf("base").forGetter(ImbueRecipe::base),
                    ItemStack.CODEC.fieldOf("catalyst").forGetter(ImbueRecipe::catalyst),
                    ItemStack.CODEC.fieldOf("result").forGetter(ImbueRecipe::result),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("casttime", 100).forGetter(ImbueRecipe::castTime)
            ).apply(instance, ImbueRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ImbueRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, ImbueRecipe::base,
            ItemStack.STREAM_CODEC, ImbueRecipe::catalyst,
            ItemStack.STREAM_CODEC, ImbueRecipe::result,
            ByteBufCodecs.INT, ImbueRecipe::castTime,
            ImbueRecipe::new);

    @Override
    public MapCodec<ImbueRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ImbueRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
