package com.github.darksonic300.effect_buff;

import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

import com.github.darksonic300.effect_buff.EffectBuff.ActiveEffectVisual;

import java.util.Comparator;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Handles the actual drawing of the rising translucent cuboid during the level rendering stage.
 */
@Mod.EventBusSubscriber(modid = EffectBuff.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class EffectCuboidRenderer {

    // The time duration of one game tick (1000ms / 20 ticks)
    private static final float MS_PER_TICK = 50.0F;

    /**
     * Subscribe to the Render Level Stage event to draw custom 3D geometry.
     * We use AFTER_PARTICLES stage to render translucent geometry over the world.
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
    }


    /**
     * Utility method to draw one face of a cuboid.
     * * NOTE: Corrected coordinate mixing for side faces to ensure coplanar quad vertices.
     */
    private static void drawFace(BufferBuilder buffer, Matrix4f matrix, float x1, float x2, float y1, float y2, float z1, float z2, float r, float g, float b, float a, boolean isYAxis, boolean isBottom) {
        if (isYAxis) {
            // Top/Bottom face (Y=constant)
            if (isBottom) {
                // Correct winding order for the bottom face
                buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x2, y1, z2).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).endVertex();
            } else {
                // Correct winding order for the top face
                buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x2, y2, z2).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).endVertex();
            }
        } else {
            // Side faces (X=constant or Z=constant)

            if (z1 == z2) { // Front/Back Face (X-Y plane, Z is fixed at z1)
                // Use z1 consistently for all vertices
                buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x2, y2, z1).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x2, y1, z1).color(r, g, b, a).endVertex();
            } else if (x1 == x2) { // Left/Right Face (Y-Z plane, X is fixed at x1)
                // Use x1 consistently for all vertices
                buffer.vertex(matrix, x1, y1, z1).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x1, y2, z1).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x1, y2, z2).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, x1, y1, z2).color(r, g, b, a).endVertex();
            }
        }
    }
}