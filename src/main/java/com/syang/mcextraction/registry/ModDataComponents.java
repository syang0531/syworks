package com.syang.mcextraction.registry;

import com.mojang.serialization.Codec;
import com.syang.mcextraction.MCExtraction;
import com.syang.mcextraction.world.item.ModSpell;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Data components carried on a wand (see §5.7.7): the list of bound spells and the active one.
 * Modelled on how enchanted books carry {@code stored_enchantments} — spells are stored as their
 * stable {@link ModSpell#id()} strings and resolved back through {@link ModSpell#byId(String)}.
 */
public final class ModDataComponents {

    public static final DeferredRegister<DataComponentType<?>> COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MCExtraction.MOD_ID);

    /** All spells bound onto the wand, in binding order. Selection cycles through this list. */
    public static final Supplier<DataComponentType<List<String>>> BOUND_SPELLS =
            COMPONENTS.register("bound_spells", () -> DataComponentType.<List<String>>builder()
                    .persistent(Codec.STRING.listOf())
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()))
                    .build());

    /** The currently-selected spell id (the one that casts on right-click). */
    public static final Supplier<DataComponentType<String>> ACTIVE_SPELL =
            COMPONENTS.register("active_spell", () -> DataComponentType.<String>builder()
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8)
                    .build());

    private ModDataComponents() {
    }

    public static void register(IEventBus modBus) {
        COMPONENTS.register(modBus);
    }

    // ----- convenience accessors -----

    /** The spells bound on {@code stack}, resolved and de-duplicated; unknown ids are dropped. */
    public static List<ModSpell> getBoundSpells(ItemStack stack) {
        List<String> ids = stack.get(BOUND_SPELLS.get());
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        List<ModSpell> spells = new ArrayList<>(ids.size());
        for (String id : ids) {
            ModSpell.byId(id).ifPresent(s -> {
                if (!spells.contains(s)) {
                    spells.add(s);
                }
            });
        }
        return spells;
    }

    /** The active spell, falling back to the first bound spell when none is explicitly selected. */
    public static Optional<ModSpell> getActiveSpell(ItemStack stack) {
        String id = stack.get(ACTIVE_SPELL.get());
        if (id != null) {
            Optional<ModSpell> active = ModSpell.byId(id);
            if (active.isPresent() && getBoundSpells(stack).contains(active.get())) {
                return active;
            }
        }
        List<ModSpell> bound = getBoundSpells(stack);
        return bound.isEmpty() ? Optional.empty() : Optional.of(bound.get(0));
    }

    public static void setActiveSpell(ItemStack stack, ModSpell spell) {
        stack.set(ACTIVE_SPELL.get(), spell.id());
    }

    /**
     * Adds a spell to the wand if it is not already bound. Returns {@code false} when it is a
     * duplicate (caller should reject the anvil binding in that case).
     */
    public static boolean addBoundSpell(ItemStack stack, ModSpell spell) {
        List<String> current = stack.get(BOUND_SPELLS.get());
        List<String> next = current == null ? new ArrayList<>() : new ArrayList<>(current);
        if (next.contains(spell.id())) {
            return false;
        }
        next.add(spell.id());
        stack.set(BOUND_SPELLS.get(), List.copyOf(next));
        return true;
    }
}
