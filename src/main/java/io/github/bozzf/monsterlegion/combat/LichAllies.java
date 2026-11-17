package io.github.bozzf.monsterlegion.combat;

import io.github.bozzf.monsterlegion.entity.LichKingEntity;
import io.github.bozzf.monsterlegion.patrol.PatrolIdentity;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.server.world.ServerWorld;

public final class LichAllies {
    public static UUID lichId(Entity entity) {
        if (entity instanceof LichKingEntity) return entity.getUuid();
        return entity instanceof MobEntity ? ((SummonData) entity).monsterLegion$lichOwner() : null;
    }
    public static boolean allied(Entity a, Entity b) {
        if (a == null || b == null) return false;
        if (a == b) return true;
        UUID first = lichId(a), second = lichId(b);
        if (first != null && first.equals(second)) return true;
        if (a instanceof MobEntity) {
            UUID owner = ((SummonData) a).monsterLegion$playerOwner();
            if (owner != null && (owner.equals(b.getUuid()) || b instanceof MobEntity
                    && owner.equals(((SummonData) b).monsterLegion$playerOwner()))) return true;
        }
        if (b instanceof MobEntity) {
            UUID owner = ((SummonData) b).monsterLegion$playerOwner();
            if (owner != null && owner.equals(a.getUuid())) return true;
        }
        return PatrolIdentity.sameGroup(a, b);
    }
    public static LichKingEntity leader(MobEntity member) {
        UUID owner = ((SummonData) member).monsterLegion$lichOwner();
        if (owner == null || !(member.getWorld() instanceof ServerWorld world)) return null;
        Entity entity = world.getEntity(owner);
        return entity instanceof LichKingEntity lich && lich.isAlive()
                && member.squaredDistanceTo(lich) <= 48 * 48 ? lich : null;
    }
    public static void recruitNearby(LichKingEntity lich) {
        if (!(lich.getWorld() instanceof ServerWorld world)) return;
        for (MobEntity member : world.getEntitiesByClass(MobEntity.class,
                lich.getBoundingBox().expand(LichRules.ALLY_RADIUS),
                mob -> mob.isAlive() && !(mob instanceof LichKingEntity) && !(mob instanceof WitherEntity)
                        && mob.getType().getSpawnGroup() == SpawnGroup.MONSTER
                        && mob.squaredDistanceTo(lich) <= LichRules.ALLY_RADIUS * LichRules.ALLY_RADIUS)) {
            SummonData data = (SummonData) member;
            if (data.monsterLegion$playerOwner() != null) continue;
            LichKingEntity previous = leader(member);
            if (previous != null && previous != lich) continue;
            data.monsterLegion$lichOwner(lich.getUuid());
            if (allied(lich, member.getTarget())) member.setTarget(null);
        }
    }
    public static void release(LichKingEntity lich) {
        if (!(lich.getWorld() instanceof ServerWorld world)) return;
        for (Entity entity : world.iterateEntities()) {
            if (entity instanceof MobEntity mob && lich.getUuid().equals(((SummonData) mob).monsterLegion$lichOwner())) {
                ((SummonData) mob).monsterLegion$lichOwner(null);
                mob.setTarget(null);
            }
        }
    }
    private LichAllies() { }
}
