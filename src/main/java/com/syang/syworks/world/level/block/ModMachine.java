package com.syang.syworks.world.level.block;

import com.mojang.serialization.Codec;
import com.syang.syworks.SyWorks;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;

/**
 * The machines, one row each. Everything that differs between them lives here; everything they
 * share lives in {@link MachineBlock}, {@code MachineBlockEntity}, {@code MachineMenu} and
 * {@code ProcessingRecipe}.
 *
 * <p>They are mechanically identical on purpose — fuel in, one item in, one item out, furnace-style
 * burn and progress — because the thing that tells them apart is <b>what they accept</b>, not how
 * they work. Vanilla does the same with the furnace, the smoker and the blast furnace: a player who
 * has used one has used all three. See docs/정체성-재설계.md §3.
 *
 * <p><b>광석은 굽고, 돌은 부수고, 나무는 숯으로.</b>
 *
 * <p>Adding a machine is this enum row, a block texture colour in
 * {@code tools/gen_block_textures.ps1}, and its recipes. The registries all iterate these values.
 */
public enum ModMachine implements StringRepresentable {
    /**
     * Roasting. Heats silk-touched ore so it yields more than smelting it would — the one place
     * this mod touches progression, and deliberately a trade with Fortune rather than a gain.
     */
    EXTRACTION_FURNACE("extraction_furnace", "Extraction Furnace", "extraction"),

    /**
     * Comminution. Takes every rock and every stair/wall/slab cut from one, plus dirt and calcite,
     * and grinds them down the one chain: rock → gravel → sand.
     */
    CRUSHER("crusher", "Crusher", "crushing");

    /** Used by the block codec, which has to name its machine when a block is serialized. */
    public static final Codec<ModMachine> CODEC = StringRepresentable.fromEnum(ModMachine::values);

    private final String id;
    private final String displayName;
    private final String recipeId;

    ModMachine(String id, String displayName, String recipeId) {
        this.id = id;
        this.displayName = displayName;
        this.recipeId = recipeId;
    }

    /** Registry path, shared by the block, block entity, menu and recipe type. */
    public String id() {
        return id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }

    /** English display name for data-generated en_us lang. */
    public String displayName() {
        return displayName;
    }

    public Identifier key() {
        return Identifier.fromNamespaceAndPath(SyWorks.MOD_ID, id);
    }

    /** Registry path of this machine's recipe type — the process, not the block ({ crushing}). */
    public String recipeId() {
        return recipeId;
    }

    public Identifier recipeKey() {
        return Identifier.fromNamespaceAndPath(SyWorks.MOD_ID, recipeId);
    }

    /** Translation key of the block, reused as the menu title. */
    public String translationKey() {
        return "block." + SyWorks.MOD_ID + "." + id;
    }
}
