package com.syang.mcextraction.datagen;

import com.syang.mcextraction.MCExtraction;
import com.syang.mcextraction.registry.ModBlocks;
import com.syang.mcextraction.registry.ModItems;
import com.syang.mcextraction.world.item.ModAlloy;
import com.syang.mcextraction.world.item.ModMetal;
import com.syang.mcextraction.world.item.ModSpell;
import com.syang.mcextraction.world.item.ModWand;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Generates assets/mcextraction/lang/en_us.json from the metal/alloy display names.
 * (Korean ko_kr.json is hand-written.)
 */
public class ModEnglishLangProvider extends LanguageProvider {

    public ModEnglishLangProvider(PackOutput output) {
        super(output, MCExtraction.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.mcextraction.main", "MC Extraction");

        add(ModBlocks.EXTRACTOR.get(), "Extraction Furnace");
        add(ModBlocks.ALLOY_FURNACE.get(), "Alloy Furnace");
        add(ModBlocks.RUNE_ALTAR.get(), "Rune Altar");

        add(ModItems.RUNE.get(), "Rune");

        for (ModMetal metal : ModMetal.values()) {
            add(ModItems.METAL_INGOTS.get(metal).get(), metal.displayName());
        }

        for (ModAlloy alloy : ModAlloy.values()) {
            String name = alloy.displayName();
            add(ModItems.ALLOY_INGOTS.get(alloy).get(), name + " Ingot");
            add(ModItems.ALLOY_SWORDS.get(alloy).get(), name + " Sword");
            add(ModItems.ALLOY_PICKAXES.get(alloy).get(), name + " Pickaxe");
            add(ModItems.ALLOY_AXES.get(alloy).get(), name + " Axe");
            add(ModItems.ALLOY_SHOVELS.get(alloy).get(), name + " Shovel");
            add(ModItems.ALLOY_HOES.get(alloy).get(), name + " Hoe");
            add(ModItems.ALLOY_HELMETS.get(alloy).get(), name + " Helmet");
            add(ModItems.ALLOY_CHESTPLATES.get(alloy).get(), name + " Chestplate");
            add(ModItems.ALLOY_LEGGINGS.get(alloy).get(), name + " Leggings");
            add(ModItems.ALLOY_BOOTS.get(alloy).get(), name + " Boots");
        }

        // Wand system.
        for (ModWand wand : ModWand.values()) {
            add(ModItems.WANDS.get(wand).get(), wand.displayName());
        }
        for (ModSpell spell : ModSpell.values()) {
            add(ModItems.SPELL_BOOKS.get(spell).get(), spell.displayName() + " Spell Tome");
        }

        add("tooltip.mcextraction.wand.affinity", "Affinity: ");
        add("tooltip.mcextraction.wand.affinity_all", "All Elements");
        add("tooltip.mcextraction.wand.empty", "No spells bound");
        add("tooltip.mcextraction.wand.spells", "Spells (%s/%s):");
        add("tooltip.mcextraction.spellbook.bind", "Bind to a wand at an anvil");
        add("message.mcextraction.wand.no_spell", "No spell bound");
    }
}
