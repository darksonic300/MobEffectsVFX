package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.model.FlatCuboidModel;
import com.github.darksonic300.mob_effect_vfx.model.RisingCuboidModel;
import com.github.darksonic300.mob_effect_vfx.model.StationaryCuboidModel;
import com.github.darksonic300.mob_effect_vfx.particle.MEVParticles;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = MobEffectsVFX.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class CuboidVFXRenderer {

    private static long ANIMATION_DURATION_MS;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        ANIMATION_DURATION_MS = MEVConfig.CLIENT.duration.get();

        // Ensure we are in the correct rendering stage
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        long currentTime = Util.getMillis();

        // Prepare for drawing custom geometry
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // Disable culling so we see faces from both sides
        RenderSystem.disableCull();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        animationLoop(event, currentTime, poseStack, player, camera);

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Handles animation logic for the vfx, the model definition is found in CuboidModel.java
     */
    private static void animationLoop(RenderLevelStageEvent event, long currentTime, PoseStack poseStack, Player player, Camera camera) {
        for (MobEffectsVFX.ActiveEffectVisual visual : MobEffectsVFX.activeVisuals) {
            MobEffectCategory effectCategory = visual.effect().getCategory();
            long elapsedTime = currentTime - visual.startTime();
            // Calculate animation progress (0.0 to 1.0)
            float progress = (float) elapsedTime / ANIMATION_DURATION_MS;

            // Stop rendering if the animation is finished (cleanup is handled in ClientTick)
            if (progress >= 1.0F) {
                continue;
            }

            // Push the matrix state to isolate transformations for this specific visual
            poseStack.pushPose();
            switch(MEVConfig.CLIENT.effect_type.get()) {
                case FLAT -> flatEffectRendering(event, poseStack, player, camera, progress, effectCategory, visual.color());
                case STATIONARY -> stationaryEffectRendering(event, poseStack, player, camera, progress, visual.color());
                //case GLOW -> glowEffectRendering(poseStack, player, camera, progress, new MEVColor(r,g,b,a));
                default -> risingEffectRendering(event, poseStack, player, camera, progress, effectCategory, visual.color());

            }
            poseStack.popPose();
        }
    }

    private static void risingEffectRendering(RenderLevelStageEvent event, PoseStack poseStack, Player player, Camera camera, float progress, MobEffectCategory effectCategory, MEVColor color) {

        float a = calculateAlpha(color.a(), progress);

        // Calculate animated properties
        float baseSize = 1.3F;
        float yOffset = progress * 1.6F;

        double visualX = Mth.lerp(event.getPartialTick(), player.xo, player.getX()) - (baseSize / 2.0); // Center the cuboid on the player
        double visualZ = Mth.lerp(event.getPartialTick(), player.zo, player.getZ()) - (baseSize / 2.0);

        // Apply camera offset transformation
        double x = visualX - camera.getPosition().x;

        double y = Mth.lerp(event.getPartialTick(), player.yo, player.getY()) - camera.getPosition().y;

        y = effectCategory != MobEffectCategory.HARMFUL ? y + yOffset : y + 1.7 - yOffset;

        double z = visualZ - camera.getPosition().z;

        poseStack.translate(x, y, z);
        poseStack.scale(baseSize, baseSize, baseSize);

        RisingCuboidModel.render(poseStack, color.r(), color.g(), color.b(), a, effectCategory);
    }

    private static void stationaryEffectRendering(RenderLevelStageEvent event, PoseStack poseStack, Player player, Camera camera, float progress, MEVColor color) {
        float a = calculateAlpha(color.a(), progress);

        // Calculate animated properties
        float baseSize = 1.3F;
        float height = (float) ((baseSize - 0.2) * (progress) + 0.5);

        double visualX = Mth.lerp(event.getPartialTick(), player.xo, player.getX()) - (baseSize / 2.0); // Center the cuboid on the player
        double visualZ = Mth.lerp(event.getPartialTick(), player.zo, player.getZ()) - (baseSize / 2.0);

        // Apply camera offset transformation
        double x = visualX - camera.getPosition().x;
        double y = Mth.lerp(event.getPartialTick(), player.yo, player.getY()) - camera.getPosition().y;
        double z = visualZ - camera.getPosition().z;

        poseStack.translate(x, y, z);
        poseStack.scale(baseSize, height, baseSize);

        StationaryCuboidModel.render(poseStack, color.r(), color.g(), color.b(), a);
    }

    private static void flatEffectRendering(RenderLevelStageEvent event, PoseStack poseStack, Player player, Camera camera, float progress, MobEffectCategory effectCategory, MEVColor color) {
        float a = calculateAlpha(color.a(), progress);
        a += 0.1f;

        // Calculate animated properties
        float scaleOffset = progress * 1.5F;
        float baseSize = effectCategory == MobEffectCategory.HARMFUL ? (1.3F * 1.5F) - scaleOffset : 1.3F * scaleOffset;

        double visualX = Mth.lerp(event.getPartialTick(), player.xo, player.getX()) - (baseSize / 2.0); // Center the cuboid on the player
        double visualZ = Mth.lerp(event.getPartialTick(), player.zo, player.getZ()) - (baseSize / 2.0);

        // Apply camera offset transformation
        double x = visualX - camera.getPosition().x;
        double y = Mth.lerp(event.getPartialTick(), player.yo, player.getY()) - camera.getPosition().y + 0.01D;
        double z = visualZ - camera.getPosition().z;

        poseStack.translate(x, y, z);
        poseStack.scale(baseSize, 0, baseSize);

        FlatCuboidModel.render(poseStack, color.r(), color.g(), color.b(), a, effectCategory);
    }

//    private static void glowEffectRendering(PoseStack poseStack, Player player, Camera camera, float progress, MEVColor color) {
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
//        double x = visualX - camera.getPosition().x;
//        double y = Mth.lerp(event.getPartialTick(), player.yo, player.getY()) - camera.getPosition().y;
//        double z = visualZ - camera.getPosition().z;
//
//        poseStack.translate(x, y, z);
//        poseStack.scale(baseSize, height, baseSize);
//    }

    private static float calculateAlpha(float alpha, double progress){
        return (float) Mth.clamp(0, alpha * Math.exp(-2.5 * progress) , 1);
    }
}