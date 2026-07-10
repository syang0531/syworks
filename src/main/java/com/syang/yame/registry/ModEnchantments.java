package com.syang.yame.registry;

import com.syang.yame.Yame;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

/**
 * Staff-exclusive enchantments (§5.7.9). These are data-driven enchantments defined in
 * {@code data/yame/enchantment/*.json}; their <i>gameplay</i> is applied here in code because a
 * "spell power" concept is a mod invention with no vanilla effect component. The JSONs only carry
 * the metadata (name, cost, {@code supported_items = #yame:enchantable/staff}) so the enchanted
 * books bind to a staff at an anvil.
 *
 * <p>They are obtained as enchanted books at the Rune Altar (like the 42 vanilla ones) and are not
 * offered at an enchanting table — a {@link com.syang.yame.world.item.StaffItem} is a plain
 * (non-tiered) item with enchantment value 0, so only the anvil/book path applies them, exactly as
 * intended for the book-centric magic system.
 */
public final class ModEnchantments {

    /** Item tag that gates every staff-exclusive enchantment (populated in {@code ModItemTagsProvider}). */
    public static final TagKey<Item> STAFF_ENCHANTABLE =
            ItemTags.create(ResourceLocation.fromNamespaceAndPath(Yame.MOD_ID, "enchantable/staff"));

    /** +20% spell damage / heal / effect magnitude per level (I–V). Like Sharpness, but for magic. */
    public static final ResourceKey<Enchantment> SPELL_POWER = key("spell_power");

    /** −10% effective cooldown per level (I–IV) — faster casting. */
    public static final ResourceKey<Enchantment> ALACRITY = key("alacrity");

    /** +25% area-of-effect radius and projectile speed per level (I–III). */
    public static final ResourceKey<Enchantment> ARCANE_REACH = key("arcane_reach");

    private ModEnchantments() {
    }

    private static ResourceKey<Enchantment> key(String path) {
        return ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.fromNamespaceAndPath(Yame.MOD_ID, path));
    }

    /** Level of {@code key} on {@code stack}, or 0 if absent. Resolves the datapack enchantment holder. */
    public static int level(ServerLevel level, ItemStack stack, ResourceKey<Enchantment> key) {
        Holder<Enchantment> holder = level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
        return EnchantmentHelper.getItemEnchantmentLevel(holder, stack);
    }

    /** Spell-power damage/effect multiplier for {@code stack}: 1.0 + 0.20·level. */
    public static float spellPower(ServerLevel level, ItemStack stack) {
        return 1.0F + 0.20F * level(level, stack, SPELL_POWER);
    }

    /** Cooldown multiplier for {@code stack}: 1.0 − 0.10·level, floored at 0.20 (−80% cap). */
    public static float cooldown(ServerLevel level, ItemStack stack) {
        return Math.max(0.20F, 1.0F - 0.10F * level(level, stack, ALACRITY));
    }

    /** Area/projectile-reach multiplier for {@code stack}: 1.0 + 0.25·level. */
    public static float reach(ServerLevel level, ItemStack stack) {
        return 1.0F + 0.25F * level(level, stack, ARCANE_REACH);
    }
}
