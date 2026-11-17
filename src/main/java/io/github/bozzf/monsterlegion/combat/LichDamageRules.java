package io.github.bozzf.monsterlegion.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;

public final class LichDamageRules {
    public static boolean friendlyGroup(SpawnGroup group) { return group != SpawnGroup.MONSTER; }
    public static boolean allowedAttacker(Entity attacker) {
        if (attacker instanceof PlayerEntity) return true;
        return attacker instanceof MobEntity mob && (friendlyGroup(mob.getType().getSpawnGroup())
                || ((SummonData) mob).monsterLegion$playerOwner() != null);
    }
    private LichDamageRules() { }
}
