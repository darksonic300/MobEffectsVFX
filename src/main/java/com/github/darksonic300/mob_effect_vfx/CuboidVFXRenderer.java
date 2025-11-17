package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.models.FlatCuboidModel;
import com.github.darksonic300.mob_effect_vfx.models.RisingCuboidModel;
import com.github.darksonic300.mob_effect_vfx.models.StationaryCuboidModel;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MobEffectsVFX.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class CuboidVFXRenderer {

    private static final long ANIMATION_DURATION_MS = 500L;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // Ensure we are in the correct rendering stage
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        Camera camera = event.getCamera();
        long currentTime = Util.getMillis();

        // RENDER SETUP: Prepare for drawing custom geometry
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        // Enable blending for transparency
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // Disable culling so we see faces from both sides
        RenderSystem.disableCull();

        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(false);

        animationLoop(currentTime, poseStack, player, camera);

        RenderSystem.depthMask(true);
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Handles animation logic for the vfx, the model definition is found in CuboidModel.java
     */
    private static void animationLoop(long currentTime, PoseStack poseStack, Player player, Camera camera) {
        for (MobEffectsVFX.ActiveEffectVisual visual : MobEffectsVFX.activeVisuals) {
            MobEffectCategory effectCategory = visual.effect().getCategory();
            long elapsedTime = currentTime - visual.startTime();
            // Calculate animation progress (0.0 to 1.0)
            float progress = (float) elapsedTime / ANIMATION_DURATION_MS;

            // Stop rendering if the animation is finished (cleanup is handled in ClientTick)
            if (progress >= 1.0F) {
                continue;
            }

            // Get effect color (use the MobEffect's color for visual theming)
            int color = visual.effect().getColor();
            float r = ((color >> 16) & 0xFF) / 255.0F;
            float g = ((color >> 8) & 0xFF) / 255.0F;
            float b = (color & 0xFF) / 255.0F;

            // Base Alpha value for opacity
            float a = MEVConfig.CLIENT.opacity.get().floatValue();

            // Push the matrix state to isolate transformations for this specific visual
            poseStack.pushPose();
            switch(MEVConfig.CLIENT.effect_type.get()) {
                case FLAT -> flatEffectRendering(poseStack, player, camera, progress, effectCategory, new MEVColor(r,g,b,a));
                case STATIONARY -> stationaryEffectRendering(poseStack, player, camera, progress, new MEVColor(r,g,b,a));
                default -> risingEffectRendering(poseStack, player, camera, progress, effectCategory, new MEVColor(r,g,b,a));
            }
            poseStack.popPose();
        }
    }

    private static void risingEffectRendering(PoseStack poseStack, Player player, Camera camera, float progress, MobEffectCategory effectCategory, MEVColor color) {
        float a = calculateAlpha(color.a(), progress);

        // Calculate animated properties
        float baseSize = 1.3F;
        float yOffset = progress * 1.6F;

        double visualX = player.getX() - (baseSize / 2.0); // Center the cuboid on the player
        double visualZ = player.getZ() - (baseSize / 2.0);

        // Apply camera offset transformation
        double x = visualX - camera.getPosition().x;

        double y = player.getY() - camera.getPosition().y;

        y = effectCategory != MobEffectCategory.HARMFUL ? y + yOffset : y + 1.7 - yOffset;

        double z = visualZ - camera.getPosition().z;

        poseStack.translate(x, y, z);
        poseStack.scale(baseSize, baseSize, baseSize);

        RisingCuboidModel.render(poseStack, color.r(), color.g(), color.b(), a, effectCategory);
    }

    private static void stationaryEffectRendering(PoseStack poseStack, Player player, Camera camera, float progress, MEVColor color) {
        float a = calculateAlpha(color.a(), progress);

        // Calculate animated properties
        float baseSize = 1.3F;
        float height = (float) ((baseSize - 0.2) * (progress) + 0.5);

        double visualX = player.getX() - (baseSize / 2.0); // Center the cuboid on the player
        double visualZ = player.getZ() - (baseSize / 2.0);

        // Apply camera offset transformation
        double x = visualX - camera.getPosition().x;
        double y = player.getY() - camera.getPosition().y;
        double z = visualZ - camera.getPosition().z;

        poseStack.translate(x, y, z);
        poseStack.scale(baseSize, height, baseSize);

        StationaryCuboidModel.render(poseStack, color.r(), color.g(), color.b(), a);
    }

    private static void flatEffectRendering(PoseStack poseStack, Player player, Camera camera, float progress, MobEffectCategory effectCategory, MEVColor color) {
        float a = calculateAlpha(color.a(), progress);
        a += 0.1f;

        // Calculate animated properties
        float scaleOffset = progress * 1.5F;
        float baseSize = effectCategory == MobEffectCategory.HARMFUL ? (1.3F * 1.5F) - scaleOffset : 1.3F * scaleOffset;

        double visualX = player.getX() - (baseSize / 2.0); // Center the cuboid on the player
        double visualZ = player.getZ() - (baseSize / 2.0);

        // Apply camera offset transformation
        double x = visualX - camera.getPosition().x;
        double y = player.getY() - camera.getPosition().y + 0.01D;
        double z = visualZ - camera.getPosition().z;

        poseStack.translate(x, y, z);
        poseStack.scale(baseSize, 0, baseSize);

        FlatCuboidModel.render(poseStack, color.r(), color.g(), color.b(), a, effectCategory);
    }

    private static float calculateAlpha(float alpha, double progress){
        return (float) Mth.clamp(0, alpha * Math.exp(-2.5 * progress) , 1);
    }

}