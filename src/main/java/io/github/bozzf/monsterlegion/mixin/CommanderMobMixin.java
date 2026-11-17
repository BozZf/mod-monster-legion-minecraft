package io.github.bozzf.monsterlegion.mixin;

import io.github.bozzf.monsterlegion.patrol.Commanders;
import io.github.bozzf.monsterlegion.patrol.PatrolIdentity;
import io.github.bozzf.monsterlegion.patrol.PatrolRules;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public abstract class CommanderMobMixin {
    @Shadow protected int experiencePoints;

    @Inject(method = "getXpToDrop", at = @At("HEAD"), cancellable = true)
    private void monsterLegion$commanderXp(CallbackInfoReturnable<Integer> cir) {
        if (PatrolIdentity.isCommander((MobEntity) (Object) this)) {
            // Use species base XP; vanilla would add a random bonus for all five equipped items.
            cir.setReturnValue(PatrolRules.commanderXp(experiencePoints));
        }
    }
    @Inject(method = "dropEquipment(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;Z)V",
            at = @At("HEAD"), cancellable = true)
    private void monsterLegion$noCommanderEquipment(ServerWorld world, DamageSource source,
            boolean causedByPlayer, CallbackInfo ci) {
        if (PatrolIdentity.isCommander((MobEntity) (Object) this)) ci.cancel();
    }
    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void monsterLegion$noPatrolInfighting(LivingEntity target, CallbackInfo ci) {
        if (target != null && PatrolIdentity.sameGroup((MobEntity) (Object) this, target)) ci.cancel();
    }
    @Inject(method = "convertTo", at = @At("RETURN"))
    private <T extends MobEntity> void monsterLegion$keepCommanderIdentity(EntityType<T> type,
            boolean keepEquipment, CallbackInfoReturnable<T> cir) {
        Commanders.inherit((MobEntity) (Object) this, cir.getReturnValue());
    }
}
