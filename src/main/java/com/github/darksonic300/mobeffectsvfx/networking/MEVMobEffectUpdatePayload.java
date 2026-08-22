package com.github.darksonic300.mobeffectsvfx.networking;

import com.github.darksonic300.mobeffectsvfx.MobEffectsVFX;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.effect.MobEffectInstance;

public record MEVMobEffectUpdatePayload(int entityId, MobEffectInstance instance) implements CustomPacketPayload {
	public static final Type<MEVMobEffectUpdatePayload> TYPE = new Type<>(MobEffectsVFX.getResource("effect_update"));
	public static final StreamCodec<RegistryFriendlyByteBuf, MEVMobEffectUpdatePayload> STREAM_CODEC = StreamCodec
			.composite(ByteBufCodecs.VAR_INT, MEVMobEffectUpdatePayload::entityId, MobEffectInstance.STREAM_CODEC,
					MEVMobEffectUpdatePayload::instance, MEVMobEffectUpdatePayload::new);

	@Override
	public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
