package io.github.bozzf.monsterlegion.legion;

import java.util.EnumSet;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;

/** Follows while idle; existing melee/ranged goals handle combat. */
public final class FollowLeaderGoal extends Goal {
    private final PathAwareEntity mob;
    private PathAwareEntity leader;
    private int repathTicks;

    public FollowLeaderGoal(PathAwareEntity mob) {
        this.mob = mob;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        leader = LegionManager.leaderFor(mob);
        if (leader == null) return false;
        return mob.getTarget() == null && mob.squaredDistanceTo(leader) > 16.0;
    }

    @Override
    public boolean shouldContinue() {
        return leader != null && LegionManager.leaderFor(mob) == leader
                && mob.getTarget() == null && mob.squaredDistanceTo(leader) > 9.0;
    }

    @Override
    public void start() { repathTicks = 0; }

    @Override
    public void tick() {
        mob.getLookControl().lookAt(leader, 10.0F, mob.getMaxLookPitchChange());
        if (--repathTicks <= 0) {
            repathTicks = 10;
            mob.getNavigation().startMovingTo(leader, 1.1);
        }
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        leader = null;
    }
}
