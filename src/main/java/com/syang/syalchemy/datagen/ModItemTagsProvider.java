package com.syang.syalchemy.datagen;

import com.syang.syalchemy.SyAlchemy;
import com.syang.syalchemy.registry.ModItems;
import com.syang.syalchemy.world.item.ModAlloy;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Places the mod's equipment into the vanilla type tags so it inherits <b>exactly</b> the vanilla
 * enchantment set for its kind — nothing more, nothing less.
 *
 * <p>Why this works: every {@code minecraft:enchantable/*} tag (which the enchantments' {@code
 * supported_items}/{@code primary_items} reference) is itself defined in terms of the type tags —
 * e.g. {@code enchantable/sword → #minecraft:swords}, {@code enchantable/sharp_weapon → #swords +
 * #axes}, {@code enchantable/durability → #swords, #axes, …, #head_armor, …}. So a modded sword put
 * in {@code #minecraft:swords} transitively becomes eligible for Sharpness/Smite/Knockback/Fire
 * Aspect/Looting/Sweeping Edge/Unbreaking/Mending/Curse of Vanishing — the vanilla sword list — and
 * for nothing else. Each armor piece likewise gets only its slot's enchantments.
 *
 * <p>Without these tags a modded item is a member of no {@code enchantable/*} tag, so in survival it
 * is enchantable with <i>nothing</i> (see {@code Enchantment#isSupportedItem}); only a creative
 * anvil bypasses the check. NeoForge does not auto-populate these tags.
 *
 * <p>Also emits the per-alloy repair tags ({@link ModAlloy#repairTag()} = the alloy ingot), which
 * is how 1.21.2+ tool/armor materials express their repair ingredient.
 */
public class ModItemTagsProvider extends ItemTagsProvider {

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, SyAlchemy.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (ModAlloy alloy : ModAlloy.values()) {
            add(alloy.repairTag(), ModItems.ALLOY_INGOTS.get(alloy).get());

            add(ItemTags.SWORDS, ModItems.ALLOY_SWORDS.get(alloy).get());
            add(ItemTags.PICKAXES, ModItems.ALLOY_PICKAXES.get(alloy).get());
            add(ItemTags.AXES, ModItems.ALLOY_AXES.get(alloy).get());
            add(ItemTags.SHOVELS, ModItems.ALLOY_SHOVELS.get(alloy).get());
            add(ItemTags.HOES, ModItems.ALLOY_HOES.get(alloy).get());

            add(ItemTags.HEAD_ARMOR, ModItems.ALLOY_HELMETS.get(alloy).get());
            add(ItemTags.CHEST_ARMOR, ModItems.ALLOY_CHESTPLATES.get(alloy).get());
            add(ItemTags.LEG_ARMOR, ModItems.ALLOY_LEGGINGS.get(alloy).get());
            add(ItemTags.FOOT_ARMOR, ModItems.ALLOY_BOOTS.get(alloy).get());
        }
    }

    private void add(TagKey<Item> tag, Item item) {
        tag(tag).add(key(item));
    }

    private static ResourceKey<Item> key(Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
    }
}
