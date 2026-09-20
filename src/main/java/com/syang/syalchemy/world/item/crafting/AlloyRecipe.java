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
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A shapeless, order-independent recipe for the Alloy Furnace: 2–3 metal inputs (each
 * with a required count) → one alloy. Each input slot must satisfy exactly one ingredient.
 *
 * <p>JSON shape (data/syalchemy/recipe/*.json) — NeoForge's nested sized-ingredient form:
 * <pre>
 * {
 *   "type": "syalchemy:alloying",
 *   "ingredients": [
 *     { "ingredient": "minecraft:copper_ingot", "count": 3 },
 *     { "ingredient": "syalchemy:tin_ingot", "count": 1 }
 *   ],
 *   "result": { "id": "syalchemy:bronze_ingot", "count": 4 },
 *   "processtime": 200
 * }
 * </pre>
 */
public record AlloyRecipe(List<SizedIngredient> inputs, ItemStackTemplate result, int processTime)
        implements Recipe<AlloyRecipeInput> {

    public static final MapCodec<AlloyRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    SizedIngredient.NESTED_CODEC.listOf().fieldOf("ingredients").forGetter(AlloyRecipe::inputs),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(AlloyRecipe::result),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("processtime", 200).forGetter(AlloyRecipe::processTime)
            ).apply(instance, AlloyRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AlloyRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), AlloyRecipe::inputs,
            ItemStackTemplate.STREAM_CODEC, AlloyRecipe::result,
            ByteBufCodecs.INT, AlloyRecipe::processTime,
            AlloyRecipe::new);

    @Override
    public boolean matches(AlloyRecipeInput input, Level level) {
        List<SizedIngredient> remaining = new ArrayList<>(inputs);
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }
            Iterator<SizedIngredient> it = remaining.iterator();
            boolean matched = false;
            while (it.hasNext()) {
                if (it.next().test(stack)) {
                    it.remove();
                    matched = true;
                    break;
                }
            }
            if (!matched) {
                // A non-empty slot that matches no ingredient → not this recipe.
                return false;
            }
        }
        return remaining.isEmpty();
    }

    @Override
    public ItemStack assemble(AlloyRecipeInput input) {
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
    public RecipeSerializer<AlloyRecipe> getSerializer() {
        return ModRecipes.ALLOY_SERIALIZER.get();
    }

    @Override
    public RecipeType<AlloyRecipe> getType() {
        return ModRecipes.ALLOY_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        // Not a crafting-grid recipe; nothing for the recipe book to auto-place.
        return PlacementInfo.NOT_PLACEABLE;
    }

    /** Like vanilla's special recipes: skipped by the recipe book, so the "can't be placed" warning is not logged. */
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipes.ALLOY_CATEGORY.get();
    }
}
