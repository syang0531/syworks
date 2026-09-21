package com.syang.syworks.registry;

import com.syang.syworks.SyWorks;
import com.syang.syworks.world.item.crafting.ExtractionRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Recipe types, serializers and recipe-book categories for the machines.
 *
 * <p>Since 1.21.2 a {@link RecipeSerializer} is a plain record of (codec, stream codec), and every
 * recipe must name a {@link RecipeBookCategory}; ours are registered here but never shown in the
 * vanilla recipe book (the machines have no book UI), so they are just the required placeholders.
 */
public final class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, SyWorks.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, SyWorks.MOD_ID);
    public static final DeferredRegister<RecipeBookCategory> BOOK_CATEGORIES =
            DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, SyWorks.MOD_ID);

    public static final Supplier<RecipeType<ExtractionRecipe>> EXTRACTION_TYPE =
            RECIPE_TYPES.register("extraction", () -> RecipeType.simple(id("extraction")));
    public static final Supplier<RecipeSerializer<ExtractionRecipe>> EXTRACTION_SERIALIZER =
            RECIPE_SERIALIZERS.register("extraction",
                    () -> new RecipeSerializer<>(ExtractionRecipe.CODEC, ExtractionRecipe.STREAM_CODEC));
    public static final Supplier<RecipeBookCategory> EXTRACTION_CATEGORY =
            BOOK_CATEGORIES.register("extraction", RecipeBookCategory::new);

    private ModRecipes() {
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(SyWorks.MOD_ID, path);
    }

    public static void register(IEventBus modBus) {
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        BOOK_CATEGORIES.register(modBus);
    }
}
