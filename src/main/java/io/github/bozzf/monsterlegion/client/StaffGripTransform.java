package io.github.bozzf.monsterlegion.client;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public final class StaffGripTransform {
    public static void apply(MatrixStack matrices) {
        // Apply the supplied pose in this exact order, inside the renderer's push/pop.
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(67));
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
        matrices.translate(0, -0.5, 0);
    }
    private StaffGripTransform() { }
}
