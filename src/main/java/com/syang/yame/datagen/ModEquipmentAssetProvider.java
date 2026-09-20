package com.syang.yame.datagen;

import com.syang.yame.Yame;
import com.syang.yame.registry.ModArmorMaterials;
import com.syang.yame.world.item.ModAlloy;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

import java.util.function.BiConsumer;

/**
 * Generates {@code assets/yame/equipment/<alloy>.json} — the worn-armor model definition each
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
                    .addHumanoidLayers(Identifier.fromNamespaceAndPath(Yame.MOD_ID, alloy.id()))
                    .build());
        }
    }
}
