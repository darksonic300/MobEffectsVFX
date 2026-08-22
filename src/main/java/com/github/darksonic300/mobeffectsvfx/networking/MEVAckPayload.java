package com.github.darksonic300.mobeffectsvfx.networking;

import com.github.darksonic300.mobeffectsvfx.MobEffectsVFX;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MEVAckPayload() implements CustomPacketPayload {
	public static final CustomPacketPayload.Type<MEVAckPayload> TYPE = new CustomPacketPayload.Type<>(
			MobEffectsVFX.getResource("ack"));

	public static final StreamCodec<ByteBuf, MEVAckPayload> STREAM_CODEC = StreamCodec.unit(new MEVAckPayload());

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}