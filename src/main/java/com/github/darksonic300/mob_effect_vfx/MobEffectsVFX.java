package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.particle.LoweringParticles;
import com.github.darksonic300.mob_effect_vfx.registry.MEVParticles;
import com.github.darksonic300.mob_effect_vfx.particle.RisingParticles;
import com.github.darksonic300.mob_effect_vfx.util.EffectTypes;
import com.github.darksonic300.mob_effect_vfx.util.MEVColor;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;


@Mod(MobEffectsVFX.MODID)
public class MobEffectsVFX {
    public static final String MODID = "mob_effects_vfx";
    private static final Logger LOGGER = LogUtils.getLogger();

    public record ActiveEffectVisual(MobEffect effect, long startTime, MEVColor color) {}

    public MobEffectsVFX() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MEVParticles.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,
                MEVConfig.CLIENT_SPEC);
    }

    @Mod.EventBusSubscriber(modid = MobEffectsVFX.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ModClientBusEvents {
        @SubscribeEvent
        public static void registerParticleFactories(final RegisterParticleProvidersEvent event) {
            Minecraft.getInstance().particleEngine.register(MEVParticles.RISING_PARTICLES.get(),
                    RisingParticles.Provider::new);

            Minecraft.getInstance().particleEngine.register(MEVParticles.LOWERING_PARTICLES.get(),
                    LoweringParticles.Provider::new);
        }

        @SubscribeEvent
        public static void onConfigLoad(final ModConfigEvent event) {
            ClientSideRenderEvent.blacklist.clear();
            ClientSideRenderEvent.blacklist.addAll(
                    MEVConfig.CLIENT.blacklist.get().stream()
                            .map(entry ->
                                    ForgeRegistries.MOB_EFFECTS.getHolder(ResourceLocation.parse(entry)).get().get())
                            .toList()
            );
        }
    }
}
