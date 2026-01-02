package com.github.darksonic300.mob_effect_vfx.model;

import com.github.darksonic300.mob_effect_vfx.util.MEVColor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;

public interface IEffectRenderer {
    void startRendering(MultiBufferSource.BufferSource bufferSource, RenderLevelStageEvent event, PoseStack poseStack, Player player, Vec3 camera, float progress, MobEffectCategory effectCategory, MEVColor color);
}
