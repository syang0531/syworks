package com.syang.mcextraction.world.item;

/**
 * The pure metals extracted from vanilla natural blocks (see docs/기획.md §2).
 *
 * <p>These are raw materials only — they are combined in the Alloy Furnace to make
 * the {@link ModAlloy} equipment materials. Vanilla copper / iron / gold are reused
 * directly and are not listed here.
 */
public enum ModMetal {
    TIN("tin_ingot", "Tin Ingot", false),
    ZINC("zinc_ingot", "Zinc Ingot", false),
    NICKEL("nickel_ingot", "Nickel Ingot", false),
    ALUMINUM("aluminum_ingot", "Aluminum Ingot", false),
    SILVER("silver_ingot", "Silver Ingot", false),
    CHROMIUM("chromium_ingot", "Chromium Ingot", false),
    TITANIUM("titanium_ingot", "Titanium Ingot", false),
    COBALT("cobalt_ingot", "Cobalt Ingot", false),
    TUNGSTEN("tungsten_ingot", "Tungsten Ingot", false),
    // Sulfur is a non-metal byproduct; kept here as a raw extracted material.
    SULFUR("sulfur", "Sulfur", false),
    // Platinum group — extracted from Ancient Debris at very low yield; fire-immune.
    PLATINUM("platinum_ingot", "Platinum Ingot", true);

    private final String itemName;
    private final String displayName;
    private final boolean fireResistant;

    ModMetal(String itemName, String displayName, boolean fireResistant) {
        this.itemName = itemName;
        this.displayName = displayName;
        this.fireResistant = fireResistant;
    }

    /** Registry path, e.g. {@code tin_ingot}. */
    public String itemName() {
        return itemName;
    }

    /** English display name for data-generated en_us lang. */
    public String displayName() {
        return displayName;
    }

    public boolean isFireResistant() {
        return fireResistant;
    }
}
