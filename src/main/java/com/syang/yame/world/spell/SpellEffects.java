package com.syang.yame.world.spell;

import com.syang.yame.registry.ModEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Server-side cast behaviours for every {@link com.syang.yame.world.item.ModSpell}.
 *
 * <p>Each method matches the {@code ModSpell.SpellAction} shape
 * {@code (ServerLevel, Player, ItemStack staff, float power)} and is referenced by method handle in
 * the {@code ModSpell} table, keeping the spell list declarative.
 *
 * <p><b>Balance (§5.7.10):</b> base damage/heal magnitudes here are the buffed values (≈2× the
 * original) so staff magic scales into the boss-tier game; effect durations are likewise ≈2×. {@code
 * power} is the staff's cast-power multiplier <i>already including</i> the Spell Power enchantment
 * (folded in by {@code StaffItem}), so damage/heal magnitudes multiply by it. Area radii and
 * projectile speed additionally scale by the Arcane Reach enchantment via
 * {@link ModEnchantments#reach}. Fixed vanilla projectiles (the fireball) do not scale in damage.
 */
public final class SpellEffects {

    private static final double HITSCAN_RANGE = 24.0;

    private SpellEffects() {
    }

    // ---------------------------------------------------------------- attack

    /** Firebolt — a small fireball that ignites and deals the vanilla fireball's ~5 fire damage. */
    public static void firebolt(ServerLevel level, Player caster, ItemStack staff, float power) {
        float reach = ModEnchantments.reach(level, staff);
        Vec3 dir = caster.getViewVector(1.0F).scale(reach);
        SmallFireball fireball = new SmallFireball(level, caster, dir);
        Vec3 eye = caster.getEyePosition();
        Vec3 view = caster.getViewVector(1.0F);
        fireball.setPos(eye.x + view.x, eye.y - 0.1 + view.y, eye.z + view.z);
        level.addFreshEntity(fireball);
    }

    /** Frost Arrow — a fast-reload arrow doing 5×power damage + Slowness I for 3 s (loose-mob clearer). */
    public static void frostArrow(ServerLevel level, Player caster, ItemStack staff, float power) {
        float velocity = 3.0F * ModEnchantments.reach(level, staff);
        Arrow arrow = new Arrow(level, caster, new ItemStack(Items.ARROW), null);
        // Vanilla arrow damage = impact-speed × baseDamage. Divide the launch speed back out so a hit
        // lands for exactly 5×power regardless of speed — Arcane Reach then extends range, not damage.
        arrow.setBaseDamage((5.0 * power) / velocity);
        arrow.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        arrow.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, velocity, 1.0F);
        level.addFreshEntity(arrow);
    }

    /** Lightning Strike — hitscan 21×power damage that bypasses armour, with a visual bolt. */
    public static void lightning(ServerLevel level, Player caster, ItemStack staff, float power) {
        LivingEntity target = rayTraceLiving(level, caster);
        if (target == null) {
            fizzle(level, caster.getEyePosition());
            return;
        }
        target.hurt(level.damageSources().magic(), 21.0F * power);
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.setVisualOnly(true);
            bolt.moveTo(target.getX(), target.getY(), target.getZ());
            level.addFreshEntity(bolt);
        }
    }

    /**
     * Blizzard — a burst at the aimed point: 12×power freeze damage + Slowness II, plus ground ice.
     * The radius scales hard with staff tier and enchants ({@code 10 × power × reach}, capped 64): a
     * bare staff clears the mob cluster right in front of you (r ≈ 9–15), while a fully-enchanted
     * capstone staff levels a ~100-block-wide field (r ≈ 52).
     */
    public static void blizzard(ServerLevel level, Player caster, ItemStack staff, float power) {
        Vec3 point = aimedPoint(caster);
        double r = Math.min(64.0, 10.0 * power * ModEnchantments.reach(level, staff));
        AABB area = new AABB(point, point).inflate(r, 4.0, r);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != caster && e.isAlive())) {
            entity.hurt(level.damageSources().freeze(), 12.0F * power);
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 1));
        }
        // Cap the ground-freeze sweep so the biggest blasts don't scan tens of thousands of blocks.
        freezeGround(level, BlockPos.containing(point), Math.min(24, (int) Math.round(r)));
        int particles = Mth.clamp((int) (r * 6), 80, 400);
        level.sendParticles(ParticleTypes.SNOWFLAKE, point.x, point.y + 0.5, point.z, particles, r * 0.7, 1.5, r * 0.7, 0.05);
    }

    // ---------------------------------------------------------------- heal

    /** Heal — restores 12×power health to the caster. */
    public static void heal(ServerLevel level, Player caster, ItemStack staff, float power) {
        caster.heal(12.0F * power);
        level.sendParticles(ParticleTypes.HEART, caster.getX(), caster.getY() + 1.0, caster.getZ(), 8, 0.4, 0.6, 0.4, 0.0);
    }

    /** Regeneration — Regen II for 16 s to the caster and every player within 8 blocks. */
    public static void regeneration(ServerLevel level, Player caster, ItemStack staff, float power) {
        double radius = 8.0 * ModEnchantments.reach(level, staff);
        caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 320, 1));
        for (Player ally : level.getEntitiesOfClass(Player.class, caster.getBoundingBox().inflate(radius), Player::isAlive)) {
            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 320, 1));
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, caster.getX(), caster.getY() + 1.0, caster.getZ(), 16, 1.0, 1.0, 1.0, 0.0);
    }

    // ---------------------------------------------------------------- buff

    /** Haste — Haste II + Speed I for 40 s on the caster. */
    public static void haste(ServerLevel level, Player caster, ItemStack staff, float power) {
        caster.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 800, 1));
        caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 800, 0));
    }

    /** Shield — Absorption III + Resistance I for 30 s on the caster. */
    public static void shield(ServerLevel level, Player caster, ItemStack staff, float power) {
        caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 2));
        caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
    }

    // ---------------------------------------------------------------- debuff

    /** Poison Cloud — Poison II for 10 s to everything in a wide area at the aimed point. */
    public static void poisonCloud(ServerLevel level, Player caster, ItemStack staff, float power) {
        float reach = ModEnchantments.reach(level, staff);
        Vec3 point = aimedPoint(caster);
        double r = 4.0 * reach;
        AABB area = new AABB(point, point).inflate(r, 2.0, r);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != caster && e.isAlive())) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
        }
        level.sendParticles(ParticleTypes.SNEEZE, point.x, point.y + 0.5, point.z, 80, r * 0.7, 0.8, r * 0.7, 0.01);
    }

    /** Curse — Weakness II + Slowness II for 20 s on the aimed target. */
    public static void curse(ServerLevel level, Player caster, ItemStack staff, float power) {
        LivingEntity target = rayTraceLiving(level, caster);
        if (target == null) {
            fizzle(level, caster.getEyePosition());
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 1));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 1));
        level.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 1.0, target.getZ(), 20, 0.4, 0.6, 0.4, 0.0);
    }

    // ---------------------------------------------------------------- helpers

    /** The first living entity on the caster's view ray (up to {@link #HITSCAN_RANGE}), or null. */
    private static LivingEntity rayTraceLiving(ServerLevel level, Player caster) {
        Vec3 start = caster.getEyePosition();
        Vec3 dir = caster.getViewVector(1.0F);
        Vec3 end = start.add(dir.scale(HITSCAN_RANGE));
        // Stop the ray at the first solid block so we cannot hit through walls.
        Vec3 blockHit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, caster)).getLocation();
        AABB search = caster.getBoundingBox().expandTowards(dir.scale(HITSCAN_RANGE)).inflate(1.0);
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(level, caster, start, blockHit, search,
                e -> e instanceof LivingEntity && e.isAlive() && e != caster);
        return hit != null && hit.getEntity() instanceof LivingEntity living ? living : null;
    }

    /** The block/entity point the caster is looking at (falls back to a point in front). */
    private static Vec3 aimedPoint(Player caster) {
        return caster.pick(HITSCAN_RANGE, 1.0F, false).getLocation();
    }

    /** Turns exposed water sources within {@code radius} of {@code center} into frosted ice. */
    private static void freezeGround(ServerLevel level, BlockPos center, int radius) {
        int rSq = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > rSq) {
                    continue;
                }
                for (int dy = -1; dy <= 1; dy++) {
                    BlockPos pos = center.offset(dx, dy, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(Blocks.WATER) && state.getFluidState().isSource() && level.getBlockState(pos.above()).isAir()) {
                        level.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                        break;
                    }
                }
            }
        }
    }

    /** A little puff of smoke when a targeted spell finds nothing. */
    private static void fizzle(ServerLevel level, Vec3 at) {
        level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 5, 0.1, 0.1, 0.1, 0.01);
    }
}
