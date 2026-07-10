package com.syang.yame.registry;

import com.syang.yame.Yame;
import com.syang.yame.world.item.crafting.AlloyRecipe;
import com.syang.yame.world.item.crafting.AlloyRecipeSerializer;
import com.syang.yame.world.item.crafting.ExtractionRecipe;
import com.syang.yame.world.item.crafting.ExtractionRecipeSerializer;
import com.syang.yame.world.item.crafting.ImbueRecipe;
import com.syang.yame.world.item.crafting.ImbueRecipeSerializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, Yame.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Yame.MOD_ID);

    public static final Supplier<RecipeType<ExtractionRecipe>> EXTRACTION_TYPE =
            RECIPE_TYPES.register("extraction", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Yame.MOD_ID + ":extraction";
                }
            });

    public static final Supplier<RecipeSerializer<ExtractionRecipe>> EXTRACTION_SERIALIZER =
            RECIPE_SERIALIZERS.register("extraction", ExtractionRecipeSerializer::new);

    public static final Supplier<RecipeType<AlloyRecipe>> ALLOY_TYPE =
            RECIPE_TYPES.register("alloying", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Yame.MOD_ID + ":alloying";
                }
            });

    public static final Supplier<RecipeSerializer<AlloyRecipe>> ALLOY_SERIALIZER =
            RECIPE_SERIALIZERS.register("alloying", AlloyRecipeSerializer::new);

    public static final Supplier<RecipeType<ImbueRecipe>> IMBUE_TYPE =
            RECIPE_TYPES.register("imbuing", () -> new RecipeType<>() {
                @Override
                public String toString() {
                    return Yame.MOD_ID + ":imbuing";
                }
            });

    public static final Supplier<RecipeSerializer<ImbueRecipe>> IMBUE_SERIALIZER =
            RECIPE_SERIALIZERS.register("imbuing", ImbueRecipeSerializer::new);

    private ModRecipes() {
    }

    public static void register(IEventBus modBus) {
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
    }
}
