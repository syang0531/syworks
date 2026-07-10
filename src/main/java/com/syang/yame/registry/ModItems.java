package com.syang.yame.registry;

import com.syang.yame.Yame;
import com.syang.yame.world.item.ModAlloy;
import com.syang.yame.world.item.ModMetal;
import com.syang.yame.world.item.ModSpell;
import com.syang.yame.world.item.ModStaff;
import com.syang.yame.world.item.SpellBookItem;
import com.syang.yame.world.item.StaffItem;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.HoeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * All items: pure metal ingots, alloy ingots, and a full tool + armor set per alloy.
 * BlockItems for {@link ModBlocks} are registered into this same register.
 */
public final class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Yame.MOD_ID);

    /** Pure extracted metals (raw materials). */
    public static final Map<ModMetal, DeferredItem<Item>> METAL_INGOTS = new EnumMap<>(ModMetal.class);

    /** Alloy ingots (equipment materials). */
    public static final Map<ModAlloy, DeferredItem<Item>> ALLOY_INGOTS = new EnumMap<>(ModAlloy.class);

    /** Per-alloy tools. */
    public static final Map<ModAlloy, DeferredItem<SwordItem>> ALLOY_SWORDS = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<PickaxeItem>> ALLOY_PICKAXES = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<AxeItem>> ALLOY_AXES = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<ShovelItem>> ALLOY_SHOVELS = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<HoeItem>> ALLOY_HOES = new EnumMap<>(ModAlloy.class);

    /** Per-alloy armor. */
    public static final Map<ModAlloy, DeferredItem<ArmorItem>> ALLOY_HELMETS = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<ArmorItem>> ALLOY_CHESTPLATES = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<ArmorItem>> ALLOY_LEGGINGS = new EnumMap<>(ModAlloy.class);
    public static final Map<ModAlloy, DeferredItem<ArmorItem>> ALLOY_BOOTS = new EnumMap<>(ModAlloy.class);

    /** Magic system: the rune — catalyst for imbuing magic (enchanted) books at the Rune Altar. */
    public static final DeferredItem<Item> RUNE = ITEMS.registerSimpleItem("rune");

    /** Staff system (§5.7): one castable staff per {@link ModStaff} and one tome per {@link ModSpell}. */
    public static final Map<ModStaff, DeferredItem<StaffItem>> STAFFS = new EnumMap<>(ModStaff.class);
    public static final Map<ModSpell, DeferredItem<SpellBookItem>> SPELL_BOOKS = new EnumMap<>(ModSpell.class);

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

            // Repair ingredient is the alloy's own ingot (supplied lazily).
            Tier tier = alloy.createTier(ingot);

            ALLOY_SWORDS.put(alloy, ITEMS.registerItem(alloy.swordName(),
                    props -> new SwordItem(tier, fire(alloy, props).attributes(SwordItem.createAttributes(tier, 3, -2.4F)))));
            ALLOY_PICKAXES.put(alloy, ITEMS.registerItem(alloy.pickaxeName(),
                    props -> new PickaxeItem(tier, fire(alloy, props).attributes(PickaxeItem.createAttributes(tier, 1, -2.8F)))));
            ALLOY_AXES.put(alloy, ITEMS.registerItem(alloy.axeName(),
                    props -> new AxeItem(tier, fire(alloy, props).attributes(AxeItem.createAttributes(tier, 5.0F, -3.0F)))));
            ALLOY_SHOVELS.put(alloy, ITEMS.registerItem(alloy.shovelName(),
                    props -> new ShovelItem(tier, fire(alloy, props).attributes(ShovelItem.createAttributes(tier, 1.5F, -3.0F)))));
            ALLOY_HOES.put(alloy, ITEMS.registerItem(alloy.hoeName(),
                    props -> new HoeItem(tier, fire(alloy, props).attributes(HoeItem.createAttributes(tier, 0, -2.0F)))));

            Holder<ArmorMaterial> material = ModArmorMaterials.BY_ALLOY.get(alloy);
            int mult = alloy.armorPreset().durabilityMult();

            ALLOY_HELMETS.put(alloy, ITEMS.registerItem(alloy.helmetName(),
                    props -> new ArmorItem(material, ArmorItem.Type.HELMET,
                            fire(alloy, props).durability(ArmorItem.Type.HELMET.getDurability(mult)))));
            ALLOY_CHESTPLATES.put(alloy, ITEMS.registerItem(alloy.chestplateName(),
                    props -> new ArmorItem(material, ArmorItem.Type.CHESTPLATE,
                            fire(alloy, props).durability(ArmorItem.Type.CHESTPLATE.getDurability(mult)))));
            ALLOY_LEGGINGS.put(alloy, ITEMS.registerItem(alloy.leggingsName(),
                    props -> new ArmorItem(material, ArmorItem.Type.LEGGINGS,
                            fire(alloy, props).durability(ArmorItem.Type.LEGGINGS.getDurability(mult)))));
            ALLOY_BOOTS.put(alloy, ITEMS.registerItem(alloy.bootsName(),
                    props -> new ArmorItem(material, ArmorItem.Type.BOOTS,
                            fire(alloy, props).durability(ArmorItem.Type.BOOTS.getDurability(mult)))));
        }

        for (ModStaff staff : ModStaff.values()) {
            Supplier<Ingredient> repair = staffMaterial(staff);
            STAFFS.put(staff, ITEMS.registerItem(staff.id(), props -> {
                Item.Properties p = props.durability(staff.durability());
                return new StaffItem(staff, repair, staff.isFireResistant() ? p.fireResistant() : p);
            }));
        }

        for (ModSpell spell : ModSpell.values()) {
            SPELL_BOOKS.put(spell, ITEMS.registerItem(spell.bookName(),
                    props -> new SpellBookItem(spell, props.stacksTo(16))));
        }
    }

    /** Applies fire/lava immunity to the item properties when the alloy calls for it. */
    private static Item.Properties fire(ModAlloy alloy, Item.Properties props) {
        return alloy.isFireResistant() ? props.fireResistant() : props;
    }

    /** Crafting/repair ingredient for a staff: its alloy ingot, or the vanilla material. */
    public static Supplier<Ingredient> staffMaterial(ModStaff staff) {
        if (staff.alloy() != null) {
            ModAlloy alloy = staff.alloy();
            return () -> Ingredient.of(ALLOY_INGOTS.get(alloy).get());
        }
        return staff.vanillaMaterial();
    }

    private ModItems() {
    }

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
    }
}
