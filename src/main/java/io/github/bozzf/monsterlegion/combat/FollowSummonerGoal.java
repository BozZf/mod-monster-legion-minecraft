package io.github.bozzf.monsterlegion.combat;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;

public final class FollowSummonerGoal extends Goal {
    private final MobEntity mob;
    private LivingEntity owner;
    private int ticks;
    public FollowSummonerGoal(MobEntity mob) {
        this.mob = mob;
        setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }
    @Override public boolean canStart() {
        SummonData data = (SummonData) mob;
        owner = data.monsterLegion$playerOwner() != null
                ? mob.getWorld().getPlayerByUuid(data.monsterLegion$playerOwner()) : LichAllies.leader(mob);
        return owner != null && owner.isAlive() && mob.getTarget() == null && mob.squaredDistanceTo(owner) > 16;
    }
    @Override public boolean shouldContinue() {
        return owner != null && owner.isAlive() && mob.getTarget() == null && mob.squaredDistanceTo(owner) > 9
                && (((SummonData) mob).monsterLegion$playerOwner() != null || LichAllies.leader(mob) == owner);
    }
    @Override public void start() { ticks = 0; }
    @Override public void tick() {
        mob.getLookControl().lookAt(owner, 10, mob.getMaxLookPitchChange());
        if (--ticks <= 0) {
            ticks = 10;
            mob.getNavigation().startMovingTo(owner, 1.1);
        }
    }
    @Override public void stop() { mob.getNavigation().stop(); owner = null; }
}
