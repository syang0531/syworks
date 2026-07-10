package com.syang.yame.world.item;

import com.syang.yame.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A castable wand (see §5.7.2/§5.7.7). The wand itself is inert — the {@link ModSpell spells}
 * are stored on the stack (bound at an anvil), and right-click casts the {@link ModWand#affinity
 * active} one, paying an effective cooldown and 1 durability.
 */
public class WandItem extends Item {

    private final ModWand wand;
    private final Supplier<Ingredient> repairIngredient;

    public WandItem(ModWand wand, Supplier<Ingredient> repairIngredient, Properties properties) {
        super(properties);
        this.wand = wand;
        this.repairIngredient = repairIngredient;
    }

    public ModWand wand() {
        return wand;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Optional<ModSpell> active = ModDataComponents.getActiveSpell(stack);

        if (active.isEmpty()) {
            if (level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.yame.wand.no_spell").withStyle(ChatFormatting.GRAY), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        ModSpell spell = active.get();

        if (level instanceof ServerLevel serverLevel) {
            float power = wand.effectivePower(spell);
            spell.cast(serverLevel, player, stack, power);

            player.getCooldowns().addCooldown(this, wand.effectiveCooldown(spell));
            stack.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));

            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.0F);
        }

        player.swing(hand, true);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return repairIngredient.get().test(repair) || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        Element affinity = wand.affinity();
        Component affinityText = affinity == null
                ? Component.translatable("tooltip.yame.wand.affinity_all").withStyle(ChatFormatting.GOLD)
                : Component.literal(affinity.glyph() + " " + affinity.displayName()).withStyle(affinity.color());
        tooltip.add(Component.translatable("tooltip.yame.wand.affinity").withStyle(ChatFormatting.GRAY)
                .append(affinityText));

        List<ModSpell> bound = ModDataComponents.getBoundSpells(stack);
        Optional<ModSpell> active = ModDataComponents.getActiveSpell(stack);
        if (bound.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.yame.wand.empty").withStyle(ChatFormatting.DARK_GRAY));
            return;
        }
        tooltip.add(Component.translatable("tooltip.yame.wand.spells", bound.size(), wand.slots())
                .withStyle(ChatFormatting.GRAY));
        for (ModSpell spell : bound) {
            boolean isActive = active.isPresent() && active.get() == spell;
            Component line = Component.literal((isActive ? " ▸ " : "   ") + spell.element().glyph() + " " + spell.displayName())
                    .withStyle(isActive ? spell.element().color() : ChatFormatting.DARK_GRAY);
            tooltip.add(line);
        }
    }
}
