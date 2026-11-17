package io.github.bozzf.monsterlegion.entity;

import io.github.bozzf.monsterlegion.legion.LegionManager;
import io.github.bozzf.monsterlegion.registry.ModContent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

/** A non-explosive magic projectile; no block destruction or fire. */
public final class LichBoltEntity extends ThrownItemEntity {
    public LichBoltEntity(EntityType<? extends LichBoltEntity> type, World world) {
        super(type, world);
        setNoGravity(true);
    }
    public LichBoltEntity(World world, LivingEntity owner) {
        super(ModContent.LICH_BOLT, owner, world);
        setNoGravity(true);
    }
    @Override protected Item getDefaultItem() { return Items.ENDER_PEARL; }
    @Override protected boolean canHit(Entity entity) {
        if (getOwner() instanceof LichKingEntity leader && entity instanceof LivingEntity living
                && LegionManager.isLegionMember(leader, living)) return false;
        return super.canHit(entity);
    }
    @Override protected void onEntityHit(EntityHitResult hit) {
        super.onEntityHit(hit);
        if (!getWorld().isClient) {
            hit.getEntity().damage(getDamageSources().indirectMagic(this, getOwner()), 8.0F);
        }
    }
    @Override protected void onCollision(HitResult hit) {
        super.onCollision(hit);
        if (!getWorld().isClient) discard();
    }
    @Override public void tick() {
        super.tick();
        if (!getWorld().isClient && age > 80) discard();
    }
}
