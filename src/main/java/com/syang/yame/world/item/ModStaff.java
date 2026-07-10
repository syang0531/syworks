package com.syang.yame.world.item;

import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.function.Supplier;

/**
 * The 18 staffs (see docs/진행상황.md §5.7.2): 6 vanilla sword materials + the 12 mod alloys.
 * Table-driven exactly like {@link ModAlloy} — each row is affinity element + spell stats, and
 * the item, model, lang, and crafting recipe are all generated from it.
 *
 * <p>Stat columns: {@code power}=주문위력(cast power ×), {@code cooldownReduction}=쿨감(base),
 * {@code affinityPowerBonus}/{@code affinityCooldownBonus}=친화 보너스 (only applied when the cast
 * spell's element matches {@link #affinity}), {@code slots}=담을 수 있는 마법 개수, {@code durability}=내구.
 *
 * <p>A {@code null} affinity means "all elements" (the Platinum Superalloy capstone), which
 * matches every spell.
 */
public enum ModStaff {
    // ----- vanilla materials -----
    // id             display          alloy               vanillaMaterial                           affinity        pow    cdR    affPow affCd  slot dur   fire
    WOODEN("wooden_staff", "Wooden Staff", null, () -> Ingredient.of(ItemTags.PLANKS), Element.NATURE, 0.85F, 0.00F, 0.15F, 0.00F, 1, 60, false),
    STONE("stone_staff", "Stone Staff", null, () -> Ingredient.of(Items.COBBLESTONE), Element.FIRE, 0.90F, 0.00F, 0.20F, 0.00F, 2, 130, false),
    IRON("iron_staff", "Iron Staff", null, () -> Ingredient.of(Items.IRON_INGOT), Element.FROST, 1.00F, 0.00F, 0.20F, 0.00F, 2, 250, false),
    GOLDEN("golden_staff", "Golden Staff", null, () -> Ingredient.of(Items.GOLD_INGOT), Element.HOLY, 0.95F, 0.10F, 0.30F, 0.00F, 2, 100, false),
    DIAMOND("diamond_staff", "Diamond Staff", null, () -> Ingredient.of(Items.DIAMOND), Element.HOLY, 1.15F, 0.05F, 0.25F, 0.00F, 3, 900, false),
    NETHERITE("netherite_staff", "Netherite Staff", null, () -> Ingredient.of(Items.NETHERITE_INGOT), Element.SHADOW, 1.35F, 0.05F, 0.30F, 0.00F, 3, 2031, true),

    // ----- mod alloys -----
    BRONZE("bronze_staff", "Bronze Staff", ModAlloy.BRONZE, null, Element.FIRE, 1.00F, 0.00F, 0.25F, 0.00F, 2, 250, false),
    BRASS("brass_staff", "Brass Staff", ModAlloy.BRASS, null, Element.NATURE, 0.90F, 0.20F, 0.00F, 0.20F, 3, 180, false),
    CONSTANTAN("constantan_staff", "Constantan Staff", ModAlloy.CONSTANTAN, null, Element.NATURE, 1.00F, -0.05F, 0.20F, 0.00F, 2, 400, false),
    DURALUMIN("duralumin_staff", "Duralumin Staff", ModAlloy.DURALUMIN, null, Element.STORM, 0.90F, 0.15F, 0.00F, 0.20F, 2, 200, false),
    STEEL("steel_staff", "Steel Staff", ModAlloy.STEEL, null, Element.FROST, 1.15F, 0.05F, 0.25F, 0.00F, 3, 800, false),
    STAINLESS_STEEL("stainless_steel_staff", "Stainless Steel Staff", ModAlloy.STAINLESS_STEEL, null, Element.FROST, 1.10F, -0.05F, 0.25F, 0.00F, 3, 1100, false),
    TITANIUM_ALLOY("titanium_alloy_staff", "Titanium Alloy Staff", ModAlloy.TITANIUM_ALLOY, null, Element.STORM, 1.25F, 0.10F, 0.00F, 0.25F, 3, 1400, false),
    TUNGSTEN_STEEL("tungsten_steel_staff", "Tungsten Steel Staff", ModAlloy.TUNGSTEN_STEEL, null, Element.SHADOW, 1.45F, -0.10F, 0.30F, 0.00F, 3, 1600, false),
    COBALT_STEEL("cobalt_steel_staff", "Cobalt Steel Staff", ModAlloy.COBALT_STEEL, null, Element.SHADOW, 1.35F, 0.05F, 0.30F, 0.00F, 3, 1400, false),
    ELECTRUM("electrum_staff", "Electrum Staff", ModAlloy.ELECTRUM, null, Element.STORM, 1.10F, 0.35F, 0.00F, 0.25F, 3, 120, false),
    TUNGSTEN_CARBIDE("tungsten_carbide_staff", "Tungsten Carbide Staff", ModAlloy.TUNGSTEN_CARBIDE, null, Element.FIRE, 1.35F, 0.00F, 0.30F, 0.00F, 3, 1800, true),
    // Capstone: affinity = all elements (null).
    PLATINUM_SUPERALLOY("platinum_superalloy_staff", "Platinum Superalloy Staff", ModAlloy.PLATINUM_SUPERALLOY, null, null, 1.30F, 0.15F, 0.15F, 0.10F, 4, 2000, true);

    /** Never let a staff cast faster than the global-cooldown floor (0.25 s). */
    private static final int MIN_COOLDOWN_TICKS = 5;

    private final String id;
    private final String displayName;
    private final ModAlloy alloy;
    private final Supplier<Ingredient> vanillaMaterial;
    private final Element affinity;
    private final float power;
    private final float cooldownReduction;
    private final float affinityPowerBonus;
    private final float affinityCooldownBonus;
    private final int slots;
    private final int durability;
    private final boolean fireResistant;

    ModStaff(String id, String displayName, ModAlloy alloy, Supplier<Ingredient> vanillaMaterial,
            Element affinity, float power, float cooldownReduction, float affinityPowerBonus,
            float affinityCooldownBonus, int slots, int durability, boolean fireResistant) {
        this.id = id;
        this.displayName = displayName;
        this.alloy = alloy;
        this.vanillaMaterial = vanillaMaterial;
        this.affinity = affinity;
        this.power = power;
        this.cooldownReduction = cooldownReduction;
        this.affinityPowerBonus = affinityPowerBonus;
        this.affinityCooldownBonus = affinityCooldownBonus;
        this.slots = slots;
        this.durability = durability;
        this.fireResistant = fireResistant;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    /** The mod alloy this staff is made of, or {@code null} for a vanilla-material staff. */
    public ModAlloy alloy() {
        return alloy;
    }

    /** Crafting/repair ingredient for vanilla-material staffs ({@code null} for alloy staffs). */
    public Supplier<Ingredient> vanillaMaterial() {
        return vanillaMaterial;
    }

    public Element affinity() {
        return affinity;
    }

    public int slots() {
        return slots;
    }

    public int durability() {
        return durability;
    }

    public boolean isFireResistant() {
        return fireResistant;
    }

    /** True when a spell's element benefits from this staff's affinity ({@code null} = all). */
    public boolean matchesAffinity(Element element) {
        return affinity == null || affinity == element;
    }

    /** Cast-power multiplier for a spell, including the affinity bonus when it applies. */
    public float effectivePower(ModSpell spell) {
        float bonus = matchesAffinity(spell.element()) ? affinityPowerBonus : 0.0F;
        return power * (1.0F + bonus);
    }

    /** Cooldown in ticks for a spell, shortened by base cooldown-reduction + affinity bonus. */
    public int effectiveCooldown(ModSpell spell) {
        float reduction = cooldownReduction + (matchesAffinity(spell.element()) ? affinityCooldownBonus : 0.0F);
        int ticks = Math.round(spell.cooldownTicks() * (1.0F - reduction));
        return Math.max(MIN_COOLDOWN_TICKS, ticks);
    }
}
