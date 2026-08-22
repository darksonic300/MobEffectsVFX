package com.github.darksonic300.mobeffectvfx;

import com.github.darksonic300.mobeffectvfx.particle.LoweringParticles;
import com.github.darksonic300.mobeffectvfx.particle.RisingParticles;
import com.github.darksonic300.mobeffectvfx.registry.MEVParticles;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.ForgeRegistries;

@Mod(value = MobEffectsVFX.MODID)
public class MobEffectsVFX {
	public static final String MODID = "mob_effects_vfx";

	public MobEffectsVFX(FMLJavaModLoadingContext context) {
		if (!FMLLoader.getDist().equals(Dist.CLIENT))
			return;

		IEventBus modEventBus = context.getModEventBus();
		MEVParticles.register(modEventBus);
		MinecraftForge.EVENT_BUS.register(this);
		context.registerConfig(ModConfig.Type.CLIENT, MEVConfig.CLIENT_SPEC);
	}

	@Mod.EventBusSubscriber(modid = MobEffectsVFX.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
	public static class ModClientBusEvents {
		@SubscribeEvent
		public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
			event.registerSpriteSet(MEVParticles.RISING_PARTICLES.get(), RisingParticles.Provider::new);
			event.registerSpriteSet(MEVParticles.LOWERING_PARTICLES.get(), LoweringParticles.Provider::new);
		}

		@SubscribeEvent
		public static void onConfigLoad(final ModConfigEvent event) {
			MobEffectsHandlingEvents.BLOCKLIST.clear();
			MobEffectsHandlingEvents.BLOCKLIST.addAll(MEVConfig.CLIENT.blocklist.get().stream()
					.map(entry -> ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.parse(entry))).toList());

			MobEffectsHandlingEvents.ENTITY_BLOCKLIST.clear();
			MobEffectsHandlingEvents.ENTITY_BLOCKLIST.addAll(MEVConfig.CLIENT.entityBlocklist.get().stream()
					.map(entry -> ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.parse(entry))).toList());
		}
	}

	public record ActiveEffectVisual(LivingEntity source, MobEffect effect, long startTime) {
	}
}
