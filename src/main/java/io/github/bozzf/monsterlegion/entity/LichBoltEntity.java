package io.github.bozzf.monsterlegion.entity;

import io.github.bozzf.monsterlegion.combat.LichAllies;
import io.github.bozzf.monsterlegion.combat.LichRules;
import io.github.bozzf.monsterlegion.registry.ModContent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.WitherSkullEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** Vanilla skull geometry and damage type, with no explosion path. */
public final class LichBoltEntity extends WitherSkullEntity {
    public LichBoltEntity(EntityType<? extends LichBoltEntity> type, World world) { super(type, world); }
    public LichBoltEntity(World world, LivingEntity owner) {
        this(ModContent.LICH_BOLT, world);
        setOwner(owner);
        setPosition(owner.getX(), owner.getEyeY() - 0.3, owner.getZ());
    }
    @Override protected boolean canHit(Entity entity) {
        return !LichAllies.allied(getOwner(), entity) && super.canHit(entity);
    }
    @Override protected void onEntityHit(EntityHitResult hit) {
        if (getWorld().isClient) return;
        Entity target = hit.getEntity();
        float before = target instanceof LivingEntity living ? living.getHealth() + living.getAbsorptionAmount() : 0;
        if (target.damage(getDamageSources().witherSkull(this, getOwner()), LichRules.SKULL_DAMAGE)
                && target instanceof LivingEntity living) {
            float dealt = Math.max(0, before - living.getHealth() - living.getAbsorptionAmount());
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.WITHER, LichRules.EFFECT_TICKS, 1), getOwner());
            living.addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, LichRules.EFFECT_TICKS, 0), getOwner());
            if (getOwner() instanceof LichKingEntity lich) lich.healFromHit(dealt);
        }
    }
    @Override protected void onCollision(HitResult hit) {
        // Do not call WitherSkullEntity.onCollision: it always creates an explosion.
        if (!getWorld().isClient) {
            if (hit instanceof EntityHitResult entityHit) onEntityHit(entityHit);
            discard();
        }
    }
    @Override public void tick() {
        super.tick();
        if (!getWorld().isClient && age > 80) discard();
    }
}
