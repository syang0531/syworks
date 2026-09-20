package com.syang.syalchemy.datagen;

import com.syang.syalchemy.SyAlchemy;
import com.syang.syalchemy.registry.ModBlocks;
import com.syang.syalchemy.registry.ModItems;
import com.syang.syalchemy.world.item.ModAlloy;
import com.syang.syalchemy.world.item.ModMetal;
import com.syang.syalchemy.world.item.ModSpell;
import com.syang.syalchemy.world.item.ModStaff;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Generates assets/syalchemy/lang/en_us.json from the metal/alloy display names.
 * (Korean ko_kr.json is hand-written.)
 */
public class ModEnglishLangProvider extends LanguageProvider {

    public ModEnglishLangProvider(PackOutput output) {
        super(output, SyAlchemy.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.syalchemy.main", "SY Alchemy");

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

        // Staff system.
        for (ModStaff staff : ModStaff.values()) {
            add(ModItems.STAFFS.get(staff).get(), staff.displayName());
        }
        for (ModSpell spell : ModSpell.values()) {
            add(ModItems.SPELL_BOOKS.get(spell).get(), spell.displayName() + " Spellbook");
        }

        // Staff-exclusive enchantments (§5.7.9).
        add("enchantment.syalchemy.spell_power", "Spell Power");
        add("enchantment.syalchemy.alacrity", "Alacrity");
        add("enchantment.syalchemy.arcane_reach", "Arcane Reach");

        add("tooltip.syalchemy.staff.affinity", "Affinity: ");
        add("tooltip.syalchemy.staff.affinity_all", "All Elements");
        add("tooltip.syalchemy.staff.empty", "No spells bound");
        add("tooltip.syalchemy.staff.spells", "Spells (%s/%s):");
        add("tooltip.syalchemy.staff.innate", " (innate)");
        add("tooltip.syalchemy.spellbook.bind", "Bind to a staff at an anvil");
        add("message.syalchemy.staff.no_spell", "No spell bound");
    }
}
