package com.syang.mcextraction.world.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * A spell tome — the anvil-binding material that teaches a {@link ModSpell} to a {@link WandItem}.
 * One tome item exists per spell, so the Rune Altar recipe and the anvil binding both reference a
 * plain item (no component needed). The tome knows its own spell for the anvil handler.
 */
public class SpellBookItem extends Item {

    private final ModSpell spell;

    public SpellBookItem(ModSpell spell, Properties properties) {
        super(properties);
        this.spell = spell;
    }

    public ModSpell spell() {
        return spell;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.literal(spell.element().glyph() + " " + spell.element().displayName())
                .withStyle(spell.element().color()));
        tooltip.add(Component.translatable("tooltip.mcextraction.spellbook.bind").withStyle(ChatFormatting.DARK_GRAY));
    }
}
