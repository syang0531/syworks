package com.syang.mcextraction.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

public class AlloyRecipeSerializer implements RecipeSerializer<AlloyRecipe> {

    public static final MapCodec<AlloyRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    SizedIngredient.FLAT_CODEC.listOf().fieldOf("ingredients").forGetter(AlloyRecipe::inputs),
                    ItemStack.CODEC.fieldOf("result").forGetter(AlloyRecipe::result),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("processtime", 200).forGetter(AlloyRecipe::processTime)
            ).apply(instance, AlloyRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), AlloyRecipe::inputs,
            ItemStack.STREAM_CODEC, AlloyRecipe::result,
            ByteBufCodecs.INT, AlloyRecipe::processTime,
            AlloyRecipe::new);

    @Override
    public MapCodec<AlloyRecipe> codec() {
        return CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> streamCodec() {
        return STREAM_CODEC;
    }
}
