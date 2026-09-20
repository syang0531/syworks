package com.syang.yame.world.item.crafting;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.syang.yame.registry.ModRecipes;
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

/**
 * Rune Altar recipe: a {@code base} item + a {@code catalyst} item → a {@code result}, cast over
 * {@code castTime} ticks. Both base and catalyst are matched <b>exactly</b> (item + data
 * components), and their stack {@code count} is the required amount.
 *
 * <p>Exact-component matching is what makes the enchanted-book level chain work: a
 * "Sharpness&nbsp;I" book is component-distinct from a "Sharpness&nbsp;II" book, so
 * {@code (Sharpness I book) + rune → (Sharpness II book)} only fires on the Lv1 book. A plain
 * {@code Ingredient} (item/tag only) could not tell the two books apart.
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
public record ImbueRecipe(ItemStackTemplate base, ItemStackTemplate catalyst, ItemStackTemplate result, int castTime)
        implements Recipe<ImbueRecipeInput> {

    public static final MapCodec<ImbueRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ItemStackTemplate.CODEC.fieldOf("base").forGetter(ImbueRecipe::base),
                    ItemStackTemplate.CODEC.fieldOf("catalyst").forGetter(ImbueRecipe::catalyst),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(ImbueRecipe::result),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("casttime", 100).forGetter(ImbueRecipe::castTime)
            ).apply(instance, ImbueRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ImbueRecipe> STREAM_CODEC = StreamCodec.composite(
            ItemStackTemplate.STREAM_CODEC, ImbueRecipe::base,
            ItemStackTemplate.STREAM_CODEC, ImbueRecipe::catalyst,
            ItemStackTemplate.STREAM_CODEC, ImbueRecipe::result,
            ByteBufCodecs.INT, ImbueRecipe::castTime,
            ImbueRecipe::new);

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
    private static boolean matchesStack(ItemStack in, ItemStackTemplate required) {
        return in.getCount() >= required.count() && ItemStack.isSameItemSameComponents(in, required);
    }

    @Override
    public ItemStack assemble(ImbueRecipeInput input) {
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
    public RecipeSerializer<ImbueRecipe> getSerializer() {
        return ModRecipes.IMBUE_SERIALIZER.get();
    }

    @Override
    public RecipeType<ImbueRecipe> getType() {
        return ModRecipes.IMBUE_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    /** Like vanilla's special recipes: skipped by the recipe book, so the "can't be placed" warning is not logged. */
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipes.IMBUE_CATEGORY.get();
    }
}
