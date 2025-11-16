package com.github.darksonic300.mob_effect_vfx;

import com.mojang.blaze3d.vertex.*;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import org.joml.Matrix4f;

public class StationaryCuboidModel {

    private static final float LIGHTEN_FACTOR = 0.3F;

    /**
     * Renders a cuboid with the given color. Scale and other factors are defined in the animation logic.
     * @param poseStack The current PoseStack.
     * @param r Red component (0.0 to 1.0).
     * @param g Green component (0.0 to 1.0).
     * @param b Blue component (0.0 to 1.0).
     * @param a Alpha component (0.0 to 1.0).
     */
    public static void render(PoseStack poseStack, float r, float g, float b, float a) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        float la = a - 0.8f;
        la = Mth.clamp(0, la, 1.0f);

        float r_t = Math.min(1.0F, r + LIGHTEN_FACTOR);
        float g_t = Math.min(1.0F, g + LIGHTEN_FACTOR);
        float b_t = Math.min(1.0F, b + LIGHTEN_FACTOR);

        bufferBuilder = engageRender(r_t, g_t, b_t, la, bufferBuilder, matrix, a, r, g, b );
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private static BufferBuilder engageRender(float r, float g, float b, float a, BufferBuilder bufferBuilder, Matrix4f matrix, float la, float r_t, float g_t, float b_t) {
        // FRONT FACE (Z = 0)

        bufferBuilder.vertex(matrix, 0, 0, 0).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 1, 0, 0).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 1, 0.7f, 0).color(r_t, g_t, b_t, a).endVertex();
        bufferBuilder.vertex(matrix, 0, 0.7f, 0).color(r_t, g_t, b_t, a).endVertex();

        // BACK FACE (Z = 1)

        bufferBuilder.vertex(matrix, 0, 0, 1).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 0, 0.7f, 1).color(r_t, g_t, b_t, a).endVertex();
        bufferBuilder.vertex(matrix, 1, 0.7f, 1).color(r_t, g_t, b_t, a).endVertex();
        bufferBuilder.vertex(matrix, 1, 0, 1).color(r, g, b, la).endVertex();

        // LEFT FACE (X = 0)

        bufferBuilder.vertex(matrix, 0, 0, 0).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 0, 0, 1).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 0, 0.7f, 1).color(r_t, g_t, b_t, a).endVertex();
        bufferBuilder.vertex(matrix, 0, 0.7f, 0).color(r_t, g_t, b_t, a).endVertex();

        // RIGHT FACE (X = 1)

        bufferBuilder.vertex(matrix, 1, 0, 0).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 1, 0.7f, 0).color(r_t, g_t, b_t, a).endVertex();
        bufferBuilder.vertex(matrix, 1, 0.7f, 1f).color(r_t, g_t, b_t, a).endVertex();
        bufferBuilder.vertex(matrix, 1, 0, 1f).color(r, g, b, la).endVertex();

        return bufferBuilder;
    }
}