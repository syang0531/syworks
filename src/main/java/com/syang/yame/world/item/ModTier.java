package com.syang.yame.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

/**
 * A simple {@link Tier} implementation for alloy tools.
 *
 * @param incorrectBlocksForDrops blocks this tier CANNOT harvest (vanilla-style tag)
 * @param uses                    durability
 * @param speed                   mining speed
 * @param attackDamageBonus       added to base attack damage
 * @param enchantmentValue        enchantability
 * @param repairIngredient        lazily-supplied repair material
 */
public record ModTier(
        TagKey<Block> incorrectBlocksForDrops,
        int uses,
        float speed,
        float attackDamageBonus,
        int enchantmentValue,
        Supplier<Ingredient> repairIngredient
) implements Tier {

    @Override
    public int getUses() {
        return uses;
    }

    @Override
    public float getSpeed() {
        return speed;
    }

    @Override
    public float getAttackDamageBonus() {
        return attackDamageBonus;
    }

    @Override
    public TagKey<Block> getIncorrectBlocksForDrops() {
        return incorrectBlocksForDrops;
    }

    @Override
    public int getEnchantmentValue() {
        return enchantmentValue;
    }

    @Override
    public Ingredient getRepairIngredient() {
        return repairIngredient.get();
    }
}
