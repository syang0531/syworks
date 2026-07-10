package com.syang.yame.datagen;

import com.syang.yame.Yame;
import com.syang.yame.registry.ModItems;
import com.syang.yame.world.item.ModAlloy;
import com.syang.yame.world.item.ModWand;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

/**
 * Generates standard vanilla-pattern crafting recipes (alloy ingot + sticks) for every
 * alloy's tools and armor. This closes the survival loop: ingot → equipment.
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
    // Wand: material head on a two-stick diagonal shaft.
    private static final String[] WAND = {"  X", " S ", "S  "};

    public ModRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        for (ModAlloy alloy : ModAlloy.values()) {
            ItemLike ingot = ModItems.ALLOY_INGOTS.get(alloy).get();

            equip(recipeOutput, ingot, ModItems.ALLOY_SWORDS.get(alloy).get(), alloy.swordName(), RecipeCategory.COMBAT, SWORD);
            equip(recipeOutput, ingot, ModItems.ALLOY_PICKAXES.get(alloy).get(), alloy.pickaxeName(), RecipeCategory.TOOLS, PICKAXE);
            equip(recipeOutput, ingot, ModItems.ALLOY_AXES.get(alloy).get(), alloy.axeName(), RecipeCategory.TOOLS, AXE);
            equip(recipeOutput, ingot, ModItems.ALLOY_SHOVELS.get(alloy).get(), alloy.shovelName(), RecipeCategory.TOOLS, SHOVEL);
            equip(recipeOutput, ingot, ModItems.ALLOY_HOES.get(alloy).get(), alloy.hoeName(), RecipeCategory.TOOLS, HOE);

            equip(recipeOutput, ingot, ModItems.ALLOY_HELMETS.get(alloy).get(), alloy.helmetName(), RecipeCategory.COMBAT, HELMET);
            equip(recipeOutput, ingot, ModItems.ALLOY_CHESTPLATES.get(alloy).get(), alloy.chestplateName(), RecipeCategory.COMBAT, CHESTPLATE);
            equip(recipeOutput, ingot, ModItems.ALLOY_LEGGINGS.get(alloy).get(), alloy.leggingsName(), RecipeCategory.COMBAT, LEGGINGS);
            equip(recipeOutput, ingot, ModItems.ALLOY_BOOTS.get(alloy).get(), alloy.bootsName(), RecipeCategory.COMBAT, BOOTS);
        }

        for (ModWand wand : ModWand.values()) {
            wand(recipeOutput, wand);
        }
    }

    /** Crafts a wand from its material (alloy ingot or vanilla item) + two sticks. */
    private void wand(RecipeOutput recipeOutput, ModWand wand) {
        Ingredient material = ModItems.wandMaterial(wand).get();
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.COMBAT, ModItems.WANDS.get(wand).get());
        for (String row : WAND) {
            builder.pattern(row);
        }
        builder.define('X', material);
        builder.define('S', Items.STICK);
        builder.unlockedBy("has_stick", has(Items.STICK));
        builder.save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Yame.MOD_ID, wand.id()));
    }

    private void equip(RecipeOutput recipeOutput, ItemLike ingot, ItemLike result, String id,
                       RecipeCategory category, String[] pattern) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(category, result);
        for (String row : pattern) {
            builder.pattern(row);
        }
        builder.define('X', ingot);
        if (Arrays.stream(pattern).anyMatch(row -> row.indexOf('S') >= 0)) {
            builder.define('S', Items.STICK);
        }
        builder.unlockedBy("has_" + id, has(ingot));
        builder.save(recipeOutput, ResourceLocation.fromNamespaceAndPath(Yame.MOD_ID, id));
    }
}
