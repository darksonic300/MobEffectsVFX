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
import org.joml.Matrix4f;

/**
 * Handles the actual drawing of the rising translucent cuboid during the level rendering stage.
 * This class now iterates over EffectBuff.activeVisuals to render all currently playing animations.
 */
@Mod.EventBusSubscriber(modid = EffectBuff.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = net.minecraftforge.api.distmarker.Dist.CLIENT)
public class EffectCuboidRenderer {

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

        // Get the PoseStack and the player's camera position
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

        // --- THE ANIMATION LOOP ---
        for (EffectBuff.ActiveEffectVisual visual : EffectBuff.activeVisuals) {
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
            // Fading transparency: start at 0.7 alpha, fade to 0.0
            float a = 0.5F;

            // Calculate animated properties
            float baseSize = 1.15F; // Start with a smaller base size
            float height = 1.15F; // Fixed height
            float yOffset = progress * 1.7F; // Rises from chest level

            // Push the matrix state to isolate transformations for this specific visual
            poseStack.pushPose();

            // 1. Translate to the world position (player's feet + animation offset)
            double visualX = player.getX() - (baseSize / 2.0); // Center the cuboid on the player
            double visualZ = player.getZ() - (baseSize / 2.0);

            // Apply camera offset transformation
            double x = visualX - camera.getPosition().x;
            double y = player.getY() - camera.getPosition().y + yOffset;
            double z = visualZ - camera.getPosition().z;

            poseStack.translate(x, y, z);

            // 2. Apply scale
            poseStack.scale(baseSize, height, baseSize);

            // 3. Call the rendering method
            renderSimpleCube(poseStack, r, g, b, a);

            // Pop the matrix state to restore previous transformations
            poseStack.popPose();
        }

        // RENDER TEARDOWN
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }


    /**
     * Renders a 1x1x1 cube at (0, 0, 0) with the given color.
     * @param poseStack The current PoseStack.
     * @param r Red component (0.0 to 1.0).
     * @param g Green component (0.0 to 1.0).
     * @param b Blue component (0.0 to 1.0).
     * @param a Alpha component (0.0 to 1.0).
     */
    private static void renderSimpleCube(PoseStack poseStack, float r, float g, float b, float a) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();
        
        float la = a - 0.4f;

        // FRONT FACE (Z = 0)
        
        bufferBuilder.vertex(matrix, 0, 0, 0).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 1, 0, 0).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 1, 0.5f, 0).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, 0, 0.5f, 0).color(r, g, b, a).endVertex();

        // BACK FACE (Z = 1)

        bufferBuilder.vertex(matrix, 0, 0, 1).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 0, 0.5f, 1).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, 1, 0.5f, 1).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, 1, 0, 1).color(r, g, b, la).endVertex();

        // LEFT FACE (X = 0)

        bufferBuilder.vertex(matrix, 0, 0, 0).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 0, 0, 1).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 0, 0.5f, 1).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, 0, 0.5f, 0).color(r, g, b, a).endVertex();

        // RIGHT FACE (X = 1)

        bufferBuilder.vertex(matrix, 1, 0, 0).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 1, 0.5f, 0).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, 1, 0.5f, 1f).color(r, g, b, a).endVertex();
        bufferBuilder.vertex(matrix, 1, 0, 1f).color(r, g, b, la).endVertex();

        BufferUploader.drawWithShader(bufferBuilder.end());
    }
}