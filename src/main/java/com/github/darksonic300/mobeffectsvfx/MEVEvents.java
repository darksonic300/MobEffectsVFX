package com.github.darksonic300.mobeffectsvfx;

import com.github.darksonic300.mobeffectsvfx.util.VisualLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.HashMap;

@EventBusSubscriber(modid = MobEffectsVFX.MODID, value = Dist.CLIENT)
public class MEVEvents {

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

    public static void processLivingVisual(final LivingEntity entity, final ClientLevel level, MobEffectInstance instance) {
        final var map = MEVDataManager.EFFECT_CACHE.asMap().computeIfAbsent(entity.getUUID(), k -> new HashMap<>());

        final var effect = instance.getEffect().value();
        final var duration = instance.getDuration();

        if (MEVDataManager.EFFECT_BLOCKLIST.contains(effect))
            return;

        if (!map.containsKey(effect) || duration > map.get(effect) + MEVConfig.CLIENT.refresh_cooldown.get()) {
            VisualLogic.triggerEffectVFX(entity, effect);
            VisualLogic.triggerSoundAndParticles(level, entity, effect);
        }

        map.put(effect, duration);
    }
}
