package com.syang.yame.datagen;

import com.syang.yame.Yame;
import com.syang.yame.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Generates item models: ingots use {@code item/generated}, tools use {@code item/handheld}.
 * Textures are expected at assets/yame/textures/item/&lt;name&gt;.png (add later).
 */
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Yame.MOD_ID, existingFileHelper);
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

        // Staff system. Staffs use a per-material tinted staff icon (item/handheld); spellbooks
        // use a single blue-recolored enchanted-book icon (item/generated). Textures are
        // produced by tools/gen_textures.ps1 at assets/yame/textures/item/<name>.png.
        ModItems.STAFFS.values().forEach(this::handheldItem);
        ModItems.SPELL_BOOKS.values().forEach(this::basicItem);
    }

    private void basicItem(DeferredItem<?> item) {
        basicItem(item.get());
    }

    private void handheldItem(DeferredItem<?> item) {
        String path = item.getId().getPath();
        withExistingParent(path, mcLoc("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(Yame.MOD_ID, "item/" + path));
    }
}
