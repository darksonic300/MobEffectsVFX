package com.github.darksonic300.mobeffectsvfx.networking;

import com.github.darksonic300.mobeffectsvfx.MobEffectsVFX;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.effect.MobEffectInstance;

public record MobEffectUpdatePayload(int entityId, MobEffectInstance instance) implements CustomPacketPayload {
    public static final Type<MobEffectUpdatePayload> TYPE = new Type<>(MobEffectsVFX.getResource("effect_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, MobEffectUpdatePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            MobEffectUpdatePayload::entityId,
            MobEffectInstance.STREAM_CODEC,
            MobEffectUpdatePayload::instance,
            MobEffectUpdatePayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
