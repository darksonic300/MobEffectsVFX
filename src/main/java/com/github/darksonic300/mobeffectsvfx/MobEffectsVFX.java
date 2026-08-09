package com.github.darksonic300.mobeffectsvfx;

import com.github.darksonic300.mobeffectsvfx.particle.LoweringParticles;
import com.github.darksonic300.mobeffectsvfx.particle.RisingParticles;
import com.github.darksonic300.mobeffectsvfx.registry.MEVParticles;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import org.slf4j.Logger;

@Mod(value = MobEffectsVFX.MODID, dist = Dist.CLIENT)
public class MobEffectsVFX {
	public static final String MODID = "mobeffectsvfx";
	public static final Logger LOGGER = LogUtils.getLogger();

	public MobEffectsVFX(IEventBus bus, ModContainer modContainer) {
		MEVParticles.register(bus);
		modContainer.registerConfig(ModConfig.Type.CLIENT, MEVConfig.CLIENT_SPEC);
	}

	@EventBusSubscriber(modid = MobEffectsVFX.MODID, value = Dist.CLIENT)
	public static class ModClientBusEvents {

		@SubscribeEvent
		public static void onModInit(final FMLClientSetupEvent event) {
			MobEffectsVFX.LOGGER.info("Hello from MobEffectsVFX! Adding more clutter to the log.");
		}

		@SubscribeEvent
		public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
			event.registerSpriteSet(MEVParticles.RISING_PARTICLES.get(), RisingParticles.Provider::new);
			event.registerSpriteSet(MEVParticles.LOWERING_PARTICLES.get(), LoweringParticles.Provider::new);
		}

		@SubscribeEvent
		public static void onConfigLoad(final ModConfigEvent event) {
			MobEffectsVFX.LOGGER.info("Loading Blocklists config");
			MEVDataManager.EFFECT_BLOCKLIST.clear();
			MEVDataManager.EFFECT_BLOCKLIST.addAll(MEVConfig.CLIENT.blocklist.get().stream()
					.map(entry -> BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(entry))).toList());

			MEVDataManager.ENTITY_BLOCKLIST.clear();
			MEVDataManager.ENTITY_BLOCKLIST.addAll(MEVConfig.CLIENT.entityBlocklist.get().stream()
					.map(entry -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entry))).toList());
		}
	}
}
