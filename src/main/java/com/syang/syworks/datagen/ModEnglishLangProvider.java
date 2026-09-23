package com.syang.syworks.datagen;

import com.syang.syworks.SyWorks;
import com.syang.syworks.registry.ModBlocks;
import com.syang.syworks.world.level.block.ModMachine;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * Generates assets/syworks/lang/en_us.json. (Korean ko_kr.json is hand-written.)
 */
public class ModEnglishLangProvider extends LanguageProvider {

    public ModEnglishLangProvider(PackOutput output) {
        super(output, SyWorks.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.syworks.main", "SY Works");

        for (ModMachine machine : ModMachine.values()) {
            add(ModBlocks.MACHINES.get(machine).get(), machine.displayName());
        }
        add(ModBlocks.INCINERATOR.get(), "Incinerator");
    }
}
