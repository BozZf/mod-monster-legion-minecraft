package io.github.bozzf.monsterlegion.combat;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class AllyTargetGoal extends Goal {
    private final MobEntity mob;
    public AllyTargetGoal(MobEntity mob) {
        this.mob = mob;
        setControls(EnumSet.of(Control.TARGET));
    }
    @Override public boolean canStart() {
        SummonData data = (SummonData) mob;
        return data.monsterLegion$playerOwner() != null || data.monsterLegion$lichOwner() != null;
    }
    @Override public boolean shouldContinue() { return canStart(); }
    @Override public boolean shouldRunEveryTick() { return true; }
    @Override public void tick() {
        SummonData data = (SummonData) mob;
        LivingEntity target = null;
        var lich = LichAllies.leader(mob);
        if (lich != null) target = lich.getTarget();
        else if (data.monsterLegion$playerOwner() != null) {
            PlayerEntity owner = mob.getWorld().getPlayerByUuid(data.monsterLegion$playerOwner());
            if (owner != null && owner.isAlive()) {
                if (owner.age - owner.getLastAttackedTime() < 200) target = owner.getAttacker();
                if (!valid(target) && owner.age - owner.getLastAttackTime() < 200) target = owner.getAttacking();
            }
        }
        mob.setTarget(valid(target) ? target : null);
    }
    private boolean valid(LivingEntity target) {
        return target != null && target.isAlive() && !LichAllies.allied(mob, target) && mob.canTarget(target);
    }
    @Override public void stop() { mob.setTarget(null); }
}
