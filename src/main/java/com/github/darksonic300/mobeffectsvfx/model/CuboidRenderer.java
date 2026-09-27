package com.github.darksonic300.mobeffectsvfx.model;

import com.github.darksonic300.mobeffectsvfx.util.MEVColor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.joml.Matrix4f;

public abstract class CuboidRenderer implements SubmitNodeCollector.CustomGeometryRenderer {
    protected static final float LIGHTEN_FACTOR = 0.3F;

    protected final MobEffectCategory category;
    protected MEVColor color;

    public CuboidRenderer(MobEffectCategory category, MEVColor color) {
        this.category = category;
        this.color = color;
    }

    @Override
    public void render(PoseStack.Pose pose, VertexConsumer vertexConsumer) {
        Matrix4f matrix = pose.pose();

        float la = color.a() - 0.8f;
        la = Mth.clamp(la, 0, 1.0f);

        MEVColor transparency = new MEVColor(Math.min(1.0F, color.r() + LIGHTEN_FACTOR),
                Math.min(1.0F, color.g() + LIGHTEN_FACTOR), Math.min(1.0F, color.b() + LIGHTEN_FACTOR), la);

        if (category != MobEffectCategory.HARMFUL)
            drawCuboid(vertexConsumer, color, transparency, matrix);
        else
            drawCuboid(vertexConsumer, transparency, color, matrix);
    }

    abstract void drawCuboid(VertexConsumer builder, MEVColor opaque, MEVColor transparency, Matrix4f matrix);

    abstract RenderType getRenderType();

    public abstract void setup(SubmitCustomGeometryEvent event, LivingEntity source, float progress);

    static void addVertex(VertexConsumer buffer, Matrix4f matrix, float x, float y, float z, float r, float g, float b,
                          float a) {
        buffer.addVertex(matrix, x, y, z).setColor(r, g, b, a);
    }

    static float calculateAlpha(float alpha, double progress) {
        return (float) Mth.clamp(alpha * Math.exp(-2.5 * progress), 0, 1);
    }
}
