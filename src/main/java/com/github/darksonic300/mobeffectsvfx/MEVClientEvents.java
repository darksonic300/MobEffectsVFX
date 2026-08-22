package com.github.darksonic300.mobeffectsvfx;

import com.github.darksonic300.mobeffectsvfx.particle.LoweringParticles;
import com.github.darksonic300.mobeffectsvfx.particle.RisingParticles;
import com.github.darksonic300.mobeffectsvfx.registry.MEVParticles;
import com.github.darksonic300.mobeffectsvfx.util.VisualLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MobEffectsVFX.MODID, value = Dist.CLIENT)
public class MEVClientEvents {

    @SubscribeEvent
    public static void onModInit(final FMLClientSetupEvent event) {
        MobEffectsVFX.LOGGER.info("Hello from MobEffectsVFX! Adding more clutter to the log.");
        MEVDataManager.initColorMap();
    }

    @SubscribeEvent
    public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(MEVParticles.RISING_PARTICLES.get(), RisingParticles.Provider::new);
        event.registerSpriteSet(MEVParticles.LOWERING_PARTICLES.get(), LoweringParticles.Provider::new);
    }

    @SubscribeEvent
    public static void onConfigLoad(final ModConfigEvent event) {
        MobEffectsVFX.LOGGER.info("Loading Blocklists config");
        MEVDataManager.EFFECT_BLOCKLIST.clear();
        MEVDataManager.EFFECT_BLOCKLIST.addAll(MEVConfig.CLIENT.blocklist.get().stream()
                .map(entry -> BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(entry))).toList());

        MEVDataManager.ENTITY_BLOCKLIST.clear();
        MEVDataManager.ENTITY_BLOCKLIST.addAll(MEVConfig.CLIENT.entityBlocklist.get().stream()
                .map(entry -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entry))).toList());
    }

    // <-- ENTITY EVENTS -->

    @SubscribeEvent
    public static void onEntityLeave(final EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof LivingEntity && event.getLevel().isClientSide()) {
            MEVDataManager.EFFECT_CACHE.invalidate(event.getEntity().getUUID());
        }
    }

    @SubscribeEvent
    public static void onPlayerLeave(final ClientPlayerNetworkEvent.LoggingOut event) {
        MEVDataManager.clearAllState();
    }

    // <-- RENDERING EVENTS -->

    @SubscribeEvent
    public static void onRenderLevelStage(final RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
            return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || MEVDataManager.ACTIVE_VISUALS.isEmpty())
            return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        var iterator = MEVDataManager.ACTIVE_VISUALS.iterator();
        while (iterator.hasNext()) {
            var item = iterator.next();
            poseStack.pushPose();
            boolean hasFinished = VisualLogic.animationLoop(event, bufferSource, item);
            if (hasFinished)
                iterator.remove();
            poseStack.popPose();
        }

        bufferSource.endBatch();
    }
}
