package com.github.darksonic300.mobeffectsvfx;

import com.github.darksonic300.mobeffectsvfx.networking.MEVAckPayload;
import com.github.darksonic300.mobeffectsvfx.networking.MEVMobEffectUpdatePayload;
import com.github.darksonic300.mobeffectsvfx.networking.MEVPresenceConfigurationTask;
import com.github.darksonic300.mobeffectsvfx.networking.MEVServerPresencePayload;
import com.github.darksonic300.mobeffectsvfx.util.MEVCommonVisualProcessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MobEffectsVFX.MODID)
public class MEVCommonEvents {

	@SubscribeEvent
	public static void registerConfigTask(final RegisterConfigurationTasksEvent event) {
		event.register(new MEVPresenceConfigurationTask(event.getListener()));
	}

	@SubscribeEvent
	public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
		final PayloadRegistrar registrar = event.registrar(MobEffectsVFX.MODID).optional();

		registrar.configurationToClient(MEVServerPresencePayload.TYPE, MEVServerPresencePayload.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() -> MEVDataManager.setIsServerSide(true))
						.thenAccept(v -> context.reply(new MEVAckPayload())));

		registrar.playToClient(MEVMobEffectUpdatePayload.TYPE, MEVMobEffectUpdatePayload.STREAM_CODEC,
				(payload, context) -> context.enqueueWork(() -> {
					MEVCommonVisualProcessor.processVisual(payload.entityId(), payload.instance());
				}));

		registrar.playToServer(MEVAckPayload.TYPE, MEVAckPayload.STREAM_CODEC,
				(payload, context) -> context.finishCurrentTask(MEVPresenceConfigurationTask.TYPE));
	}

	@SubscribeEvent
	public static void onEffectAdd(final MobEffectEvent.Added event) {
		final var entity = event.getEntity();

		if (MEVDataManager.ENTITY_BLOCKLIST.contains(entity.getType()))
			return;

		PacketDistributor.sendToAllPlayers(new MEVMobEffectUpdatePayload(entity.getId(), event.getEffectInstance()));
	}
}
