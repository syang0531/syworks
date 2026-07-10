package com.syang.mcextraction.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class ExtractionRecipeSerializer implements RecipeSerializer<ExtractionRecipe> {

    public static final MapCodec<ExtractionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(ExtractionRecipe::input),
                    ItemStack.CODEC.fieldOf("result").forGetter(ExtractionRecipe::result),
                    net.minecraft.util.ExtraCodecs.POSITIVE_INT.optionalFieldOf("processtime", 200)
                            .forGetter(ExtractionRecipe::processTime)
            ).apply(instance, ExtractionRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExtractionRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, ExtractionRecipe::input,
            ItemStack.STREAM_CODEC, ExtractionRecipe::result,
            ByteBufCodecs.INT, ExtractionRecipe::processTime,
            ExtractionRecipe::new);

    @Override
    public MapCodec<ExtractionRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, ExtractionRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
