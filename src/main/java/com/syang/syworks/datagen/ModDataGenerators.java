package com.syang.syworks.datagen;

import com.syang.syworks.SyWorks;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;

/**
 * Hooks the data providers to {@code ./gradlew runData}. Output lands in
 * src/generated/resources, which build.gradle adds as a resource root.
 *
 * <p>NeoForge runs client <b>and</b> server providers in the single {@code clientData} run
 * (there is no separate server run configured), so everything is added on the Client event.
 */
@EventBusSubscriber(modid = SyWorks.MOD_ID)
public final class ModDataGenerators {

    private ModDataGenerators() {
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModEnglishLangProvider::new);
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModBlockTagsProvider::new);
        event.createProvider((output, registries) -> new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLootProvider::new, LootContextParamSets.BLOCK)),
                registries));
    }
}
