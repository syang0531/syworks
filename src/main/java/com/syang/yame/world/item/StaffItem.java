package com.syang.yame.world.item;

import com.syang.yame.registry.ModDataComponents;
import com.syang.yame.registry.ModEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
 * A castable staff (see §5.7.2/§5.7.7). The staff itself is inert — the {@link ModSpell spells}
 * are stored on the stack (bound at an anvil), and right-click casts the {@link ModStaff#affinity
 * active} one, paying an effective cooldown and 1 durability.
 */
public class StaffItem extends Item {

    private final ModStaff staff;
    private final Supplier<Ingredient> repairIngredient;

    public StaffItem(ModStaff staff, Supplier<Ingredient> repairIngredient, Properties properties) {
        super(properties);
        this.staff = staff;
        this.repairIngredient = repairIngredient;
    }

    public ModStaff staff() {
        return staff;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Optional<ModSpell> active = ModDataComponents.getActiveSpell(stack);

        if (active.isEmpty()) {
            if (level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("message.yame.staff.no_spell").withStyle(ChatFormatting.GRAY), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        if (player.getCooldowns().isOnCooldown(this)) {
            return InteractionResultHolder.fail(stack);
        }

        ModSpell spell = active.get();

        if (level instanceof ServerLevel serverLevel) {
            // Spell Power enchant folds into the cast power; Alacrity shortens the effective cooldown.
            float power = staff.effectivePower(spell) * ModEnchantments.spellPower(serverLevel, stack);
            spell.cast(serverLevel, player, stack, power);

            int cooldown = Math.round(staff.effectiveCooldown(spell) * ModEnchantments.cooldown(serverLevel, stack));
            player.getCooldowns().addCooldown(this, Math.max(1, cooldown));
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
        Element affinity = staff.affinity();
        Component affinityText = affinity == null
                ? Component.translatable("tooltip.yame.staff.affinity_all").withStyle(ChatFormatting.GOLD)
                : Component.literal(affinity.glyph() + " " + affinity.displayName()).withStyle(affinity.color());
        tooltip.add(Component.translatable("tooltip.yame.staff.affinity").withStyle(ChatFormatting.GRAY)
                .append(affinityText));

        // Firebolt is innate (free); the slot count reflects only the bound (spellbook-taught) spells.
        List<ModSpell> castable = ModDataComponents.getCastableSpells(stack);
        int boundCount = ModDataComponents.getBoundSpells(stack).size();
        Optional<ModSpell> active = ModDataComponents.getActiveSpell(stack);
        tooltip.add(Component.translatable("tooltip.yame.staff.spells", boundCount, staff.slots())
                .withStyle(ChatFormatting.GRAY));
        for (ModSpell spell : castable) {
            boolean isActive = active.isPresent() && active.get() == spell;
            MutableComponent line = Component.literal((isActive ? " ▸ " : "   ") + spell.element().glyph() + " " + spell.displayName())
                    .withStyle(isActive ? spell.element().color() : ChatFormatting.DARK_GRAY);
            if (spell == ModSpell.FIREBOLT) {
                line.append(Component.translatable("tooltip.yame.staff.innate").withStyle(ChatFormatting.DARK_GRAY));
            }
            tooltip.add(line);
        }
    }
}
