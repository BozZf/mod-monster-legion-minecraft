package io.github.bozzf.monsterlegion.client;

import io.github.bozzf.monsterlegion.item.LichStaffItem;
import net.minecraft.client.render.RenderLayer;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public final class LichStaffRenderer extends GeoItemRenderer<LichStaffItem> {
    public LichStaffRenderer() {
        super(new LichStaffModel());
        addRenderLayer(new AutoGlowingGeoLayer<LichStaffItem>(this) {
            @Override protected RenderLayer getRenderType(LichStaffItem item) {
                return MonsterLegionClient.CONFIG.enableStaffGlow ? super.getRenderType(item) : null;
            }
        });
    }
}
