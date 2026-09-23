package com.syang.syworks.datagen;

import com.syang.syworks.SyWorks;
import com.syang.syworks.registry.ModBlocks;
import com.syang.syworks.world.level.block.ModMachine;
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
 * Crafting recipes for the machines themselves, and the incinerator. Without these the mod is survival-unobtainable, and
 * they use vanilla-only ingredients so nothing has to exist before them.
 *
 * <p>The machines' own recipes (what they accept and produce) are hand-written JSON under
 * {@code data/syworks/recipe/}, because they are tag-driven one-liners rather than patterns.
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
        // Ore Roaster — a furnace core wrapped in iron.
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC,
                        ModBlocks.MACHINES.get(ModMachine.ORE_ROASTER).get())
                .pattern("III").pattern("IFI").pattern("III")
                .define('I', Items.IRON_INGOT).define('F', Items.FURNACE)
                .unlockedBy("has_furnace", has(Items.FURNACE))
                .save(this.output, key(ModMachine.ORE_ROASTER.id()));

        // Crusher — a piston doing the crushing, braced with iron inside a stone shell.
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC,
                        ModBlocks.MACHINES.get(ModMachine.CRUSHER).get())
                .pattern("CIC").pattern("IPI").pattern("CIC")
                .define('C', Items.COBBLESTONE).define('I', Items.IRON_INGOT).define('P', Items.PISTON)
                .unlockedBy("has_piston", has(Items.PISTON))
                .save(this.output, key(ModMachine.CRUSHER.id()));

        // Charcoal Kiln — a furnace sealed in fired clay, which is what a real charcoal kiln is.
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC,
                        ModBlocks.MACHINES.get(ModMachine.CHARCOAL_KILN).get())
                .pattern("BBB").pattern("BFB").pattern("BBB")
                .define('B', Items.BRICK).define('F', Items.FURNACE)
                .unlockedBy("has_brick", has(Items.BRICK))
                .save(this.output, key(ModMachine.CHARCOAL_KILN.id()));

        // Incinerator — a pool of lava walled in stone, which is the trash can it replaces.
        // The bucket comes back: a lava bucket's crafting remainder is an empty bucket.
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.INCINERATOR.get())
                .pattern("CCC").pattern("CLC").pattern("CCC")
                .define('C', Items.COBBLESTONE).define('L', Items.LAVA_BUCKET)
                .unlockedBy("has_lava_bucket", has(Items.LAVA_BUCKET))
                .save(this.output, key("incinerator"));
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
