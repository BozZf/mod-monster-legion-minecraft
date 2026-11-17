package io.github.bozzf.monsterlegion.combat;

import io.github.bozzf.monsterlegion.entity.LichKingEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

/** Looks for reachable aerial sight lines instead of pushing straight into a wall. */
public final class LichFlightSearch {
    private int repathTicks;
    private UUID targetId;

    public static List<Vec3d> vantagePositions(Vec3d target) {
        List<Vec3d> positions = new ArrayList<>();
        for (int height : new int[]{2, 5, 8}) {
            for (int[] offset : new int[][]{{6, 0}, {-6, 0}, {0, 6}, {0, -6}, {4, 4}, {4, -4}, {-4, 4}, {-4, -4}})
                positions.add(target.add(offset[0], height, offset[1]));
        }
        return List.copyOf(positions);
    }
    public void reset() { repathTicks = 0; targetId = null; }

    public void pursue(LichKingEntity lich, LivingEntity target, boolean visible) {
        if (!(lich.getWorld() instanceof ServerWorld world)) return;
        if (!target.getUuid().equals(targetId)) { targetId = target.getUuid(); repathTicks = 0; }
        if (--repathTicks > 0) return;
        repathTicks = 20;
        if (visible) {
            lich.getNavigation().startMovingTo(target.getX(), target.getY() + 1.5, target.getZ(), 1.0);
            return;
        }
        Path best = null;
        double bestScore = Double.POSITIVE_INFINITY;
        for (Vec3d position : vantagePositions(target.getPos())) {
            if (!open(world, lich, position) || !loadedBetween(world, position, target.getEyePos())) continue;
            Vec3d eye = position.add(0, lich.getStandingEyeHeight(), 0);
            if (world.raycast(new RaycastContext(eye, target.getEyePos(), RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE, lich)).getType() != HitResult.Type.MISS) continue;
            Path path = lich.getNavigation().findPathTo(BlockPos.ofFloored(position), 0);
            if (path == null || !path.reachesTarget()) continue;
            double score = path.getLength() + position.squaredDistanceTo(lich.getPos()) * 0.05;
            if (score < bestScore) { bestScore = score; best = path; }
        }
        if (best != null) {
            lich.getNavigation().startMovingAlong(best, 1.0);
            return;
        }
        // Try a short climb first; pathfinding still handles ceilings and obstacles.
        Vec3d climb = lich.getPos().add(0, 4, 0);
        if (open(world, lich, climb) && lich.getNavigation().startMovingTo(climb.x, climb.y, climb.z, 1.0)) return;
        lich.getNavigation().startMovingTo(target.getX(), target.getY() + 1.5, target.getZ(), 1.0);
    }
    private static boolean open(ServerWorld world, LichKingEntity lich, Vec3d position) {
        BlockPos pos = BlockPos.ofFloored(position);
        if (!world.isInBuildLimit(pos) || !world.isInBuildLimit(pos.up(4)) || !world.getWorldBorder().contains(pos)
                || !loadedBetween(world, position.add(-2, 0, -2), position.add(2, 0, 2))) return false;
        var box = lich.getBoundingBox().offset(position.subtract(lich.getPos()));
        return world.getFluidState(pos).isEmpty() && world.isSpaceEmpty(lich, box)
                && world.getOtherEntities(lich, box).isEmpty();
    }
    private static boolean loadedBetween(ServerWorld world, Vec3d a, Vec3d b) {
        BlockPos first = BlockPos.ofFloored(a), last = BlockPos.ofFloored(b);
        for (int x = Math.min(first.getX(), last.getX()) >> 4; x <= (Math.max(first.getX(), last.getX()) >> 4); x++)
            for (int z = Math.min(first.getZ(), last.getZ()) >> 4; z <= (Math.max(first.getZ(), last.getZ()) >> 4); z++)
                if (!world.isChunkLoaded(x, z)) return false;
        return true;
    }
}
