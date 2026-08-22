package com.github.darksonic300.mobeffectsvfx.networking;

import com.github.darksonic300.mobeffectsvfx.MobEffectsVFX;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record MEVServerPresencePayload() implements CustomPacketPayload {
	public static final Type<MEVServerPresencePayload> TYPE = new Type<>(MobEffectsVFX.getResource("server_presence"));
	public static final StreamCodec<ByteBuf, MEVServerPresencePayload> STREAM_CODEC = StreamCodec
			.unit(new MEVServerPresencePayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
