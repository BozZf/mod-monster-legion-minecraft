package io.github.bozzf.monsterlegion.client;

import io.github.bozzf.monsterlegion.item.LichStaffItem;
import io.github.bozzf.monsterlegion.registry.ModContent;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public final class LichStaffModel extends GeoModel<LichStaffItem> {
    @Override public Identifier getModelResource(LichStaffItem item) { return ModContent.id("geo/staff.geo.json"); }
    @Override public Identifier getTextureResource(LichStaffItem item) { return ModContent.id("textures/item/lich_staff.png"); }
    @Override public Identifier getAnimationResource(LichStaffItem item) { return ModContent.id("animations/staff.animation.json"); }
}
