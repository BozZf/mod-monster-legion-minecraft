package io.github.bozzf.monsterlegion.mixin;

import io.github.bozzf.monsterlegion.combat.SummonData;
import io.github.bozzf.monsterlegion.combat.Summoning;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class SummonedLootMixin {
    @Inject(method = "drop(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;)V", at = @At("HEAD"), cancellable = true)
    private void monsterLegion$noSummonedLoot(ServerWorld world, DamageSource source, CallbackInfo ci) {
        if ((Object) this instanceof MobEntity mob && ((SummonData) mob).monsterLegion$conjured()) ci.cancel();
    }
    @Inject(method = "onDeath", at = @At("TAIL"))
    private void monsterLegion$freeSlot(DamageSource source, CallbackInfo ci) {
        if ((Object) this instanceof MobEntity mob) Summoning.onDeath(mob);
    }
}
