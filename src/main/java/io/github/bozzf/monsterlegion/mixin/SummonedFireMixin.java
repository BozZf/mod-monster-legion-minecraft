package io.github.bozzf.monsterlegion.mixin;

import io.github.bozzf.monsterlegion.combat.SummonData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class SummonedFireMixin {
    @Inject(method = "isFireImmune", at = @At("HEAD"), cancellable = true)
    private void monsterLegion$conjuredUndeadCanBurn(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof MobEntity mob && ((SummonData) mob).monsterLegion$conjured())
            cir.setReturnValue(false);
    }
}
