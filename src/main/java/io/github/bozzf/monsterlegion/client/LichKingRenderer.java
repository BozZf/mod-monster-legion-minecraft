package io.github.bozzf.monsterlegion.client;

import io.github.bozzf.monsterlegion.entity.LichKingEntity;
import io.github.bozzf.monsterlegion.registry.ModContent;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

public final class LichKingRenderer extends GeoEntityRenderer<LichKingEntity> {
    public LichKingRenderer(EntityRendererFactory.Context context) {
        super(context, new LichKingModel());
        shadowRadius = 0.6F;
        addRenderLayer(new AutoGlowingGeoLayer<LichKingEntity>(this) {
            @Override protected RenderLayer getRenderType(LichKingEntity entity) {
                return MonsterLegionClient.CONFIG.enableLichGlow ? super.getRenderType(entity) : null;
            }
        });
        addRenderLayer(new BlockAndItemGeoLayer<LichKingEntity>(this) {
            private final ItemStack staff = new ItemStack(ModContent.LICH_STAFF);
            @Override protected ItemStack getStackForBone(GeoBone bone, LichKingEntity entity) {
                return bone.getName().equals("staff_anchor") ? staff : null;
            }
            @Override protected void renderStackForBone(MatrixStack matrices, GeoBone bone, ItemStack stack,
                    LichKingEntity entity, VertexConsumerProvider vertices, float tickDelta, int light, int overlay) {
                matrices.push();
                // Align the standalone staff grip (0, 28, 0) to the dedicated hand anchor.
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-90));
                matrices.translate(0, -28.0 / 16.0, 0);
                super.renderStackForBone(matrices, bone, stack, entity, vertices, tickDelta, light, overlay);
                matrices.pop();
            }
        });
    }
    @Override protected float getDeathMaxRotation(LichKingEntity entity) { return 0; }
    @Override protected void applyRotations(LichKingEntity entity, MatrixStack matrices, float age,
            float bodyYaw, float tickDelta, float scale) {
        super.applyRotations(entity, matrices, age, bodyYaw, tickDelta, scale);
        // Keep the original 90-degree axis correction and turn the visible model another 180 degrees.
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90 + 180));
    }
}
