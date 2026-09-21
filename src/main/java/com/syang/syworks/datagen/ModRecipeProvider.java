package com.syang.syworks.datagen;

import com.syang.syworks.SyWorks;
import com.syang.syworks.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.concurrent.CompletableFuture;

/**
 * Crafting recipes for the machines themselves. Without these the mod is survival-unobtainable, and
 * they use vanilla-only ingredients so nothing has to exist before them.
 *
 * <p>Since 1.21.4 a {@link RecipeProvider} is created per run by a {@link Runner}; the provider
 * itself only holds the registries and the output.
 */
public class ModRecipeProvider extends RecipeProvider {

    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        // Extraction Furnace — a furnace core wrapped in iron.
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.EXTRACTION_FURNACE.get())
                .pattern("III").pattern("IFI").pattern("III")
                .define('I', Items.IRON_INGOT).define('F', Items.FURNACE)
                .unlockedBy("has_furnace", has(Items.FURNACE))
                .save(this.output, key("extraction_furnace"));
    }

    private static ResourceKey<Recipe<?>> key(String path) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(SyWorks.MOD_ID, path));
    }

    /** What the data generator actually instantiates; it hands each run a fresh provider. */
    public static final class Runner extends RecipeProvider.Runner {

        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
            super(output, registries);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new ModRecipeProvider(registries, output);
        }

        @Override
        public String getName() {
            return "SyWorks recipes";
        }
    }
}
