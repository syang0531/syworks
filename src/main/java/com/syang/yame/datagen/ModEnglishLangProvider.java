package com.syang.yame.datagen;

import com.syang.yame.Yame;
import com.syang.yame.registry.ModBlocks;
import com.syang.yame.registry.ModItems;
import com.syang.yame.world.item.ModAlloy;
import com.syang.yame.world.item.ModMetal;
import com.syang.yame.world.item.ModSpell;
import com.syang.yame.world.item.ModWand;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Generates assets/yame/lang/en_us.json from the metal/alloy display names.
 * (Korean ko_kr.json is hand-written.)
 */
public class ModEnglishLangProvider extends LanguageProvider {

    public ModEnglishLangProvider(PackOutput output) {
        super(output, Yame.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.yame.main", "Yame");

        add(ModBlocks.EXTRACTION_FURNACE.get(), "Extraction Furnace");
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
            add(ModItems.SPELL_BOOKS.get(spell).get(), spell.displayName() + " Spellbook");
        }

        add("tooltip.yame.wand.affinity", "Affinity: ");
        add("tooltip.yame.wand.affinity_all", "All Elements");
        add("tooltip.yame.wand.empty", "No spells bound");
        add("tooltip.yame.wand.spells", "Spells (%s/%s):");
        add("tooltip.yame.spellbook.bind", "Bind to a wand at an anvil");
        add("message.yame.wand.no_spell", "No spell bound");
    }
}
