package io.github.bozzf.monsterlegion.patrol;

import io.github.bozzf.monsterlegion.legion.LegionManager;
import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.PathAwareEntity;

/** Owns TARGET while linked to a patrol commander, ahead of independent vanilla target goals. */
public final class ObeyCommanderGoal extends Goal {
    private final PathAwareEntity member;
    private PathAwareEntity commander;
    private LivingEntity sharedTarget;

    public ObeyCommanderGoal(PathAwareEntity member) {
        this.member = member;
        setControls(EnumSet.of(Control.TARGET));
    }
    @Override public boolean canStart() {
        commander = LegionManager.leaderFor(member);
        return commander != null && PatrolIdentity.isCommander(commander);
    }
    @Override public boolean shouldContinue() {
        return commander != null && LegionManager.leaderFor(member) == commander;
    }
    @Override public boolean shouldRunEveryTick() { return true; }
    @Override public void tick() {
        LivingEntity target = commander.getTarget();
        sharedTarget = target != null && target.isAlive() && member.canTarget(target)
                && !LegionManager.isLegionMember(commander, target) ? target : null;
        member.setTarget(sharedTarget);
    }
    @Override public void stop() {
        if (member.getTarget() == sharedTarget) member.setTarget(null);
        commander = null;
        sharedTarget = null;
    }
}
