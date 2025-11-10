package com.github.darksonic300.effect_buff;

import com.mojang.blaze3d.vertex.*;
import org.joml.Matrix4f;

public class CuboidModel {

    /**
     * Renders a cuboid with the given color. Scale and other factors are defined in the animation logic.
     * @param poseStack The current PoseStack.
     * @param r Red component (0.0 to 1.0).
     * @param g Green component (0.0 to 1.0).
     * @param b Blue component (0.0 to 1.0).
     * @param a Alpha component (0.0 to 1.0).
     */
    public static void render(PoseStack poseStack, float r, float g, float b, float a, boolean beneficial) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        float la = a - 0.5f;

        // Calculate LIGHTER color for the top of the frustum (new!)
        // We increase the base color values towards 1.0 (white) for a glow effect
        final float LIGHTEN_FACTOR = 0.3F;
        float r_t = Math.min(1.0F, r + LIGHTEN_FACTOR);
        float g_t = Math.min(1.0F, g + LIGHTEN_FACTOR);
        float b_t = Math.min(1.0F, b + LIGHTEN_FACTOR);

        if(!beneficial) {
            a = a + la;
            la = a - la;
            a = a - la;

            r = r + r_t;
            r_t = r - r_t;
            r = r - r_t;

            g = g + g_t;
            g_t = g - g_t;
            g = g - g_t;

            b = b + b_t;
            b_t = b - b_t;
            b = b - b_t;
        }

        // FRONT FACE (Z = 0)

        bufferBuilder.vertex(matrix, 0, 0, 0).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 1, 0, 0).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 1, 0.5f, 0).color(r_t, g_t, b_t, a).endVertex();
        bufferBuilder.vertex(matrix, 0, 0.5f, 0).color(r_t, g_t, b_t, a).endVertex();

        // BACK FACE (Z = 1)

        bufferBuilder.vertex(matrix, 0, 0, 1).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 0, 0.5f, 1).color(r_t, g_t, b_t, a).endVertex();
        bufferBuilder.vertex(matrix, 1, 0.5f, 1).color(r_t, g_t, b_t, a).endVertex();
        bufferBuilder.vertex(matrix, 1, 0, 1).color(r, g, b, la).endVertex();

        // LEFT FACE (X = 0)

        bufferBuilder.vertex(matrix, 0, 0, 0).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 0, 0, 1).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 0, 0.5f, 1).color(r_t, g_t, b_t, a).endVertex();
        bufferBuilder.vertex(matrix, 0, 0.5f, 0).color(r_t, g_t, b_t, a).endVertex();

        // RIGHT FACE (X = 1)

        bufferBuilder.vertex(matrix, 1, 0, 0).color(r, g, b, la).endVertex();
        bufferBuilder.vertex(matrix, 1, 0.5f, 0).color(r_t, g_t, b_t, a).endVertex();
        bufferBuilder.vertex(matrix, 1, 0.5f, 1f).color(r_t, g_t, b_t, a).endVertex();
        bufferBuilder.vertex(matrix, 1, 0, 1f).color(r, g, b, la).endVertex();

        BufferUploader.drawWithShader(bufferBuilder.end());
    }
}
