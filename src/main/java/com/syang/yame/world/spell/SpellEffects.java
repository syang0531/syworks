package com.syang.yame.world.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
 * the {@code ModSpell} table, keeping the spell list declarative. {@code power} is the staff's
 * already-computed cast-power multiplier — damage/heal magnitudes scale by it; fixed vanilla
 * projectiles (fireball) do not.
 */
public final class SpellEffects {

    private static final double HITSCAN_RANGE = 24.0;

    private SpellEffects() {
    }

    // ---------------------------------------------------------------- attack

    /** Firebolt — a small fireball that ignites and deals ~5 damage on hit. */
    public static void firebolt(ServerLevel level, Player caster, ItemStack staff, float power) {
        Vec3 dir = caster.getViewVector(1.0F);
        SmallFireball fireball = new SmallFireball(level, caster, dir);
        Vec3 eye = caster.getEyePosition();
        fireball.setPos(eye.x + dir.x, eye.y - 0.1 + dir.y, eye.z + dir.z);
        level.addFreshEntity(fireball);
    }

    /** Frost Arrow — an arrow doing 5×power damage that applies Slowness I for 3 s on hit. */
    public static void frostArrow(ServerLevel level, Player caster, ItemStack staff, float power) {
        Arrow arrow = new Arrow(level, caster, new ItemStack(Items.ARROW), null);
        arrow.setBaseDamage(5.0 * power);
        arrow.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0));
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        arrow.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, 2.5F, 1.0F);
        level.addFreshEntity(arrow);
    }

    /** Lightning Strike — hitscan 7×power damage that bypasses armour, with a visual bolt. */
    public static void lightning(ServerLevel level, Player caster, ItemStack staff, float power) {
        LivingEntity target = rayTraceLiving(level, caster);
        if (target == null) {
            fizzle(level, caster.getEyePosition());
            return;
        }
        target.hurt(level.damageSources().magic(), 7.0F * power);
        var bolt = EntityType.LIGHTNING_BOLT.create(level);
        if (bolt != null) {
            bolt.setVisualOnly(true);
            bolt.moveTo(target.getX(), target.getY(), target.getZ());
            level.addFreshEntity(bolt);
        }
    }

    /** Blizzard — a 5×5 burst at the aimed point: ~6×power freeze damage + Slowness II + ground ice. */
    public static void blizzard(ServerLevel level, Player caster, ItemStack staff, float power) {
        Vec3 point = aimedPoint(caster);
        AABB area = new AABB(point, point).inflate(2.5, 2.0, 2.5);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != caster && e.isAlive())) {
            entity.hurt(level.damageSources().freeze(), 6.0F * power);
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        }
        freezeGround(level, BlockPos.containing(point));
        level.sendParticles(ParticleTypes.SNOWFLAKE, point.x, point.y + 0.5, point.z, 60, 2.0, 1.0, 2.0, 0.02);
    }

    // ---------------------------------------------------------------- heal

    /** Heal — restores 6×power health to the caster. */
    public static void heal(ServerLevel level, Player caster, ItemStack staff, float power) {
        caster.heal(6.0F * power);
        level.sendParticles(ParticleTypes.HEART, caster.getX(), caster.getY() + 1.0, caster.getZ(), 6, 0.4, 0.6, 0.4, 0.0);
    }

    /** Regeneration — Regen II for 8 s to the caster and every player within 4 blocks. */
    public static void regeneration(ServerLevel level, Player caster, ItemStack staff, float power) {
        caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
        for (Player ally : level.getEntitiesOfClass(Player.class, caster.getBoundingBox().inflate(4.0), p -> p.isAlive())) {
            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 160, 1));
        }
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, caster.getX(), caster.getY() + 1.0, caster.getZ(), 12, 1.0, 1.0, 1.0, 0.0);
    }

    // ---------------------------------------------------------------- buff

    /** Haste — Haste II + Speed I for 20 s on the caster. */
    public static void haste(ServerLevel level, Player caster, ItemStack staff, float power) {
        caster.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 400, 1));
        caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 0));
    }

    /** Shield — Absorption II + Resistance I for 15 s on the caster. */
    public static void shield(ServerLevel level, Player caster, ItemStack staff, float power) {
        caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 300, 1));
        caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 0));
    }

    // ---------------------------------------------------------------- debuff

    /** Poison Cloud — Poison II for 5 s to everything in a 3×3 area at the aimed point. */
    public static void poisonCloud(ServerLevel level, Player caster, ItemStack staff, float power) {
        Vec3 point = aimedPoint(caster);
        AABB area = new AABB(point, point).inflate(1.5, 1.0, 1.5);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area, e -> e != caster && e.isAlive())) {
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
        }
        level.sendParticles(ParticleTypes.SNEEZE, point.x, point.y + 0.5, point.z, 40, 1.5, 0.8, 1.5, 0.01);
    }

    /** Curse — Weakness II + Slowness II for 10 s on the aimed target. */
    public static void curse(ServerLevel level, Player caster, ItemStack staff, float power) {
        LivingEntity target = rayTraceLiving(level, caster);
        if (target == null) {
            fizzle(level, caster.getEyePosition());
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
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

    /** Turns exposed water sources near {@code center} into frosted ice, à la Frost Walker. */
    private static void freezeGround(ServerLevel level, BlockPos center) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx * dx + dz * dz > 6) {
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
