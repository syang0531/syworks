package com.syang.yame.world.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * The 10 alloy equipment materials (see docs/기획.md §3–4).
 *
 * <p>Each alloy defines a tool {@link Tier} and an {@link ArmorPreset}. Balance follows
 * the four rules from the design doc:
 * <ul>
 *   <li>Copper-based → iron-like</li>
 *   <li>Iron-based → diamond-like</li>
 *   <li>Titanium-based → netherite-like</li>
 *   <li>Steel-based → beyond netherite (current cap ≈ ×1.3; ×2 reserved for Phase 2)</li>
 * </ul>
 *
 * <p>Tool stat columns: {@code uses}=내구도, {@code speed}=채굴속도,
 * {@code attackDamageBonus}=티어 공격 보너스 (검 공격력 = 1 + 보너스 + 3),
 * {@code enchantmentValue}=인챈트성.
 */
public enum ModAlloy {
    // name              display            uses  speed  atk  ench  mining              armor               fireResistant
    BRONZE("bronze", "Bronze", 230, 6.0F, 2.0F, 10, MiningLevel.IRON, ArmorPreset.IRON, false),
    BRASS("brass", "Brass", 180, 7.0F, 1.0F, 18, MiningLevel.IRON, ArmorPreset.IRON, false),
    CONSTANTAN("constantan", "Constantan", 320, 5.0F, 2.0F, 8, MiningLevel.IRON, ArmorPreset.IRON, false),
    DURALUMIN("duralumin", "Duralumin", 200, 9.0F, 1.0F, 12, MiningLevel.IRON, ArmorPreset.IRON, false),
    STEEL("steel", "Steel", 1400, 8.0F, 3.0F, 10, MiningLevel.DIAMOND, ArmorPreset.DIAMOND, false),
    STAINLESS_STEEL("stainless_steel", "Stainless Steel", 1800, 7.0F, 3.0F, 8, MiningLevel.DIAMOND, ArmorPreset.DIAMOND, false),
    TITANIUM_ALLOY("titanium_alloy", "Titanium Alloy", 2000, 10.0F, 4.0F, 14, MiningLevel.NETHERITE, ArmorPreset.NETHERITE, false),
    TUNGSTEN_STEEL("tungsten_steel", "Tungsten Steel", 2600, 9.0F, 5.0F, 12, MiningLevel.NETHERITE, ArmorPreset.SUPERIOR, false),
    COBALT_STEEL("cobalt_steel", "Cobalt Steel", 2400, 11.0F, 5.0F, 16, MiningLevel.NETHERITE, ArmorPreset.SUPERIOR, false),
    ELECTRUM("electrum", "Electrum", 150, 12.0F, 1.0F, 25, MiningLevel.IRON, ArmorPreset.IRON, false),
    // Phase 2 capstone — Tungsten Carbide (초경합금). ≈ ×2 netherite durability, fire-resistant.
    TUNGSTEN_CARBIDE("tungsten_carbide", "Tungsten Carbide", 4064, 11.0F, 6.0F, 15, MiningLevel.NETHERITE, ArmorPreset.CARBIDE, true),
    // Platinum-group extension — the absolute final tier (초경합금 + 백금). Deepest tech tree.
    PLATINUM_SUPERALLOY("platinum_superalloy", "Platinum Superalloy", 4600, 12.0F, 7.0F, 22, MiningLevel.NETHERITE, ArmorPreset.SUPERALLOY, true);

    private final String id;
    private final String displayName;
    private final int uses;
    private final float speed;
    private final float attackDamageBonus;
    private final int enchantmentValue;
    private final MiningLevel miningLevel;
    private final ArmorPreset armorPreset;
    private final boolean fireResistant;

    ModAlloy(String id, String displayName, int uses, float speed, float attackDamageBonus,
             int enchantmentValue, MiningLevel miningLevel, ArmorPreset armorPreset, boolean fireResistant) {
        this.id = id;
        this.displayName = displayName;
        this.uses = uses;
        this.speed = speed;
        this.attackDamageBonus = attackDamageBonus;
        this.enchantmentValue = enchantmentValue;
        this.miningLevel = miningLevel;
        this.armorPreset = armorPreset;
        this.fireResistant = fireResistant;
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public int enchantmentValue() {
        return enchantmentValue;
    }

    public ArmorPreset armorPreset() {
        return armorPreset;
    }

    /** Fire/lava-immune items (like netherite gear) — the Tungsten Carbide signature perk. */
    public boolean isFireResistant() {
        return fireResistant;
    }

    public String ingotName() {
        return id + "_ingot";
    }

    public String swordName() {
        return id + "_sword";
    }

    public String pickaxeName() {
        return id + "_pickaxe";
    }

    public String axeName() {
        return id + "_axe";
    }

    public String shovelName() {
        return id + "_shovel";
    }

    public String hoeName() {
        return id + "_hoe";
    }

    public String helmetName() {
        return id + "_helmet";
    }

    public String chestplateName() {
        return id + "_chestplate";
    }

    public String leggingsName() {
        return id + "_leggings";
    }

    public String bootsName() {
        return id + "_boots";
    }

    /**
     * Builds the tool tier for this alloy. Repair ingredient is the alloy's own ingot,
     * supplied lazily to avoid a registration ordering cycle.
     */
    public Tier createTier(Supplier<? extends ItemLike> repairIngredient) {
        return new ModTier(
                miningLevel.incorrectBlocksTag(),
                uses,
                speed,
                attackDamageBonus,
                enchantmentValue,
                () -> Ingredient.of(repairIngredient.get())
        );
    }

    /** Maps a balance bracket to the vanilla "blocks this tool cannot mine" tag. */
    public enum MiningLevel {
        IRON(BlockTags.INCORRECT_FOR_IRON_TOOL),
        DIAMOND(BlockTags.INCORRECT_FOR_DIAMOND_TOOL),
        NETHERITE(BlockTags.INCORRECT_FOR_NETHERITE_TOOL);

        private final TagKey<Block> tag;

        MiningLevel(TagKey<Block> tag) {
            this.tag = tag;
        }

        public TagKey<Block> incorrectBlocksTag() {
            return tag;
        }
    }

    /**
     * Armor stat preset. Defense is per slot (helmet/chest/leggings/boots);
     * {@code durabilityMult} is multiplied by the vanilla per-slot base durability.
     */
    public enum ArmorPreset {
        IRON(15, 2, 6, 5, 2, 0.0F, 0.0F),
        DIAMOND(33, 3, 8, 6, 3, 2.0F, 0.0F),
        NETHERITE(37, 3, 8, 6, 3, 3.0F, 0.1F),
        SUPERIOR(40, 4, 9, 7, 4, 4.0F, 0.15F),
        // Phase 2 capstone: ≈ ×2 netherite durability.
        CARBIDE(74, 4, 9, 7, 4, 5.0F, 0.2F),
        // Platinum extension: absolute top — extreme durability, toughness, knockback.
        SUPERALLOY(84, 4, 9, 7, 4, 6.0F, 0.3F);

        private final int durabilityMult;
        private final int helmet;
        private final int chest;
        private final int leggings;
        private final int boots;
        private final float toughness;
        private final float knockbackResistance;

        ArmorPreset(int durabilityMult, int helmet, int chest, int leggings, int boots,
                    float toughness, float knockbackResistance) {
            this.durabilityMult = durabilityMult;
            this.helmet = helmet;
            this.chest = chest;
            this.leggings = leggings;
            this.boots = boots;
            this.toughness = toughness;
            this.knockbackResistance = knockbackResistance;
        }

        public int durabilityMult() {
            return durabilityMult;
        }

        public int helmet() {
            return helmet;
        }

        public int chest() {
            return chest;
        }

        public int leggings() {
            return leggings;
        }

        public int boots() {
            return boots;
        }

        public float toughness() {
            return toughness;
        }

        public float knockbackResistance() {
            return knockbackResistance;
        }
    }
}
