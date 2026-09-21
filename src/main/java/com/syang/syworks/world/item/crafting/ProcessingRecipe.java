package com.syang.syworks.world.item.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.syang.syworks.registry.ModRecipes;
import com.syang.syworks.world.level.block.ModMachine;
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
 * One recipe shape for every machine: {@code count} of one input → one output, over
 * {@code processtime} ticks, paying out {@code experience} when the result is taken.
 *
 * <p>Each {@link ModMachine} gets its own recipe type and serializer, so a recipe written for the
 * crusher can never fire in the roaster. The machine is baked into the codec rather than written in
 * the JSON — the file's {@code type} already says which machine it belongs to.
 *
 * <p>JSON shape (data/syworks/recipe/*.json); ingredients use the 1.21.2+ plain-string form:
 * <pre>
 * {
 *   "type": "syworks:crushing",
 *   "ingredient": "#syworks:crushable/rock",
 *   "count": 1,
 *   "result": { "id": "minecraft:gravel", "count": 1 },
 *   "processtime": 200,
 *   "experience": 0.05
 * }
 * </pre>
 *
 * <p><b>{@code count} is what closes the slab dupe hole.</b> A stonecutter turns one stone block
 * into <i>two</i> slabs, so a slab must be worth half a block; every other cut (stairs, walls,
 * polished) is 1:1 and needs no count. See docs/정체성-재설계.md §6.
 */
public final class ProcessingRecipe implements Recipe<SingleRecipeInput> {

    private final ModMachine machine;
    private final Ingredient input;
    private final int inputCount;
    private final ItemStackTemplate result;
    private final int processTime;
    private final float experience;

    public ProcessingRecipe(ModMachine machine, Ingredient input, int inputCount,
                            ItemStackTemplate result, int processTime, float experience) {
        this.machine = machine;
        this.input = input;
        this.inputCount = inputCount;
        this.result = result;
        this.processTime = processTime;
        this.experience = experience;
    }

    /** Codec for {@code machine}'s recipes; the machine is fixed, not read from the file. */
    public static MapCodec<ProcessingRecipe> codec(ModMachine machine) {
        return RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(ProcessingRecipe::input),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("count", 1).forGetter(ProcessingRecipe::inputCount),
                ItemStackTemplate.CODEC.fieldOf("result").forGetter(ProcessingRecipe::result),
                ExtraCodecs.POSITIVE_INT.optionalFieldOf("processtime", 200).forGetter(ProcessingRecipe::processTime),
                Codec.FLOAT.optionalFieldOf("experience", 0.0F).forGetter(ProcessingRecipe::experience)
        ).apply(instance, (ingredient, count, result, time, xp) ->
                new ProcessingRecipe(machine, ingredient, count, result, time, xp)));
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ProcessingRecipe> streamCodec(ModMachine machine) {
        return StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, ProcessingRecipe::input,
                ByteBufCodecs.VAR_INT, ProcessingRecipe::inputCount,
                ItemStackTemplate.STREAM_CODEC, ProcessingRecipe::result,
                ByteBufCodecs.INT, ProcessingRecipe::processTime,
                ByteBufCodecs.FLOAT, ProcessingRecipe::experience,
                (ingredient, count, result, time, xp) -> new ProcessingRecipe(machine, ingredient, count, result, time, xp));
    }

    public ModMachine machine() {
        return machine;
    }

    public Ingredient input() {
        return input;
    }

    /** How many of {@code input} one run consumes. */
    public int inputCount() {
        return inputCount;
    }

    public ItemStackTemplate result() {
        return result;
    }

    public int processTime() {
        return processTime;
    }

    /** Experience per run, paid out when the player takes the output (as a furnace does). */
    public float experience() {
        return experience;
    }

    @Override
    public boolean matches(SingleRecipeInput recipeInput, Level level) {
        ItemStack stack = recipeInput.item();
        return input.test(stack) && stack.getCount() >= inputCount;
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
    public RecipeSerializer<ProcessingRecipe> getSerializer() {
        return ModRecipes.SERIALIZERS.get(machine).get();
    }

    @Override
    public RecipeType<ProcessingRecipe> getType() {
        return ModRecipes.TYPES.get(machine).get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(input);
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return ModRecipes.CATEGORIES.get(machine).get();
    }
}
