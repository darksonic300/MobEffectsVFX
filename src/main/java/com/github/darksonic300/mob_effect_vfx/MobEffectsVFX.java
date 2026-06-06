package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.particle.LoweringParticles;
import com.github.darksonic300.mob_effect_vfx.particle.RisingParticles;
import com.github.darksonic300.mob_effect_vfx.registry.MEVParticles;
import com.github.darksonic300.mob_effect_vfx.util.MEVColor;
import com.mojang.logging.LogUtils;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.apache.logging.slf4j.Log4jLogger;
import org.slf4j.Logger;

@Mod(value = MobEffectsVFX.MODID, dist = Dist.CLIENT)
public class MobEffectsVFX {
	public static final String MODID = "mob_effects_vfx";
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

		@SuppressWarnings("deprecated")
		@SubscribeEvent
		public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
			Minecraft.getInstance().particleEngine.register(MEVParticles.RISING_PARTICLES.get(),
					RisingParticles.Provider::new);
			Minecraft.getInstance().particleEngine.register(MEVParticles.LOWERING_PARTICLES.get(),
					LoweringParticles.Provider::new);
		}

		@SubscribeEvent
		public static void onConfigLoad(final ModConfigEvent event) {
			MobEffectsVFX.LOGGER.info("Loading Blocklists config");
			ClientSideRenderingEvents.blocklist.clear();
			ClientSideRenderingEvents.blocklist.addAll(MEVConfig.CLIENT.blocklist.get().stream()
					.map(entry -> BuiltInRegistries.MOB_EFFECT.get(ResourceLocation.parse(entry))).toList());

			ClientSideRenderingEvents.entityBlocklist.clear();
			ClientSideRenderingEvents.entityBlocklist.addAll(MEVConfig.CLIENT.entityBlocklist.get().stream()
					.map(entry -> BuiltInRegistries.ENTITY_TYPE.get(ResourceLocation.parse(entry))).toList());
		}
	}
}
