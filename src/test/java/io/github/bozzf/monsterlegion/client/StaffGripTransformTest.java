package io.github.bozzf.monsterlegion.client;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class StaffGripTransformTest {
    @Test void shaftGripRemainsAtAnchorAfterTiltAndSlide() {
        MatrixStack matrices = new MatrixStack();
        StaffGripTransform.apply(matrices);
        Vector3f grip = matrices.peek().getPositionMatrix().transformPosition(new Vector3f(0, 20F / 16F, 0));
        assertEquals(0, grip.x, 0.0001);
        assertEquals(0, grip.y, 0.0001);
        assertEquals(0, grip.z, 0.0001);
        Vector3f crystal = matrices.peek().getPositionMatrix().transformPosition(new Vector3f(0, 33F / 16F, 0));
        assertTrue(crystal.length() > 0.8, "Hand must not be attached at crystal center");
    }
    @Test void lowerEndIsTiltedSidewaysInsteadOfPointingStraightToFloor() {
        MatrixStack matrices = new MatrixStack();
        StaffGripTransform.apply(matrices);
        Vector3f lower = matrices.peek().getPositionMatrix().transformPosition(new Vector3f(0, 0, 0));
        assertTrue(Math.hypot(lower.x, lower.z) > 1);
        assertEquals(-0.625, lower.y, 0.0001);
    }
}
