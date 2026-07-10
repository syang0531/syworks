package com.syang.mcextraction.world.item.crafting;

import com.syang.mcextraction.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
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
 * <p>JSON shape (data/mcextraction/recipe/*.json):
 * <pre>
 * {
 *   "type": "mcextraction:alloying",
 *   "ingredients": [
 *     { "item": "minecraft:copper_ingot", "count": 3 },
 *     { "item": "mcextraction:tin_ingot", "count": 1 }
 *   ],
 *   "result": { "id": "mcextraction:bronze_ingot", "count": 4 },
 *   "processtime": 200
 * }
 * </pre>
 */
public record AlloyRecipe(List<SizedIngredient> inputs, ItemStack result, int processTime)
        implements Recipe<AlloyRecipeInput> {

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
    public ItemStack assemble(AlloyRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (SizedIngredient input : inputs) {
            list.add(input.ingredient());
        }
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.ALLOY_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.ALLOY_TYPE.get();
    }
}
