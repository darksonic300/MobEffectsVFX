package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.model.IEffectRenderer;
import com.github.darksonic300.mob_effect_vfx.registry.VFXRenderers;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MobEffectsVFX.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientSideRenderEvent {

    private static long ANIMATION_DURATION_MS;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        ANIMATION_DURATION_MS = MEVConfig.CLIENT.duration.get();

        // Ensure we are in the correct rendering stage
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || MobEffectsVFX.activeVisuals.isEmpty()) return;

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        Vec3 cameraPos = event.getCamera().getPosition();
        long currentTime = Util.getMillis();

        IEffectRenderer renderer = VFXRenderers.get(MEVConfig.CLIENT.effect_type.get());

        poseStack.pushPose();

        for (MobEffectsVFX.ActiveEffectVisual visual : MobEffectsVFX.activeVisuals) {
            animationLoop(event, renderer, bufferSource, visual, currentTime, poseStack, mc.player, cameraPos);
        }

        bufferSource.endBatch();
        poseStack.popPose();
    }

    /**
     * Handles animation logic for the vfx, the model definition is found in CuboidModel.java
     */
    private static void animationLoop(RenderLevelStageEvent event, IEffectRenderer renderer, MultiBufferSource.BufferSource bufferSource, MobEffectsVFX.ActiveEffectVisual visual, long currentTime, PoseStack poseStack, Player player, Vec3 camera) {
            MobEffectCategory effectCategory = visual.effect().getCategory();
            long elapsedTime = currentTime - visual.startTime();
            // Calculate animation progress (0.0 to 1.0)
            float progress = (float) elapsedTime / ANIMATION_DURATION_MS;

            // Stop rendering if the animation is finished (cleanup is handled in ClientTick)
            if (progress >= 1.0F) return;

            // Push the matrix state to isolate transformations for this specific visual
            poseStack.pushPose();
            renderer.startRendering(bufferSource, event, poseStack, player, camera, progress, effectCategory, visual.color());
            poseStack.popPose();
    }

//    private static void glowEffectRendering(PoseStack poseStack, Player player, Vec3 camera, float progress, MEVColor color) {
//        float a = calculateAlpha(color.a(), progress);
//
//        // Calculate animated properties
//        float baseSize = 1.3F;
//        float height = (float) ((baseSize - 0.2) * (progress) + 0.5);
//
//        double visualX = Mth.lerp(event.getPartialTick(), player.xo, player.getX()) - (baseSize / 2.0); // Center the cuboid on the player
//        double visualZ = Mth.lerp(event.getPartialTick(), player.zo, player.getZ()) - (baseSize / 2.0);
//
//        // Apply camera offset transformation
//        double x = visualX - camera.x;
//        double y = Mth.lerp(event.getPartialTick(), player.yo, player.getY()) - camera.y;
//        double z = visualZ - camera.z;
//
//        poseStack.translate(x, y, z);
//        poseStack.scale(baseSize, height, baseSize);
//    }
}