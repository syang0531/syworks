package com.syang.syalchemy.datagen;

import com.syang.syalchemy.SyAlchemy;
import com.syang.syalchemy.registry.ModArmorMaterials;
import com.syang.syalchemy.world.item.ModAlloy;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.function.BiConsumer;

/**
 * Generates {@code assets/syalchemy/equipment/<alloy>.json} — the worn-armor model definition each
 * {@link ModArmorMaterials} material points at. Both humanoid layers reference the alloy id, i.e.
 * {@code textures/entity/equipment/humanoid/<alloy>.png} (helmet/chest/boots) and
 * {@code textures/entity/equipment/humanoid_leggings/<alloy>.png} (leggings).
 */
public class ModEquipmentAssetProvider extends EquipmentAssetProvider {

    public ModEquipmentAssetProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
        for (ModAlloy alloy : ModAlloy.values()) {
            output.accept(ModArmorMaterials.ASSETS.get(alloy), EquipmentClientInfo.builder()
                    .addHumanoidLayers(Identifier.fromNamespaceAndPath(SyAlchemy.MOD_ID, alloy.id()))
                    .build());
        }
    }
}
