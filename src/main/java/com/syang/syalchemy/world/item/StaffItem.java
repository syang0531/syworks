package com.syang.syalchemy.world.item;

import com.syang.syalchemy.registry.ModDataComponents;
import com.syang.syalchemy.registry.ModEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Consumer;

/**
 * A castable staff (see §5.7.2/§5.7.7). The staff itself is inert — the {@link ModSpell spells}
 * are stored on the stack (bound at an anvil), and right-click casts the {@link ModStaff#affinity
 * active} one, paying an effective cooldown and 1 durability.
 *
 * <p>Repair material is set through {@code Item.Properties#repairable} at registration
 * (see {@code ModItems}); since 1.21.2 that is a data component, not an item override.
 */
public class StaffItem extends Item {

    private final ModStaff staff;

    public StaffItem(ModStaff staff, Properties properties) {
        super(properties);
        this.staff = staff;
    }

    public ModStaff staff() {
        return staff;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // Never empty: every staff casts the innate Firebolt (see ModDataComponents#getActiveSpell).
        ModSpell spell = ModDataComponents.getActiveSpell(stack).orElse(ModSpell.FIREBOLT);

        if (player.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        if (level instanceof ServerLevel serverLevel) {
            // Spell Power enchant folds into the cast power; Alacrity shortens the effective cooldown.
            float power = staff.effectivePower(spell) * ModEnchantments.spellPower(serverLevel, stack);
            spell.cast(serverLevel, player, stack, power);

            int cooldown = Math.round(staff.effectiveCooldown(spell) * ModEnchantments.cooldown(serverLevel, stack));
            player.getCooldowns().addCooldown(stack, Math.max(1, cooldown));
            stack.hurtAndBreak(1, player, hand);

            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8F, 1.0F);
        }

        // SUCCESS swings the arm client-side; nothing more to do here.
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> tooltip, TooltipFlag flag) {
        Element affinity = staff.affinity();
        Component affinityText = affinity == null
                ? Component.translatable("tooltip.syalchemy.staff.affinity_all").withStyle(ChatFormatting.GOLD)
                : Component.literal(affinity.glyph() + " " + affinity.displayName()).withStyle(affinity.color());
        tooltip.accept(Component.translatable("tooltip.syalchemy.staff.affinity").withStyle(ChatFormatting.GRAY)
                .append(affinityText));

        // Firebolt is innate (free); the slot count reflects only the bound (spellbook-taught) spells.
        List<ModSpell> castable = ModDataComponents.getCastableSpells(stack);
        int boundCount = ModDataComponents.getBoundSpells(stack).size();
        ModSpell active = ModDataComponents.getActiveSpell(stack).orElse(ModSpell.FIREBOLT);
        tooltip.accept(Component.translatable("tooltip.syalchemy.staff.spells", boundCount, staff.slots())
                .withStyle(ChatFormatting.GRAY));
        for (ModSpell spell : castable) {
            boolean isActive = active == spell;
            MutableComponent line = Component.literal((isActive ? " ▸ " : "   ") + spell.element().glyph() + " " + spell.displayName())
                    .withStyle(isActive ? spell.element().color() : ChatFormatting.DARK_GRAY);
            if (spell == ModSpell.FIREBOLT) {
                line.append(Component.translatable("tooltip.syalchemy.staff.innate").withStyle(ChatFormatting.DARK_GRAY));
            }
            tooltip.accept(line);
        }
    }
}
