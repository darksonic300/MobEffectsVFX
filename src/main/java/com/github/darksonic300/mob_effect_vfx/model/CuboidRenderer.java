package com.github.darksonic300.mob_effect_vfx.model;

import com.github.darksonic300.mob_effect_vfx.util.MEVColor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import org.joml.Matrix4f;

public abstract class CuboidRenderer implements IEffectRenderer {
    private static final float LIGHTEN_FACTOR = 0.3F;

    /**
     * Renders a cuboid with the given color. Scale and other factors are defined in the animation logic.
     * @param poseStack The current PoseStack.
     * @param r Red component (0.0 to 1.0).
     * @param g Green component (0.0 to 1.0).
     * @param b Blue component (0.0 to 1.0).
     * @param a Alpha component (0.0 to 1.0).
     */
    public void render(PoseStack poseStack, VertexConsumer buffer, MEVColor color, MobEffectCategory category) {
        Matrix4f matrix = poseStack.last().pose();

        float la = color.a() - 0.8f;
        la = Mth.clamp(0, la, 1.0f);

        float r_t = Math.min(1.0F, color.r() + LIGHTEN_FACTOR);
        float g_t = Math.min(1.0F, color.g() + LIGHTEN_FACTOR);
        float b_t = Math.min(1.0F, color.b() + LIGHTEN_FACTOR);

        MEVColor transparency = new MEVColor(r_t, g_t, b_t, la);

        if(category != MobEffectCategory.HARMFUL){
            drawCuboid(buffer, color, transparency, matrix);
        }else {
            drawCuboid(buffer, transparency, color, matrix);
        }
    }

    abstract void drawCuboid(VertexConsumer builder, MEVColor opaque, MEVColor transparency, Matrix4f matrix);

    static void addVertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, float r, float g, float b, float a) {
        buffer.vertex(matrix, x, y, z).color(r, g, b, a).endVertex();
    }

    static float calculateAlpha(float alpha, double progress){
        return (float) Mth.clamp(0, alpha * Math.exp(-2.5 * progress) , 1);
    }
}
