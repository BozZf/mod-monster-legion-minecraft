package io.github.bozzf.monsterlegion.client;

import io.github.bozzf.monsterlegion.config.VisualConfig;
import io.github.bozzf.monsterlegion.entity.LichEffects;
import io.github.bozzf.monsterlegion.entity.LichKingEntity;
import io.github.bozzf.monsterlegion.registry.ModContent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BuiltinItemRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.WitherSkullEntityRenderer;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

public final class MonsterLegionClient implements ClientModInitializer {
    public static VisualConfig CONFIG = new VisualConfig();
    @Override public void onInitializeClient() {
        CONFIG = VisualConfig.load();
        EntityRendererRegistry.register(ModContent.LICH_KING, LichKingRenderer::new);
        EntityRendererRegistry.register(ModContent.LICH_BOLT, WitherSkullEntityRenderer::new);
        LichStaffRenderer staff = new LichStaffRenderer();
        BuiltinItemRendererRegistry.INSTANCE.register(ModContent.LICH_STAFF, staff::render);
        LichEffects.handler = new LichEffects.Handler() {
            @Override public void particle(LichKingEntity lich, String effect) {
                if (!CONFIG.enableAnimationParticles || !lich.getWorld().isClient) return;
                Identifier id = Identifier.tryParse(effect);
                if (id == null || !Registries.PARTICLE_TYPE.containsId(id)) return;
                if (!(Registries.PARTICLE_TYPE.get(id) instanceof SimpleParticleType type)) return;
                int count = effect.endsWith("ash") || effect.endsWith("dust_plume") ? 30 : 12;
                for (int i = 0; i < count; i++) {
                    double x = lich.getX() + (lich.getRandom().nextDouble() - 0.5) * 1.6;
                    double y = lich.getY() + lich.getRandom().nextDouble() * lich.getHeight();
                    double z = lich.getZ() + (lich.getRandom().nextDouble() - 0.5) * 1.6;
                    lich.getWorld().addParticle(type, x, y, z, 0, 0.025, 0);
                }
            }
            @Override public void sound(LichKingEntity lich, String effect) {
                if (!CONFIG.enableAnimationSounds || !lich.getWorld().isClient) return;
                Identifier id = Identifier.tryParse(effect);
                if (id == null || !Registries.SOUND_EVENT.containsId(id)) return;
                lich.getWorld().playSound(lich.getX(), lich.getY(), lich.getZ(), Registries.SOUND_EVENT.get(id),
                        SoundCategory.HOSTILE, 1.0F, 1.0F, false);
            }
        };
    }
}
