package com.syang.syalchemy.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.syang.syalchemy.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * A single-input recipe for the Extraction Furnace: one source block/item → one metal.
 *
 * <p>JSON shape (data/syalchemy/recipe/*.json) — ingredients use the 1.21.2+ plain-string form:
 * <pre>
 * {
 *   "type": "syalchemy:extraction",
 *   "ingredient": "minecraft:granite",
 *   "result": { "id": "syalchemy:tin_ingot", "count": 1 },
 *   "processtime": 200
 * }
 * </pre>
 */
public record ExtractionRecipe(Ingredient input, ItemStackTemplate result, int processTime)
        implements Recipe<SingleRecipeInput> {

    public static final MapCodec<ExtractionRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(ExtractionRecipe::input),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(ExtractionRecipe::result),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("processtime", 200).forGetter(ExtractionRecipe::processTime)
            ).apply(instance, ExtractionRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ExtractionRecipe> STREAM_CODEC = StreamCodec.composite(
            Ingredient.CONTENTS_STREAM_CODEC, ExtractionRecipe::input,
            ItemStackTemplate.STREAM_CODEC, ExtractionRecipe::result,
            ByteBufCodecs.INT, ExtractionRecipe::processTime,
            ExtractionRecipe::new);

    @Override
    public boolean matches(SingleRecipeInput recipeInput, Level level) {
        return input.test(recipeInput.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput recipeInput) {
        return result.create();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<ExtractionRecipe> getSerializer() {
        return ModRecipes.EXTRACTION_SERIALIZER.get();
    }

    @Override
    public RecipeType<ExtractionRecipe> getType() {
        return ModRecipes.EXTRACTION_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(input);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipes.EXTRACTION_CATEGORY.get();
    }
}
