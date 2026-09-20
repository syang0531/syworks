package com.syang.syalchemy.registry;

import com.syang.syalchemy.SyAlchemy;
import com.syang.syalchemy.world.item.ModAlloy;
import com.syang.syalchemy.world.item.ModMetal;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;

/**
 * All items: pure metal ingots, alloy ingots, and a full tool + armor set per alloy.
 * BlockItems for {@link ModBlocks} are registered into this same register.
 *
 * <p>Since 1.21.2 swords, pickaxes and armor are plain {@link Item}s whose behaviour comes
 * entirely from data components set through {@code Item.Properties} ({@code sword}, {@code
 * pickaxe}, {@code humanoidArmor}); only axes, shovels and hoes keep a class because they still
 * have a right-click action (strip / path / till).
 */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SyAlchemy.MOD_ID);

    /** Pure extracted metals (raw materials). */
    public static final Map<ModMetal, DeferredItem<Item>> METAL_INGOTS = new EnumMap<>(ModMetal.class);

    /** Alloy ingots (equipment materials). */
    public static final Map<ModAlloy, DeferredItem<Item>> ALLOY_INGOTS = new EnumMap<>(ModAlloy.class);

    /** Per-alloy tools. */
    public static final Map<ModAlloy, DeferredItem<Item>> ALLOY_SWORDS = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<Item>> ALLOY_PICKAXES = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<AxeItem>> ALLOY_AXES = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<ShovelItem>> ALLOY_SHOVELS = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<HoeItem>> ALLOY_HOES = new EnumMap<>(ModAlloy.class);

    /** Per-alloy armor. */
    public static final Map<ModAlloy, DeferredItem<Item>> ALLOY_HELMETS = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<Item>> ALLOY_CHESTPLATES = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<Item>> ALLOY_LEGGINGS = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<Item>> ALLOY_BOOTS = new EnumMap<>(ModAlloy.class);

    static {
        for (ModMetal metal : ModMetal.values()) {
            DeferredItem<Item> item = metal.isFireResistant()
                    ? ITEMS.registerItem(metal.itemName(), props -> new Item(props.fireResistant()))
                    : ITEMS.registerSimpleItem(metal.itemName());
            METAL_INGOTS.put(metal, item);
        }

        for (ModAlloy alloy : ModAlloy.values()) {
            DeferredItem<Item> ingot = alloy.isFireResistant()
                    ? ITEMS.registerItem(alloy.ingotName(), props -> new Item(fire(alloy, props)))
                    : ITEMS.registerSimpleItem(alloy.ingotName());
            ALLOY_INGOTS.put(alloy, ingot);

            // Repair ingredient is the alloy's own ingot, via the per-alloy tag (see ModItemTagsProvider).
            ToolMaterial material = alloy.toolMaterial();

            ALLOY_SWORDS.put(alloy, ITEMS.registerItem(alloy.swordName(),
                    props -> new Item(fire(alloy, props).sword(material, 3.0F, -2.4F))));
            ALLOY_PICKAXES.put(alloy, ITEMS.registerItem(alloy.pickaxeName(),
                    props -> new Item(fire(alloy, props).pickaxe(material, 1.0F, -2.8F))));
            ALLOY_AXES.put(alloy, ITEMS.registerItem(alloy.axeName(),
                    props -> new AxeItem(material, 5.0F, -3.0F, fire(alloy, props))));
            ALLOY_SHOVELS.put(alloy, ITEMS.registerItem(alloy.shovelName(),
                    props -> new ShovelItem(material, 1.5F, -3.0F, fire(alloy, props))));
            ALLOY_HOES.put(alloy, ITEMS.registerItem(alloy.hoeName(),
                    props -> new HoeItem(material, 0.0F, -2.0F, fire(alloy, props))));

            ArmorMaterial armor = ModArmorMaterials.BY_ALLOY.get(alloy);

            ALLOY_HELMETS.put(alloy, ITEMS.registerItem(alloy.helmetName(),
                    props -> new Item(fire(alloy, props).humanoidArmor(armor, ArmorType.HELMET))));
            ALLOY_CHESTPLATES.put(alloy, ITEMS.registerItem(alloy.chestplateName(),
                    props -> new Item(fire(alloy, props).humanoidArmor(armor, ArmorType.CHESTPLATE))));
            ALLOY_LEGGINGS.put(alloy, ITEMS.registerItem(alloy.leggingsName(),
                    props -> new Item(fire(alloy, props).humanoidArmor(armor, ArmorType.LEGGINGS))));
            ALLOY_BOOTS.put(alloy, ITEMS.registerItem(alloy.bootsName(),
                    props -> new Item(fire(alloy, props).humanoidArmor(armor, ArmorType.BOOTS))));
        }
    }

    /** Applies fire/lava immunity to the item properties when the alloy calls for it. */
    private static Item.Properties fire(ModAlloy alloy, Item.Properties props) {
        return alloy.isFireResistant() ? props.fireResistant() : props;
    }

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
