package com.syang.yame.world.item;

import net.minecraft.ChatFormatting;

/**
 * The six magic elements (see docs/진행상황.md §5.7.1). Every {@link ModSpell} has an element,
 * every {@link ModWand} has an affinity element; when they match the wand grants a bonus.
 */
public enum Element {
    FIRE("Fire", "🔥", ChatFormatting.RED),
    FROST("Frost", "❄", ChatFormatting.AQUA),
    STORM("Storm", "⚡", ChatFormatting.YELLOW),
    HOLY("Holy", "✨", ChatFormatting.WHITE),
    NATURE("Nature", "☘", ChatFormatting.GREEN),
    SHADOW("Shadow", "🌑", ChatFormatting.DARK_PURPLE);

    private final String displayName;
    private final String glyph;
    private final ChatFormatting color;

    Element(String displayName, String glyph, ChatFormatting color) {
        this.displayName = displayName;
        this.glyph = glyph;
        this.color = color;
    }

    public String displayName() {
        return displayName;
    }

    /** A short emoji glyph for HUD/tooltip flavour. */
    public String glyph() {
        return glyph;
    }

    /** Themed text colour used in tooltips and the HUD. */
    public ChatFormatting color() {
        return color;
    }
}
