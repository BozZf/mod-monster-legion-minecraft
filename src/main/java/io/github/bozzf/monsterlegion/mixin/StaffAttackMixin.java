package io.github.bozzf.monsterlegion.mixin;

import io.github.bozzf.monsterlegion.combat.DismissSummonPayload;
import io.github.bozzf.monsterlegion.combat.StaffControls;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Covers left-click in air, on entities and on blocks before any vanilla hit is sent. */
@Mixin(MinecraftClient.class)
public abstract class StaffAttackMixin {
    @Inject(method = "doAttack", at = @At("HEAD"), cancellable = true)
    private void monsterLegion$dismissInsteadOfAttack(CallbackInfoReturnable<Boolean> cir) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (!StaffControls.holdingStaff(client.player)) return;
        if (ClientPlayNetworking.canSend(DismissSummonPayload.ID))
            ClientPlayNetworking.send(DismissSummonPayload.INSTANCE);
        cir.setReturnValue(false);
    }
    @Inject(method = "handleBlockBreaking", at = @At("HEAD"), cancellable = true)
    private void monsterLegion$noStaffMining(boolean breaking, CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;
        if (StaffControls.holdingStaff(client.player)) {
            if (client.interactionManager != null) client.interactionManager.cancelBlockBreaking();
            ci.cancel();
        }
    }
}
