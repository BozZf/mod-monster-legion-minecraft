package io.github.bozzf.monsterlegion.combat;

import io.github.bozzf.monsterlegion.registry.ModContent;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

/** Empty request: the server derives the owner from the authenticated sender. */
public record DismissSummonPayload() implements CustomPayload {
    public static final DismissSummonPayload INSTANCE = new DismissSummonPayload();
    public static final Id<DismissSummonPayload> ID = new Id<>(ModContent.id("dismiss_summon"));
    public static final PacketCodec<RegistryByteBuf, DismissSummonPayload> CODEC = PacketCodec.unit(INSTANCE);
    @Override public Id<? extends CustomPayload> getId() { return ID; }
}
