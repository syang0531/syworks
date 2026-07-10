package com.syang.yame.world.item.crafting;

import com.syang.yame.registry.ModRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

/**
 * Rune Altar recipe: a {@code base} item + a {@code catalyst} item → a {@code result}, cast over
 * {@code castTime} ticks. Both base and catalyst are matched <b>exactly</b> (item + data
 * components), and their stack {@code count} is the required amount.
 *
 * <p>Exact-component matching is what makes the enchanted-book level chain work: a
 * "Sharpness&nbsp;I" book is component-distinct from a "Sharpness&nbsp;II" book, so
 * {@code (Sharpness I book) + rune → (Sharpness II book)} only fires on the Lv1 book. A plain
 * {@link Ingredient} (item/tag only) could not tell the two books apart.
 *
 * <p>JSON shape (data/yame/recipe/*.json):
 * <pre>
 * {
 *   "type": "yame:imbuing",
 *   "base":     { "id": "minecraft:gold_ingot" },
 *   "catalyst": { "id": "minecraft:lapis_lazuli" },
 *   "result":   { "id": "yame:rune" },
 *   "casttime": 100
 * }
 * </pre>
 */
public record ImbueRecipe(ItemStack base, ItemStack catalyst, ItemStack result, int castTime)
        implements Recipe<ImbueRecipeInput> {

    @Override
    public boolean matches(ImbueRecipeInput input, Level level) {
        // Order-independent: either slot may hold the base or the catalyst.
        return (baseMatches(input.first()) && catalystMatches(input.second()))
                || (baseMatches(input.second()) && catalystMatches(input.first()));
    }

    /** Whether the given stack satisfies this recipe's base (item + components + count). */
    public boolean baseMatches(ItemStack in) {
        return matchesStack(in, base);
    }

    /** Whether the given stack satisfies this recipe's catalyst (item + components + count). */
    public boolean catalystMatches(ItemStack in) {
        return matchesStack(in, catalyst);
    }

    /** Exact item + component match, with the input holding at least the required count. */
    private static boolean matchesStack(ItemStack in, ItemStack required) {
        return in.getCount() >= required.getCount() && ItemStack.isSameItemSameComponents(in, required);
    }

    @Override
    public ItemStack assemble(ImbueRecipeInput input, HolderLookup.Provider registries) {
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
        list.add(Ingredient.of(base.getItem()));
        list.add(Ingredient.of(catalyst.getItem()));
        return list;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipes.IMBUE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipes.IMBUE_TYPE.get();
    }
}
