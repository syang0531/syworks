package com.syang.syworks.datagen;

import com.syang.syworks.SyWorks;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

/**
 * The crusher's input tags. Everything the crusher accepts is listed here rather than in a hundred
 * recipe files: one tag, one recipe.
 *
 * <p><b>Why there is a {@code _half} tag for every family.</b> A stonecutter turns one block into
 * <i>two</i> slabs, while stairs, walls and polished cuts are all 1:1. If a slab counted as a whole
 * block, cutting a block into slabs and crushing them would yield twice as much as crushing the
 * block — an infinite gravel loop. So slabs go in their own tag and their recipe consumes two.
 * See docs/정체성-재설계.md §6.
 *
 * <p>Deliberately <b>not</b> included: gilded blackstone (it holds gold), sandstone (it is made
 * from sand in the first place, so crushing it back is pointless), and anything from the End.
 */
public class ModItemTagsProvider extends ItemTagsProvider {

    /** Rock and every 1:1 cut of it → gravel. */
    public static final TagKey<Item> CRUSHABLE_ROCK = tag("crushable/rock");
    /** Stone slabs, worth half a block each → gravel. */
    public static final TagKey<Item> CRUSHABLE_ROCK_HALF = tag("crushable/rock_half");
    /** Tuff and its 1:1 cuts → clay. Volcanic ash is the one rock that really alters into clay. */
    public static final TagKey<Item> CRUSHABLE_TUFF = tag("crushable/tuff");
    /** Tuff slabs → clay. */
    public static final TagKey<Item> CRUSHABLE_TUFF_HALF = tag("crushable/tuff_half");
    /** Calcium carbonate → bone meal. Ground limestone is agricultural lime. */
    public static final TagKey<Item> CRUSHABLE_LIME = tag("crushable/lime");
    /** Soil → sand, skipping gravel: soil is already sand, silt and clay, not rock. */
    public static final TagKey<Item> CRUSHABLE_SOIL = tag("crushable/soil");

    /**
     * Kiln, plank tier → 1 charcoal per 2. A plank is a quarter of a log and a log gives 2, so two
     * planks are exactly break-even; everything else in this tier costs more wood than a plank and
     * is therefore a loss. No crafting route gains charcoal.
     */
    public static final TagKey<Item> CHARRABLE_PLANK = tag("charrable/plank");
    /**
     * Kiln, small tier → 1 charcoal per 4. A slab and a stick are each an eighth of a log, so four
     * of either is break-even; buttons, ladders and bowls cost more and are a loss.
     */
    public static final TagKey<Item> CHARRABLE_SMALL = tag("charrable/small");

    private static TagKey<Item> tag(String path) {
        return ItemTags.create(Identifier.fromNamespaceAndPath(SyWorks.MOD_ID, path));
    }

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, SyWorks.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // ----- stone -----
        rock(Items.STONE, Items.COBBLESTONE, Items.MOSSY_COBBLESTONE, Items.SMOOTH_STONE,
                Items.STONE_BRICKS, Items.MOSSY_STONE_BRICKS, Items.CRACKED_STONE_BRICKS,
                Items.CHISELED_STONE_BRICKS,
                Items.STONE_STAIRS, Items.COBBLESTONE_STAIRS, Items.MOSSY_COBBLESTONE_STAIRS,
                Items.STONE_BRICK_STAIRS, Items.MOSSY_STONE_BRICK_STAIRS,
                Items.COBBLESTONE_WALL, Items.MOSSY_COBBLESTONE_WALL,
                Items.STONE_BRICK_WALL, Items.MOSSY_STONE_BRICK_WALL);
        half(Items.STONE_SLAB, Items.COBBLESTONE_SLAB, Items.MOSSY_COBBLESTONE_SLAB,
                Items.SMOOTH_STONE_SLAB, Items.STONE_BRICK_SLAB, Items.MOSSY_STONE_BRICK_SLAB);

        // ----- granite / diorite / andesite -----
        rock(Items.GRANITE, Items.POLISHED_GRANITE, Items.GRANITE_STAIRS,
                Items.POLISHED_GRANITE_STAIRS, Items.GRANITE_WALL);
        half(Items.GRANITE_SLAB, Items.POLISHED_GRANITE_SLAB);

        rock(Items.DIORITE, Items.POLISHED_DIORITE, Items.DIORITE_STAIRS,
                Items.POLISHED_DIORITE_STAIRS, Items.DIORITE_WALL);
        half(Items.DIORITE_SLAB, Items.POLISHED_DIORITE_SLAB);

        rock(Items.ANDESITE, Items.POLISHED_ANDESITE, Items.ANDESITE_STAIRS,
                Items.POLISHED_ANDESITE_STAIRS, Items.ANDESITE_WALL);
        half(Items.ANDESITE_SLAB, Items.POLISHED_ANDESITE_SLAB);

        // ----- deepslate -----
        rock(Items.DEEPSLATE, Items.COBBLED_DEEPSLATE, Items.POLISHED_DEEPSLATE, Items.CHISELED_DEEPSLATE,
                Items.DEEPSLATE_BRICKS, Items.CRACKED_DEEPSLATE_BRICKS,
                Items.DEEPSLATE_TILES, Items.CRACKED_DEEPSLATE_TILES,
                Items.COBBLED_DEEPSLATE_STAIRS, Items.POLISHED_DEEPSLATE_STAIRS,
                Items.DEEPSLATE_BRICK_STAIRS, Items.DEEPSLATE_TILE_STAIRS,
                Items.COBBLED_DEEPSLATE_WALL, Items.POLISHED_DEEPSLATE_WALL,
                Items.DEEPSLATE_BRICK_WALL, Items.DEEPSLATE_TILE_WALL);
        half(Items.COBBLED_DEEPSLATE_SLAB, Items.POLISHED_DEEPSLATE_SLAB,
                Items.DEEPSLATE_BRICK_SLAB, Items.DEEPSLATE_TILE_SLAB);

        // ----- nether rock -----
        rock(Items.BLACKSTONE, Items.POLISHED_BLACKSTONE, Items.CHISELED_POLISHED_BLACKSTONE,
                Items.POLISHED_BLACKSTONE_BRICKS, Items.CRACKED_POLISHED_BLACKSTONE_BRICKS,
                Items.BLACKSTONE_STAIRS, Items.POLISHED_BLACKSTONE_STAIRS,
                Items.POLISHED_BLACKSTONE_BRICK_STAIRS,
                Items.BLACKSTONE_WALL, Items.POLISHED_BLACKSTONE_WALL,
                Items.POLISHED_BLACKSTONE_BRICK_WALL);
        half(Items.BLACKSTONE_SLAB, Items.POLISHED_BLACKSTONE_SLAB,
                Items.POLISHED_BLACKSTONE_BRICK_SLAB);

        rock(Items.BASALT, Items.POLISHED_BASALT, Items.SMOOTH_BASALT, Items.NETHERRACK);

        // ----- tuff: the one rock that really becomes clay -----
        add(CRUSHABLE_TUFF, Items.TUFF, Items.POLISHED_TUFF, Items.CHISELED_TUFF,
                Items.TUFF_BRICKS, Items.CHISELED_TUFF_BRICKS,
                Items.TUFF_STAIRS, Items.POLISHED_TUFF_STAIRS, Items.TUFF_BRICK_STAIRS,
                Items.TUFF_WALL, Items.POLISHED_TUFF_WALL, Items.TUFF_BRICK_WALL);
        add(CRUSHABLE_TUFF_HALF, Items.TUFF_SLAB, Items.POLISHED_TUFF_SLAB, Items.TUFF_BRICK_SLAB);

        // ----- lime and soil -----
        add(CRUSHABLE_LIME, Items.CALCITE, Items.DRIPSTONE_BLOCK);
        add(CRUSHABLE_SOIL, Items.DIRT, Items.COARSE_DIRT, Items.ROOTED_DIRT);

        // ----- kiln: the wooden things a furnace refuses -----
        // Logs are not listed: they come in through the vanilla #minecraft:logs_that_burn tag, which
        // already excludes crimson and warped (fungus, not wood) and bamboo (a grass).
        wood(Items.OAK_PLANKS, Items.OAK_STAIRS, Items.OAK_SLAB, Items.OAK_FENCE, Items.OAK_FENCE_GATE,
                Items.OAK_DOOR, Items.OAK_TRAPDOOR, Items.OAK_SIGN, Items.OAK_BUTTON, Items.OAK_PRESSURE_PLATE);
        wood(Items.SPRUCE_PLANKS, Items.SPRUCE_STAIRS, Items.SPRUCE_SLAB, Items.SPRUCE_FENCE, Items.SPRUCE_FENCE_GATE,
                Items.SPRUCE_DOOR, Items.SPRUCE_TRAPDOOR, Items.SPRUCE_SIGN, Items.SPRUCE_BUTTON, Items.SPRUCE_PRESSURE_PLATE);
        wood(Items.BIRCH_PLANKS, Items.BIRCH_STAIRS, Items.BIRCH_SLAB, Items.BIRCH_FENCE, Items.BIRCH_FENCE_GATE,
                Items.BIRCH_DOOR, Items.BIRCH_TRAPDOOR, Items.BIRCH_SIGN, Items.BIRCH_BUTTON, Items.BIRCH_PRESSURE_PLATE);
        wood(Items.JUNGLE_PLANKS, Items.JUNGLE_STAIRS, Items.JUNGLE_SLAB, Items.JUNGLE_FENCE, Items.JUNGLE_FENCE_GATE,
                Items.JUNGLE_DOOR, Items.JUNGLE_TRAPDOOR, Items.JUNGLE_SIGN, Items.JUNGLE_BUTTON, Items.JUNGLE_PRESSURE_PLATE);
        wood(Items.ACACIA_PLANKS, Items.ACACIA_STAIRS, Items.ACACIA_SLAB, Items.ACACIA_FENCE, Items.ACACIA_FENCE_GATE,
                Items.ACACIA_DOOR, Items.ACACIA_TRAPDOOR, Items.ACACIA_SIGN, Items.ACACIA_BUTTON, Items.ACACIA_PRESSURE_PLATE);
        wood(Items.DARK_OAK_PLANKS, Items.DARK_OAK_STAIRS, Items.DARK_OAK_SLAB, Items.DARK_OAK_FENCE, Items.DARK_OAK_FENCE_GATE,
                Items.DARK_OAK_DOOR, Items.DARK_OAK_TRAPDOOR, Items.DARK_OAK_SIGN, Items.DARK_OAK_BUTTON, Items.DARK_OAK_PRESSURE_PLATE);
        wood(Items.MANGROVE_PLANKS, Items.MANGROVE_STAIRS, Items.MANGROVE_SLAB, Items.MANGROVE_FENCE, Items.MANGROVE_FENCE_GATE,
                Items.MANGROVE_DOOR, Items.MANGROVE_TRAPDOOR, Items.MANGROVE_SIGN, Items.MANGROVE_BUTTON, Items.MANGROVE_PRESSURE_PLATE);
        wood(Items.CHERRY_PLANKS, Items.CHERRY_STAIRS, Items.CHERRY_SLAB, Items.CHERRY_FENCE, Items.CHERRY_FENCE_GATE,
                Items.CHERRY_DOOR, Items.CHERRY_TRAPDOOR, Items.CHERRY_SIGN, Items.CHERRY_BUTTON, Items.CHERRY_PRESSURE_PLATE);
        wood(Items.PALE_OAK_PLANKS, Items.PALE_OAK_STAIRS, Items.PALE_OAK_SLAB, Items.PALE_OAK_FENCE, Items.PALE_OAK_FENCE_GATE,
                Items.PALE_OAK_DOOR, Items.PALE_OAK_TRAPDOOR, Items.PALE_OAK_SIGN, Items.PALE_OAK_BUTTON, Items.PALE_OAK_PRESSURE_PLATE);

        add(CHARRABLE_SMALL, Items.STICK, Items.LADDER, Items.BOWL);
    }

    /** One wood type. Hanging signs are left out on purpose — they hold iron chains. */
    private void wood(Item planks, Item stairs, Item slab, Item fence, Item gate,
                      Item door, Item trapdoor, Item sign, Item button, Item plate) {
        add(CHARRABLE_PLANK, planks, stairs, fence, gate, door, trapdoor, sign, plate);
        add(CHARRABLE_SMALL, slab, button);
    }

    private void rock(Item... items) {
        add(CRUSHABLE_ROCK, items);
    }

    private void half(Item... items) {
        add(CRUSHABLE_ROCK_HALF, items);
    }

    private void add(TagKey<Item> tag, Item... items) {
        var builder = tag(tag);
        for (Item item : items) {
            builder.add(key(item));
        }
    }

    private static ResourceKey<Item> key(Item item) {
        return BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow();
    }
}
