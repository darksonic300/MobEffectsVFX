package com.github.darksonic300.mobeffectsvfx;

import com.github.darksonic300.mobeffectsvfx.model.CuboidRenderer;
import com.github.darksonic300.mobeffectsvfx.particle.MEVLoweringParticles;
import com.github.darksonic300.mobeffectsvfx.particle.MEVRisingParticles;
import com.github.darksonic300.mobeffectsvfx.registry.MEVParticles;
import com.github.darksonic300.mobeffectsvfx.registry.MEVVFXRenderers;
import com.github.darksonic300.mobeffectsvfx.util.MEVColor;
import com.github.darksonic300.mobeffectsvfx.util.MEVVisualLogic;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.device.GpuDevice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ExtractBlockScreenEffectEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import org.joml.Vector4f;

@EventBusSubscriber(modid = MobEffectsVFX.MODID, value = Dist.CLIENT)
public class MEVClientEvents {
    static GpuDevice device;
    static GpuTexture texture;
    static GpuTextureView textureView;
    static SubmitNodeStorage collector = new SubmitNodeStorage();
    static FeatureRenderDispatcher.PreparedFrame frame;

    @SubscribeEvent
	public static void onModInit(final FMLClientSetupEvent event) {
		MobEffectsVFX.LOGGER.info("Hello from MobEffectsVFX! Adding more clutter to the log.");
		MEVDataManager.initColorMap();
	}

	@SubscribeEvent
	public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
		event.registerSpriteSet(MEVParticles.RISING_PARTICLES.get(), MEVRisingParticles.Provider::new);
		event.registerSpriteSet(MEVParticles.LOWERING_PARTICLES.get(), MEVLoweringParticles.Provider::new);
	}

	@SubscribeEvent
	public static void onConfigLoad(final ModConfigEvent event) {
		MobEffectsVFX.LOGGER.info("Loading Blocklists config");
		MEVDataManager.EFFECT_BLOCKLIST.clear();
		MEVDataManager.EFFECT_BLOCKLIST.addAll(MEVConfig.CLIENT.blocklist.get().stream()
				.map(entry -> BuiltInRegistries.MOB_EFFECT.getValue(Identifier.parse(entry))).toList());

		MEVDataManager.ENTITY_BLOCKLIST.clear();
		MEVDataManager.ENTITY_BLOCKLIST.addAll(MEVConfig.CLIENT.entityBlocklist.get().stream()
				.map(entry -> BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(entry))).toList());
	}

	// <-- ENTITY EVENTS -->

	@SubscribeEvent
	public static void onEntityLeave(final EntityLeaveLevelEvent event) {
		if (event.getEntity() instanceof LivingEntity && event.getLevel().isClientSide()) {
			MEVDataManager.EFFECT_CACHE.invalidate(event.getEntity().getUUID());
		}
	}

    @SubscribeEvent
    public static void onPlayerJoin(final ClientPlayerNetworkEvent.LoggingIn event) {
        device = RenderSystem.getDevice();
        texture = device.createTexture("Test MEV Texture", GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_RENDER_ATTACHMENT, GpuFormat.RGBA8_UNORM, 100, 100, 1, 1);
        textureView = device.createTextureView(texture);
        device.createCommandEncoder().clearColorTexture(texture, new Vector4f(0f));
    }

	@SubscribeEvent
	public static void onPlayerLeave(final ClientPlayerNetworkEvent.LoggingOut event) {
		MEVDataManager.clearAllState();
	}

	// <-- RENDERING EVENTS -->

	@SubscribeEvent
	public static void submitGeometry(final SubmitCustomGeometryEvent event) {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || MEVDataManager.ACTIVE_VISUALS.isEmpty()) {
            return;
        }
        PoseStack poseStack = event.getPoseStack();

        var iterator = MEVDataManager.ACTIVE_VISUALS.iterator();
        while (iterator.hasNext()) {
            var item = iterator.next();
            poseStack.pushPose();
            CuboidRenderer renderer = MEVVFXRenderers.get(MEVConfig.CLIENT.effect_type.get()).apply(item.effect().getCategory(), MEVColor.getEffectColor(item.effect()));
            boolean hasFinished = MEVVisualLogic.animationLoop(event, renderer, item);
            if (hasFinished)
                iterator.remove();
            poseStack.popPose();
        }
    }
}
