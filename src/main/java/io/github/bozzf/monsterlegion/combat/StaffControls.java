package io.github.bozzf.monsterlegion.combat;

import io.github.bozzf.monsterlegion.registry.ModContent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public final class StaffControls {
    public static boolean holdingStaff(PlayerEntity player) {
        return player != null && player.isAlive() && !player.isSpectator()
                && player.getMainHandStack().isOf(ModContent.LICH_STAFF);
    }
    public static void cancel(ServerPlayerEntity player) {
        if (!holdingStaff(player)) return;
        boolean removed = Summoning.dismiss(player);
        player.sendMessage(Text.literal(removed ? "Invocação encerrada." : "Você não possui uma invocação ativa."), true);
    }
    public static void initialize() {
        PayloadTypeRegistry.playC2S().register(DismissSummonPayload.ID, DismissSummonPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(DismissSummonPayload.ID, (payload, context) ->
                context.server().execute(() -> cancel(context.player())));
        // Server guards also prevent a direct vanilla attack packet from dealing staff damage.
        AttackEntityCallback.EVENT.register((player, world, hand, target, hit) -> {
            if (!holdingStaff(player)) return ActionResult.PASS;
            if (player instanceof ServerPlayerEntity serverPlayer) cancel(serverPlayer);
            return ActionResult.FAIL;
        });
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!holdingStaff(player)) return ActionResult.PASS;
            if (player instanceof ServerPlayerEntity serverPlayer) cancel(serverPlayer);
            return ActionResult.FAIL;
        });
    }
    private StaffControls() { }
}
