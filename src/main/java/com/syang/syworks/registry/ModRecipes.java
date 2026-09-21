package com.syang.syworks.registry;

import com.syang.syworks.SyWorks;
import com.syang.syworks.world.item.crafting.ProcessingRecipe;
import com.syang.syworks.world.level.block.ModMachine;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * A recipe type, serializer and recipe-book category per {@link ModMachine}. Giving each machine
 * its own type is what stops a crusher recipe firing in the roaster — and it means each machine's
 * block entity can ask the recipe manager for exactly its own recipes.
 *
 * <p>Since 1.21.2 a {@link RecipeSerializer} is a plain record of (codec, stream codec), and every
 * recipe must name a {@link RecipeBookCategory}; ours are registered here but never shown in the
 * vanilla recipe book (the machines have no book UI), so they are just the required placeholders.
 */
public final class ModRecipes {

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, SyWorks.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, SyWorks.MOD_ID);
    public static final DeferredRegister<RecipeBookCategory> BOOK_CATEGORIES =
            DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, SyWorks.MOD_ID);

    public static final Map<ModMachine, Supplier<RecipeType<ProcessingRecipe>>> TYPES =
            new EnumMap<>(ModMachine.class);
    public static final Map<ModMachine, Supplier<RecipeSerializer<ProcessingRecipe>>> SERIALIZERS =
            new EnumMap<>(ModMachine.class);
    public static final Map<ModMachine, Supplier<RecipeBookCategory>> CATEGORIES =
            new EnumMap<>(ModMachine.class);

    static {
        for (ModMachine machine : ModMachine.values()) {
            String id = machine.recipeId();
            TYPES.put(machine, RECIPE_TYPES.register(id,
                    () -> RecipeType.simple(machine.recipeKey())));
            SERIALIZERS.put(machine, RECIPE_SERIALIZERS.register(id,
                    () -> new RecipeSerializer<>(ProcessingRecipe.codec(machine), ProcessingRecipe.streamCodec(machine))));
            CATEGORIES.put(machine, BOOK_CATEGORIES.register(id, RecipeBookCategory::new));
        }
    }

    private ModRecipes() {
    }

    public static void register(IEventBus modBus) {
        RECIPE_TYPES.register(modBus);
        RECIPE_SERIALIZERS.register(modBus);
        BOOK_CATEGORIES.register(modBus);
    }
}
