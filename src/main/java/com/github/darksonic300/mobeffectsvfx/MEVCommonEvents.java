package com.github.darksonic300.mobeffectsvfx;

import com.github.darksonic300.mobeffectsvfx.networking.AckPayload;
import com.github.darksonic300.mobeffectsvfx.networking.MobEffectUpdatePayload;
import com.github.darksonic300.mobeffectsvfx.networking.PresenceConfigurationTask;
import com.github.darksonic300.mobeffectsvfx.networking.ServerPresencePayload;
import com.github.darksonic300.mobeffectsvfx.util.CommonVisualProcessor;
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
        event.register(new PresenceConfigurationTask(
                event.getListener()
        ));
    }

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MobEffectsVFX.MODID)
                .optional();

        registrar.configurationToClient(
                ServerPresencePayload.TYPE,
                ServerPresencePayload.STREAM_CODEC,
                (payload, context) ->
                    context.enqueueWork(() ->
                        MEVDataManager.setIsServerSide(true)
                    ).thenAccept(v -> context.reply(new AckPayload()))
        );

        registrar.playToClient(
                MobEffectUpdatePayload.TYPE,
                MobEffectUpdatePayload.STREAM_CODEC,
                (payload, context) ->
                    context.enqueueWork(() -> {
                        MobEffectsVFX.LOGGER.info("Effect Payload update sent");
                        CommonVisualProcessor.processVisual(payload.entityId(), payload.instance());
                    })
        );

        registrar.playToServer(
                AckPayload.TYPE,
                AckPayload.STREAM_CODEC,
                (payload, context) -> context.finishCurrentTask(
                        PresenceConfigurationTask.TYPE
                )
        );
    }

    @SubscribeEvent
    public static void onEffectAdd(final MobEffectEvent.Added event) {
        final var entity = event.getEntity();

        if (MEVDataManager.ENTITY_BLOCKLIST.contains(entity.getType()))
            return;

        try {
            PacketDistributor.sendToAllPlayers(new MobEffectUpdatePayload(
                    entity.getId(), event.getEffectInstance()
            ));
        } catch (Exception t) {
            MobEffectsVFX.LOGGER.warn("MobEffectsVFX threw an exception: {}", t.fillInStackTrace());
        }
    }
}
