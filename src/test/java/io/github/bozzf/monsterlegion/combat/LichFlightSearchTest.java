package io.github.bozzf.monsterlegion.combat;

import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LichFlightSearchTest {
    @Test void searchIncludesBothSidesAndSeveralAltitudesForObstacles() {
        Vec3d target = new Vec3d(10, 64, -5);
        var positions = LichFlightSearch.vantagePositions(target);
        assertEquals(24, positions.size());
        assertTrue(positions.stream().anyMatch(p -> p.x < target.x));
        assertTrue(positions.stream().anyMatch(p -> p.x > target.x));
        assertTrue(positions.stream().anyMatch(p -> p.z < target.z));
        assertTrue(positions.stream().anyMatch(p -> p.z > target.z));
        assertEquals(3, positions.stream().map(p -> p.y).distinct().count());
        assertTrue(positions.stream().allMatch(p -> p.y > target.y && p.y <= target.y + 8));
    }
    @Test void candidateSearchTracksTargetCoordinatesRatherThanWorldOrigin() {
        Vec3d shift = new Vec3d(-1500, 90, 420);
        var origin = LichFlightSearch.vantagePositions(Vec3d.ZERO);
        var moved = LichFlightSearch.vantagePositions(shift);
        for (int i = 0; i < origin.size(); i++)
            assertEquals(0, moved.get(i).subtract(origin.get(i)).squaredDistanceTo(shift), 0.000001);
    }
}
