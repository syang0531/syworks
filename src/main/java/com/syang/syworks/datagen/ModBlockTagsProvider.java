package com.syang.syworks.datagen;

import com.syang.syworks.SyWorks;
import com.syang.syworks.registry.ModBlocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

import java.util.concurrent.CompletableFuture;

/**
 * Every block is mined with a pickaxe. The blocks set {@code requiresCorrectToolForDrops}, and a
 * block outside every {@code mineable/*} tag has no correct tool: it would mine at bare-hand speed
 * and drop nothing even with a loot table.
 */
public class ModBlockTagsProvider extends BlockTagsProvider {

    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, SyWorks.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        var pickaxe = tag(BlockTags.MINEABLE_WITH_PICKAXE);
        ModBlocks.BLOCKS.getEntries().forEach(holder -> pickaxe.add(holder.getKey()));
    }
}
