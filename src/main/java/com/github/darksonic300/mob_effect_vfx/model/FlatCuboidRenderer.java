package com.github.darksonic300.mob_effect_vfx.model;

import com.github.darksonic300.mob_effect_vfx.util.MEVColor;
import com.github.darksonic300.mob_effect_vfx.registry.MEVRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public class FlatCuboidRenderer extends CuboidRenderer {

    @Override
    public void startRendering(MultiBufferSource.BufferSource bufferSource, RenderLevelStageEvent event, PoseStack poseStack, Player player, Vec3 camera, float progress, MobEffectCategory effectCategory, MEVColor color) {
        float a = calculateAlpha(color.a(), progress);
        a += 0.1f;
        color = new MEVColor(color.r(), color.g(), color.b(), a);

        // Calculate animated properties
        float scaleOffset = progress * 1.5F;
        float baseSize = effectCategory == MobEffectCategory.HARMFUL ? (1.3F * 1.5F) - scaleOffset : 1.3F * scaleOffset;

        double visualX = Mth.lerp(event.getPartialTick(), player.xo, player.getX()) - (baseSize / 2.0); // Center the cuboid on the player
        double visualZ = Mth.lerp(event.getPartialTick(), player.zo, player.getZ()) - (baseSize / 2.0);

        // Apply camera offset transformation
        double x = visualX - camera.x;
        double y = Mth.lerp(event.getPartialTick(), player.yo, player.getY()) - camera.y + 0.01D;
        double z = visualZ - camera.z;

        poseStack.translate(x, y, z);
        poseStack.scale(baseSize, 0, baseSize);

        this.render(poseStack, bufferSource.getBuffer(MEVRenderTypes.VFX), color, effectCategory);
    }

    @Override
    void drawCuboid(VertexConsumer buffer, MEVColor opaque, MEVColor transparency, Matrix4f matrix) {
        float r = opaque.r();
        float g = opaque.g();
        float b = opaque.b();
        float a = opaque.a();

        float r_t = transparency.r();
        float g_t = transparency.g();
        float b_t = transparency.b();
        float la = transparency.a();

        // TOP FACE (Y = 0)

        addVertex(buffer, matrix, 0.5f, 0, 0.5f, r, g, b, la);
        addVertex(buffer, matrix, 0, 0, 1, r_t, g_t, b_t, a);
        addVertex(buffer, matrix, 0, 0, 0, r_t, g_t, b_t, a);

        addVertex(buffer, matrix, 0.5f, 0, 0.5f, r, g, b, la);
        addVertex(buffer, matrix, 1, 0, 0, r_t, g_t, b_t, a);
        addVertex(buffer, matrix, 0, 0, 0, r_t, g_t, b_t, a);

        addVertex(buffer, matrix, 0.5f, 0, 0.5f, r, g, b, la);
        addVertex(buffer, matrix, 1, 0, 0, r_t, g_t, b_t, a);
        addVertex(buffer, matrix, 1, 0, 1, r_t, g_t, b_t, a);

        addVertex(buffer, matrix, 0.5f, 0, 0.5f, r, g, b, la);
        addVertex(buffer, matrix, 0, 0, 1, r_t, g_t, b_t, a);
        addVertex(buffer, matrix, 1, 0, 1, r_t, g_t, b_t, a);

    }
}