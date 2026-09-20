package com.syang.syalchemy.world.item.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

/**
 * Recipe input wrapping the Rune Altar's two input slots. The two are interchangeable —
 * {@link ImbueRecipe#matches} tries both orderings — so {@code first}/{@code second} are just
 * "slot 0" and "slot 1", not a fixed base/catalyst assignment.
 */
public record ImbueRecipeInput(ItemStack first, ItemStack second) implements RecipeInput {

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? first : second;
    }

    @Override
    public int size() {
        return 2;
    }
}
