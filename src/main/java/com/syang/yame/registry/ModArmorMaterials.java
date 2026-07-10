package com.syang.yame.registry;

import com.syang.yame.Yame;
import com.syang.yame.world.item.ModAlloy;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Registers a custom {@link ArmorMaterial} per alloy. The material's repair ingredient
 * is the alloy ingot; armor body-layer textures live at
 * assets/yame/textures/models/armor/&lt;alloy&gt;_layer_1.png (and _layer_2 for leggings).
 */
public final class ModArmorMaterials {

    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, Yame.MOD_ID);

    public static final Map<ModAlloy, Holder<ArmorMaterial>> BY_ALLOY = new EnumMap<>(ModAlloy.class);

    static {
        for (ModAlloy alloy : ModAlloy.values()) {
            BY_ALLOY.put(alloy, ARMOR_MATERIALS.register(alloy.id(), () -> createMaterial(alloy)));
        }
    }

    private ModArmorMaterials() {
    }

    private static ArmorMaterial createMaterial(ModAlloy alloy) {
        ModAlloy.ArmorPreset preset = alloy.armorPreset();

        Map<ArmorItem.Type, Integer> defense = new EnumMap<>(ArmorItem.Type.class);
        defense.put(ArmorItem.Type.HELMET, preset.helmet());
        defense.put(ArmorItem.Type.CHESTPLATE, preset.chest());
        defense.put(ArmorItem.Type.LEGGINGS, preset.leggings());
        defense.put(ArmorItem.Type.BOOTS, preset.boots());
        defense.put(ArmorItem.Type.BODY, preset.chest());

        return new ArmorMaterial(
                defense,
                alloy.enchantmentValue(),
                SoundEvents.ARMOR_EQUIP_IRON,
                () -> Ingredient.of(ModItems.ALLOY_INGOTS.get(alloy).get()),
                List.of(new ArmorMaterial.Layer(
                        ResourceLocation.fromNamespaceAndPath(Yame.MOD_ID, alloy.id()))),
                preset.toughness(),
                preset.knockbackResistance()
        );
    }

    public static void register(IEventBus modBus) {
        ARMOR_MATERIALS.register(modBus);
    }
}
