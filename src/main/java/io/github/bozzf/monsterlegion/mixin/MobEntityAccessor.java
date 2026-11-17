package io.github.bozzf.monsterlegion.mixin;

import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MobEntity.class)
public interface MobEntityAccessor {
    @Invoker("isAffectedByDaylight")
    boolean monsterLegion$isAffectedByDaylight();

    @Accessor("targetSelector")
    GoalSelector monsterLegion$getTargetSelector();

    @Accessor("goalSelector")
    GoalSelector monsterLegion$getGoalSelector();
}
