package com.github.darksonic300.effect_buff;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = EffectsVFX.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class CuboidVFXRenderer {

    private static final long ANIMATION_DURATION_MS = 350L;

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

        animationLoop(currentTime, poseStack, player, camera);

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    /**
     * Handles animation logic for the vfx, the model definition is found in CuboidModel.java
     */
    private static void animationLoop(long currentTime, PoseStack poseStack, Player player, Camera camera) {
        for (EffectsVFX.ActiveEffectVisual visual : EffectsVFX.activeVisuals) {
            boolean beneficial = visual.effect().isBeneficial();
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

            float a = 0.8F;

            // Calculate animated properties
            float baseSize = 1.15F;
            //float height = 1.15F;
            float yOffset = progress * 1.7F;

            // Push the matrix state to isolate transformations for this specific visual
            poseStack.pushPose();

            double visualX = player.getX() - (baseSize / 2.0); // Center the cuboid on the player
            double visualZ = player.getZ() - (baseSize / 2.0);

            // Apply camera offset transformation
            double x = visualX - camera.getPosition().x;

            double y = player.getY() - camera.getPosition().y;
            y = beneficial ? y + yOffset : y + 1.7 - yOffset;

            double z = visualZ - camera.getPosition().z;

            poseStack.translate(x, y, z);
            poseStack.scale(baseSize, baseSize, baseSize);

            CuboidModel.render(poseStack, r, g, b, a, beneficial);

            poseStack.popPose();
        }
    }
}