package com.syang.mcextraction.datagen;

import com.syang.mcextraction.MCExtraction;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Hooks the data providers to {@code ./gradlew runData}. Output lands in
 * src/generated/resources, which build.gradle adds as a resource root.
 */
@EventBusSubscriber(modid = MCExtraction.MOD_ID, bus = EventBusSubscriber.Bus.MOD)
public final class ModDataGenerators {

    private ModDataGenerators() {
    }

    @SubscribeEvent
    public static void onGatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeClient(), new ModItemModelProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(output, existingFileHelper));
        generator.addProvider(event.includeClient(), new ModEnglishLangProvider(output));
        generator.addProvider(event.includeServer(), new ModRecipeProvider(output, event.getLookupProvider()));
    }
}
