package com.syang.syalchemy.datagen;

import com.syang.syalchemy.SyAlchemy;
import com.syang.syalchemy.registry.ModBlocks;
import com.syang.syalchemy.registry.ModItems;
import com.syang.syalchemy.world.item.ModAlloy;
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
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Generates standard vanilla-pattern crafting recipes (alloy ingot + sticks) for every
 * alloy's tools and armor, and the two machines. This closes the survival loop:
 * ingot → equipment.
 *
 * <p>Since 1.21.4 a {@link RecipeProvider} is created per run by a {@link Runner}; the provider
 * itself only holds the registries and the output.
 */
public class ModRecipeProvider extends RecipeProvider {

    // 'X' = alloy ingot, 'S' = stick.
    private static final String[] SWORD = {"X", "X", "S"};
    private static final String[] PICKAXE = {"XXX", " S ", " S "};
    private static final String[] AXE = {"XX", "XS", " S"};
    private static final String[] SHOVEL = {"X", "S", "S"};
    private static final String[] HOE = {"XX", " S", " S"};
    private static final String[] HELMET = {"XXX", "X X"};
    private static final String[] CHESTPLATE = {"X X", "XXX", "XXX"};
    private static final String[] LEGGINGS = {"XXX", "X X", "X X"};
    private static final String[] BOOTS = {"X X", "X X"};

    protected ModRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        machines();

        for (ModAlloy alloy : ModAlloy.values()) {
            ItemLike ingot = ModItems.ALLOY_INGOTS.get(alloy).get();

            equip(ingot, ModItems.ALLOY_SWORDS.get(alloy).get(), alloy.swordName(), RecipeCategory.COMBAT, SWORD);
            equip(ingot, ModItems.ALLOY_PICKAXES.get(alloy).get(), alloy.pickaxeName(), RecipeCategory.TOOLS, PICKAXE);
            equip(ingot, ModItems.ALLOY_AXES.get(alloy).get(), alloy.axeName(), RecipeCategory.TOOLS, AXE);
            equip(ingot, ModItems.ALLOY_SHOVELS.get(alloy).get(), alloy.shovelName(), RecipeCategory.TOOLS, SHOVEL);
            equip(ingot, ModItems.ALLOY_HOES.get(alloy).get(), alloy.hoeName(), RecipeCategory.TOOLS, HOE);

            equip(ingot, ModItems.ALLOY_HELMETS.get(alloy).get(), alloy.helmetName(), RecipeCategory.COMBAT, HELMET);
            equip(ingot, ModItems.ALLOY_CHESTPLATES.get(alloy).get(), alloy.chestplateName(), RecipeCategory.COMBAT, CHESTPLATE);
            equip(ingot, ModItems.ALLOY_LEGGINGS.get(alloy).get(), alloy.leggingsName(), RecipeCategory.COMBAT, LEGGINGS);
            equip(ingot, ModItems.ALLOY_BOOTS.get(alloy).get(), alloy.bootsName(), RecipeCategory.COMBAT, BOOTS);
        }
    }

    /**
     * The two functional blocks — without these the whole mod is survival-unobtainable.
     * All use vanilla-only ingredients so they can bootstrap the loop (extract → alloy → craft).
     * Tune ingredients here to taste.
     */
    private void machines() {
        // Extraction Furnace — the entry machine. Furnace core wrapped in iron.
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.EXTRACTION_FURNACE.get())
                .pattern("III").pattern("IFI").pattern("III")
                .define('I', Items.IRON_INGOT).define('F', Items.FURNACE)
                .unlockedBy("has_furnace", has(Items.FURNACE))
                .save(this.output, key("extraction_furnace"));

        // Alloy Furnace — blast-furnace core (hotter) wrapped in copper.
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.ALLOY_FURNACE.get())
                .pattern("CCC").pattern("CBC").pattern("CCC")
                .define('C', Items.COPPER_INGOT).define('B', Items.BLAST_FURNACE)
                .unlockedBy("has_blast_furnace", has(Items.BLAST_FURNACE))
                .save(this.output, key("alloy_furnace"));
    }

    private void equip(ItemLike ingot, ItemLike result, String id, RecipeCategory category, String[] pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(this.items, category, result);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.define('X', ingot);
        if (Arrays.stream(pattern).anyMatch(row -> row.indexOf('S') >= 0)) {
            builder.define('S', Items.STICK);
        }
        builder.unlockedBy("has_" + id, has(ingot));
        builder.save(this.output, key(id));
    }

    private static ResourceKey<Recipe<?>> key(String path) {
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(SyAlchemy.MOD_ID, path));
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
            return "SyAlchemy recipes";
        }
    }
}
