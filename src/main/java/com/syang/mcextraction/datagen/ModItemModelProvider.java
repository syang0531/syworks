package com.syang.mcextraction.datagen;

import com.syang.mcextraction.MCExtraction;
import com.syang.mcextraction.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Generates item models: ingots use {@code item/generated}, tools use {@code item/handheld}.
 * Textures are expected at assets/mcextraction/textures/item/&lt;name&gt;.png (add later).
 */
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, MCExtraction.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ModItems.METAL_INGOTS.values().forEach(this::basicItem);
        ModItems.ALLOY_INGOTS.values().forEach(this::basicItem);

        ModItems.ALLOY_SWORDS.values().forEach(this::handheldItem);
        ModItems.ALLOY_PICKAXES.values().forEach(this::handheldItem);
        ModItems.ALLOY_AXES.values().forEach(this::handheldItem);
        ModItems.ALLOY_SHOVELS.values().forEach(this::handheldItem);
        ModItems.ALLOY_HOES.values().forEach(this::handheldItem);

        ModItems.ALLOY_HELMETS.values().forEach(this::basicItem);
        ModItems.ALLOY_CHESTPLATES.values().forEach(this::basicItem);
        ModItems.ALLOY_LEGGINGS.values().forEach(this::basicItem);
        ModItems.ALLOY_BOOTS.values().forEach(this::basicItem);

        basicItem(ModItems.RUNE);

        // Wand system. Textures are art-track TODO — for now models reuse recognizable vanilla
        // icons (a rod for wands, an enchanted book for tomes) so runData stays green. Swap
        // layer0 to mcextraction:item/<name> once gen_textures.ps1 produces per-item art.
        ModItems.WANDS.values().forEach(item ->
                withExistingParent(item.getId().getPath(), mcLoc("item/handheld"))
                        .texture("layer0", mcLoc("item/blaze_rod")));
        ModItems.SPELL_BOOKS.values().forEach(item ->
                withExistingParent(item.getId().getPath(), mcLoc("item/generated"))
                        .texture("layer0", mcLoc("item/enchanted_book")));
    }

    private void basicItem(DeferredItem<?> item) {
        basicItem(item.get());
    }

    private void handheldItem(DeferredItem<?> item) {
        String path = item.getId().getPath();
        withExistingParent(path, mcLoc("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(MCExtraction.MOD_ID, "item/" + path));
    }
}
