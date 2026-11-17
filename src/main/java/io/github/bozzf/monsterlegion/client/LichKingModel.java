package io.github.bozzf.monsterlegion.client;

import io.github.bozzf.monsterlegion.entity.LichKingEntity;
import io.github.bozzf.monsterlegion.registry.ModContent;
import net.minecraft.util.Identifier;
import software.bernie.geckolib.model.GeoModel;

public final class LichKingModel extends GeoModel<LichKingEntity> {
    @Override public Identifier getModelResource(LichKingEntity entity) { return ModContent.id("geo/lich.geo.json"); }
    @Override public Identifier getTextureResource(LichKingEntity entity) { return ModContent.id("textures/entity/rei_lich.png"); }
    @Override public Identifier getAnimationResource(LichKingEntity entity) { return ModContent.id("animations/lich.animation.json"); }
}
