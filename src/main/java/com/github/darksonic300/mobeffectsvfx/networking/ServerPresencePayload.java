package com.github.darksonic300.mobeffectsvfx.networking;

import com.github.darksonic300.mobeffectsvfx.MobEffectsVFX;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record ServerPresencePayload() implements CustomPacketPayload {
	public static final Type<ServerPresencePayload> TYPE = new Type<>(MobEffectsVFX.getResource("server_presence"));
	public static final StreamCodec<ByteBuf, ServerPresencePayload> STREAM_CODEC = StreamCodec
			.unit(new ServerPresencePayload());

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
