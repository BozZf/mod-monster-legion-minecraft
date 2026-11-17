package io.github.bozzf.monsterlegion.legion;

import io.github.bozzf.monsterlegion.MonsterLegion;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

/** All recruitment and target changes happen on the logical server. */
public final class LegionManager {
    public static final String LEADER_TAG = "monster_legion.leader";
    public static final double RECRUIT_RADIUS = 24.0;
    public static final double RELEASE_RADIUS = 48.0;
    public static final int MAX_MEMBERS = 12;
    private static final TagKey<EntityType<?>> RECRUITABLE = TagKey.of(
            RegistryKeys.ENTITY_TYPE, Identifier.of(MonsterLegion.MOD_ID, "recruitable"));
    private static final Map<ServerWorld, LegionRoster> ROSTERS = new HashMap<>();

    private LegionManager() { }

    public static void tick(ServerWorld world) {
        if (world.getTime() % 20 != 0) return;
        LegionRoster roster = ROSTERS.computeIfAbsent(world, ignored -> new LegionRoster(MAX_MEMBERS));
        roster.retain((leaderId, memberId) -> {
            Entity leader = world.getEntity(leaderId);
            Entity member = world.getEntity(memberId);
            return isLeader(leader) && member instanceof PathAwareEntity mob && mob.isAlive()
                    && !isLeader(member) && mob.getType().isIn(RECRUITABLE)
                    && member.squaredDistanceTo(leader) <= RELEASE_RADIUS * RELEASE_RADIUS;
        });
        for (Entity entity : world.iterateEntities()) {
            if (!isLeader(entity)) continue;
            PathAwareEntity leader = (PathAwareEntity) entity;
            for (PathAwareEntity member : world.getEntitiesByClass(PathAwareEntity.class,
                    leader.getBoundingBox().expand(RECRUIT_RADIUS),
                    mob -> mob.isAlive() && !isLeader(mob) && mob.getType().isIn(RECRUITABLE)
                            && mob.squaredDistanceTo(leader) <= RECRUIT_RADIUS * RECRUIT_RADIUS)) {
                roster.recruit(leader.getUuid(), member.getUuid());
                LivingEntity target = leader.getTarget();
                if (leader.getUuid().equals(roster.leaderOf(member.getUuid()))
                        && target != null && target.isAlive() && member.canTarget(target)
                        && !isLegionMember(leader, target)) {
                    member.setTarget(target);
                }
            }
        }
    }

    public static PathAwareEntity leaderFor(PathAwareEntity member) {
        if (!(member.getWorld() instanceof ServerWorld world)) return null;
        LegionRoster roster = ROSTERS.get(world);
        if (roster == null || roster.leaderOf(member.getUuid()) == null) return null;
        Entity entity = world.getEntity(roster.leaderOf(member.getUuid()));
        return isLeader(entity) && member.squaredDistanceTo(entity) <= RELEASE_RADIUS * RELEASE_RADIUS
                ? (PathAwareEntity) entity : null;
    }

    public static boolean isLeader(Entity entity) {
        return entity instanceof PathAwareEntity mob && mob.isAlive()
                && mob.getCommandTags().contains(LEADER_TAG);
    }

    public static boolean isLegionMember(PathAwareEntity leader, LivingEntity entity) {
        return entity == leader || (entity instanceof PathAwareEntity mob && leaderFor(mob) == leader);
    }

    public static boolean tryRecruit(PathAwareEntity leader, PathAwareEntity member) {
        if (!(leader.getWorld() instanceof ServerWorld world) || member.getWorld() != world
                || !isLeader(leader) || isLeader(member) || !member.getType().isIn(RECRUITABLE)) return false;
        return ROSTERS.computeIfAbsent(world, ignored -> new LegionRoster(MAX_MEMBERS))
                .recruit(leader.getUuid(), member.getUuid());
    }

    public static void clear() { ROSTERS.clear(); }
}
