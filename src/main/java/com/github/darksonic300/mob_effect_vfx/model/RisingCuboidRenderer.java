package com.github.darksonic300.mob_effect_vfx.model;

import com.github.darksonic300.mob_effect_vfx.util.MEVColor;
import com.github.darksonic300.mob_effect_vfx.registry.MEVRenderTypes;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public class RisingCuboidRenderer extends CuboidRenderer {

    @Override
    public void startRendering(MultiBufferSource.BufferSource bufferSource, RenderLevelStageEvent event, PoseStack poseStack, Player player, Vec3 camera, float progress, MobEffectCategory effectCategory, MEVColor color) {
        float a = calculateAlpha(color.a(), progress);
        color = new MEVColor(color.r(), color.g(), color.b(), a);

        // Calculate animated properties
        float baseSize = 1.3F;
        float yOffset = progress * 1.6F;

        double visualX = Mth.lerp(event.getPartialTick(), player.xo, player.getX()) - (baseSize / 2.0); // Center the cuboid on the player
        double visualZ = Mth.lerp(event.getPartialTick(), player.zo, player.getZ()) - (baseSize / 2.0);

        // Apply camera offset transformation
        double x = visualX - camera.x;

        double y = Mth.lerp(event.getPartialTick(), player.yo, player.getY()) - camera.y;

        y = effectCategory != MobEffectCategory.HARMFUL ? y + yOffset : y + 1.7 - yOffset;

        double z = visualZ - camera.z;

        poseStack.translate(x, y, z);
        poseStack.scale(baseSize, baseSize, baseSize);

        this.render(poseStack, bufferSource.getBuffer(MEVRenderTypes.VFX), color, effectCategory);
    }

    @Override
    public void drawCuboid(VertexConsumer buffer, MEVColor opaque, MEVColor transparency, Matrix4f matrix) {
        float r = opaque.r();
        float g = opaque.g();
        float b = opaque.b();
        float a = opaque.a();

        float r_t = transparency.r();
        float g_t = transparency.g();
        float b_t = transparency.b();
        float la = transparency.a();

        // FRONT FACE (Z = 0)

        CuboidRenderer.addVertex(buffer, matrix, 0, 0, 0, r, g, b, la);
        CuboidRenderer.addVertex(buffer, matrix, 1, 0, 0, r, g, b, la);
        CuboidRenderer.addVertex(buffer, matrix, 1, 0.7f, 0, r_t, g_t, b_t, a);
        CuboidRenderer.addVertex(buffer, matrix, 0, 0.7f, 0, r_t, g_t, b_t, a);


        // BACK FACE (Z = 1)

        CuboidRenderer.addVertex(buffer, matrix, 0, 0, 1, r, g, b, la);
        CuboidRenderer.addVertex(buffer, matrix, 0, 0.7f, 1, r_t, g_t, b_t, a);
        CuboidRenderer.addVertex(buffer, matrix, 1, 0.7f, 1, r_t, g_t, b_t, a);
        CuboidRenderer.addVertex(buffer, matrix, 1, 0, 1, r, g, b, la);

        // LEFT FACE (X = 0)

        CuboidRenderer.addVertex(buffer, matrix, 0, 0, 0, r, g, b, la);
        CuboidRenderer.addVertex(buffer, matrix, 0, 0, 1, r, g, b, la);
        CuboidRenderer.addVertex(buffer, matrix, 0, 0.7f, 1, r_t, g_t, b_t, a);
        CuboidRenderer.addVertex(buffer, matrix, 0, 0.7f, 0, r_t, g_t, b_t, a);

        // RIGHT FACE (X = 1)

        CuboidRenderer.addVertex(buffer, matrix, 1, 0, 0, r, g, b, la);
        CuboidRenderer.addVertex(buffer, matrix, 1, 0.7f, 0, r_t, g_t, b_t, a);
        CuboidRenderer.addVertex(buffer, matrix, 1, 0.7f, 1f, r_t, g_t, b_t, a);
        CuboidRenderer.addVertex(buffer, matrix, 1, 0, 1f, r, g, b, la);
    }
}
