package com.syang.yame.world.item;

import com.syang.yame.world.spell.SpellEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * The active-cast spells (see docs/진행상황.md §5.7.3). This is the single table-driven
 * definition of every spell — element, grade, cooldown, the book material it is crafted from,
 * and the {@link SpellAction cast behaviour} itself.
 *
 * <p>Each spell owns a "spellbook" item ({@code spellbook_<id>}) crafted at the Rune Altar
 * ({@code bookMaterial} + {@code runeCount}×rune) and bound onto a {@link ModStaff} at an anvil.
 *
 * <p>Cooldown is stored in <b>ticks</b> (20 t = 1 s). The effective cooldown at cast time is
 * shortened by the staff's cooldown-reduction and matching-element affinity (see
 * {@code StaffItem}).
 *
 * <p><b>Material-collision rule (§5.7.7):</b> every {@code bookMaterial} is a single item that is
 * <i>not</i> used by any of the 42 enchanted-book recipes, so the Rune Altar's exact base+catalyst
 * match stays unambiguous. Verify this when adding a spell.
 */
public enum ModSpell {
    // Cooldowns are the post-buff values: base cast rate was tripled (cd ÷3) so staff magic stays
    // relevant into the boss-tier game (§5.7.10). Damage/effect magnitudes are doubled in SpellEffects.
    // id               display            element         rune cd(t) bookMaterial                        action
    // Firebolt is the innate beginner spell every staff casts for free (see ModDataComponents);
    // its cooldown is deliberately the slowest of the cheap attacks so it stays an early-game tool.
    FIREBOLT("firebolt", "Firebolt", Element.FIRE, 1, 20, "minecraft:fire_charge", SpellEffects::firebolt),
    FROST_ARROW("frost_arrow", "Frost Arrow", Element.FROST, 1, 10, "minecraft:snowball", SpellEffects::frostArrow),
    LIGHTNING("lightning", "Lightning Strike", Element.STORM, 2, 27, "minecraft:copper_ingot", SpellEffects::lightning),
    BLIZZARD("blizzard", "Blizzard", Element.FROST, 3, 80, "minecraft:packed_ice", SpellEffects::blizzard),
    HEAL("heal", "Heal", Element.HOLY, 1, 40, "minecraft:glow_berries", SpellEffects::heal),
    REGENERATION("regeneration", "Regeneration", Element.HOLY, 2, 133, "minecraft:glistering_melon_slice", SpellEffects::regeneration),
    HASTE("haste", "Haste", Element.STORM, 2, 100, "minecraft:redstone", SpellEffects::haste),
    SHIELD("shield", "Shield", Element.HOLY, 1, 80, "minecraft:turtle_scute", SpellEffects::shield),
    POISON_CLOUD("poison_cloud", "Poison Cloud", Element.NATURE, 1, 40, "minecraft:wither_rose", SpellEffects::poisonCloud),
    CURSE("curse", "Curse", Element.SHADOW, 1, 53, "minecraft:ink_sac", SpellEffects::curse);

    /** The behaviour of a spell — invoked server-side with an already-computed power multiplier. */
    @FunctionalInterface
    public interface SpellAction {
        void cast(ServerLevel level, Player caster, ItemStack staff, float power);
    }

    private final String id;
    private final String displayName;
    private final Element element;
    private final int runeCount;
    private final int cooldownTicks;
    private final String bookMaterial;
    private final SpellAction action;

    ModSpell(String id, String displayName, Element element, int runeCount, int cooldownTicks,
             String bookMaterial, SpellAction action) {
        this.id = id;
        this.displayName = displayName;
        this.element = element;
        this.runeCount = runeCount;
        this.cooldownTicks = cooldownTicks;
        this.bookMaterial = bookMaterial;
        this.action = action;
    }

    /** Stable string id stored in the {@code bound_spells}/{@code active_spell} components. */
    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public Element element() {
        return element;
    }

    /** Runes required (as the altar catalyst count) to craft this spell's tome. */
    public int runeCount() {
        return runeCount;
    }

    public int cooldownTicks() {
        return cooldownTicks;
    }

    /** Vanilla item id used as the altar base to craft this spell's tome. */
    public String bookMaterial() {
        return bookMaterial;
    }

    /** Registry path of this spell's tome item. */
    public String bookName() {
        return "spellbook_" + id;
    }

    public void cast(ServerLevel level, Player caster, ItemStack staff, float power) {
        action.cast(level, caster, staff, power);
    }

    /** Resolve a spell from its stored string id, if any. */
    public static Optional<ModSpell> byId(String id) {
        for (ModSpell spell : values()) {
            if (spell.id.equals(id)) {
                return Optional.of(spell);
            }
        }
        return Optional.empty();
    }
}
