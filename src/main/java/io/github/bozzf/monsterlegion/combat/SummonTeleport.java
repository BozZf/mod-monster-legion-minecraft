package io.github.bozzf.monsterlegion.combat;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public final class SummonTeleport {
    public static void followPlayer(MobEntity mob) {
        SummonData data = (SummonData) mob;
        if (data.monsterLegion$playerOwner() == null || !mob.isAlive()
                || !(mob.getWorld() instanceof ServerWorld world) || mob.hasVehicle()
                || mob.hasPassengers() || mob.isLeashed()) return;
        PlayerEntity owner = world.getPlayerByUuid(data.monsterLegion$playerOwner());
        if (owner == null || !owner.isAlive() || owner.isSpectator() || mob.squaredDistanceTo(owner) < 144) return;
        for (int attempt = 0; attempt < 24; attempt++) {
            int dx = mob.getRandom().nextInt(7) - 3, dz = mob.getRandom().nextInt(7) - 3;
            if (Math.abs(dx) < 2 && Math.abs(dz) < 2) continue;
            for (int dy : new int[]{0, 1, -1, -2, -3}) {
                BlockPos pos = owner.getBlockPos().add(dx, dy, dz);
                boolean loaded = true;
                for (int cx = (pos.getX() - 2) >> 4; cx <= (pos.getX() + 2) >> 4; cx++)
                    for (int cz = (pos.getZ() - 2) >> 4; cz <= (pos.getZ() + 2) >> 4; cz++)
                        loaded &= world.isChunkLoaded(cx, cz);
                if (!loaded || !world.isInBuildLimit(pos.up(2)) || !world.getWorldBorder().contains(pos)) continue;
                if (!world.getBlockState(pos.down()).isSideSolidFullSquare(world, pos.down(), Direction.UP)
                        || !world.getFluidState(pos).isEmpty() || !world.getFluidState(pos.up()).isEmpty()) continue;
                var box = mob.getBoundingBox().offset(pos.getX() + 0.5 - mob.getX(),
                        pos.getY() - mob.getY(), pos.getZ() + 0.5 - mob.getZ());
                if (!world.isSpaceEmpty(mob, box) || !world.getOtherEntities(mob, box).isEmpty()) continue;
                mob.setTarget(null);
                mob.getNavigation().stop();
                mob.setVelocity(Vec3d.ZERO);
                mob.requestTeleport(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                return;
            }
        }
    }
    private SummonTeleport() { }
}
