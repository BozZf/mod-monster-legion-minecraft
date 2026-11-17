package io.github.bozzf.monsterlegion.client;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;

public final class StaffGripTransform {
    public static void apply(MatrixStack matrices) {
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(60));
        // Original 28-pixel grip + the requested 0.5-block slide: hold at shaft pixel 20.
        matrices.translate(0, -28.0 / 16.0 + 0.5, 0);
    }
    private StaffGripTransform() { }
}
