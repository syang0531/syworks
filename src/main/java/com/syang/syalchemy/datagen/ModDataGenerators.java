package com.syang.syalchemy.datagen;

import com.syang.syalchemy.SyAlchemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Hooks the data providers to {@code ./gradlew runData}. Output lands in
 * src/generated/resources, which build.gradle adds as a resource root.
 *
 * <p>NeoForge runs client <b>and</b> server providers in the single {@code clientData} run
 * (there is no separate server run configured), so everything is added on the Client event.
 */
@EventBusSubscriber(modid = SyAlchemy.MOD_ID)
public final class ModDataGenerators {

    private ModDataGenerators() {
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event) {
        // Client resources: blockstates + models + client item definitions, equipment (armor) assets, lang.
        event.createProvider(ModModelProvider::new);
        event.createProvider(ModEquipmentAssetProvider::new);
        event.createProvider(ModEnglishLangProvider::new);
        // Server data: crafting recipes (+ their unlock advancements) and item tags.
        event.createProvider(ModRecipeProvider.Runner::new);
        event.createProvider(ModItemTagsProvider::new);
    }
}
