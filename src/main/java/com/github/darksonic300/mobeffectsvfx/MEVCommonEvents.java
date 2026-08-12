package com.github.darksonic300.mobeffectsvfx;

import com.github.darksonic300.mobeffectsvfx.networking.ServerPresencePayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = MobEffectsVFX.MODID)
public class MEVCommonEvents {

    @SubscribeEvent
    public static void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MobEffectsVFX.MODID)
                .optional();

        registrar.playToClient(
                ServerPresencePayload.TYPE,
                ServerPresencePayload.STREAM_CODEC,
                (payload, context) ->
                    context.enqueueWork(() ->
                            MEVDataManager.setIsServerSide(true)
                    )
        );
    }
}
