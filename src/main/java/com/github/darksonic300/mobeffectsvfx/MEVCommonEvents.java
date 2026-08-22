package com.github.darksonic300.mobeffectsvfx;

import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid = MobEffectsVFX.MODID)
public class MEVCommonEvents {

    @SubscribeEvent
    public static void registerConfigTask(final ModConfigEvent.Loading event) {
        event.register(new PresenceConfigurationTask(event.getListener()));
    }

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MobEffectsVFX.MODID).optional();

        registrar.configurationToClient(ServerPresencePayload.TYPE, ServerPresencePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> MEVDataManager.setIsServerSide(true))
                        .thenAccept(v -> context.reply(new AckPayload())));

        registrar.playToClient(MobEffectUpdatePayload.TYPE, MobEffectUpdatePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    CommonVisualProcessor.processVisual(payload.entityId(), payload.instance());
                }));

        registrar.playToServer(AckPayload.TYPE, AckPayload.STREAM_CODEC,
                (payload, context) -> context.finishCurrentTask(PresenceConfigurationTask.TYPE));
    }

    @SubscribeEvent
    public static void onEffectAdd(final MobEffectEvent.Added event) {
        final var entity = event.getEntity();

        if (MEVDataManager.ENTITY_BLOCKLIST.contains(entity.getType()))
            return;

        PacketDistributor.sendToAllPlayers(new MobEffectUpdatePayload(entity.getId(), event.getEffectInstance()));
    }
}
