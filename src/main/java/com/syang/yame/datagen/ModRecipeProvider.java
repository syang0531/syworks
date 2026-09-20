package com.syang.yame.datagen;

import com.syang.yame.Yame;
import com.syang.yame.registry.ModBlocks;
import com.syang.yame.registry.ModItems;
import com.syang.yame.world.item.ModAlloy;
import com.syang.yame.world.item.ModStaff;
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
 * alloy's tools and armor, the staffs, and the three machines. This closes the survival loop:
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
    // Staff: material head on a two-stick diagonal shaft.
    private static final String[] STAFF = {"  X", " S ", "S  "};

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

        for (ModStaff staff : ModStaff.values()) {
            staff(staff);
        }
    }

    /**
     * The three functional blocks — without these the whole mod is survival-unobtainable.
     * All use vanilla-only ingredients so they can bootstrap the loop (extract → alloy → magic).
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

        // Rune Altar — a recoloured enchanting table; themed on the rune (gold + lapis).
        ShapedRecipeBuilder.shaped(this.items, RecipeCategory.MISC, ModBlocks.RUNE_ALTAR.get())
                .pattern("GLG").pattern("LEL").pattern("GLG")
                .define('G', Items.GOLD_INGOT).define('L', Items.LAPIS_LAZULI).define('E', Items.ENCHANTING_TABLE)
                .unlockedBy("has_enchanting_table", has(Items.ENCHANTING_TABLE))
                .save(this.output, key("rune_altar"));
    }

    /** Crafts a staff from its material (alloy ingot, vanilla item, or vanilla tag) + two sticks. */
    private void staff(ModStaff staff) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(this.items, RecipeCategory.COMBAT, ModItems.STAFFS.get(staff).get());
        for (String row : STAFF) {
            builder.pattern(row);
        }
        if (staff.alloy() != null) {
            builder.define('X', ModItems.ALLOY_INGOTS.get(staff.alloy()).get());
        } else if (staff.materialTag() != null) {
            builder.define('X', staff.materialTag());
        } else if (staff.materialItem() != null) {
            builder.define('X', staff.materialItem().get());
        } else {
            throw new IllegalStateException("Staff " + staff + " has no crafting material");
        }
        builder.define('S', Items.STICK);
        builder.unlockedBy("has_stick", has(Items.STICK));
        builder.save(this.output, key(staff.id()));
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
        return ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(Yame.MOD_ID, path));
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
            return "Yame recipes";
        }
    }
}
