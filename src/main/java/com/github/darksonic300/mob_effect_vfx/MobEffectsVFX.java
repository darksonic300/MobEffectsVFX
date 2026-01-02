package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.particle.LoweringParticles;
import com.github.darksonic300.mob_effect_vfx.particle.MEVParticles;
import com.github.darksonic300.mob_effect_vfx.particle.RisingParticles;
import com.github.darksonic300.mob_effect_vfx.particle.VisualParticles;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@Mod(MobEffectsVFX.MODID)
@OnlyIn(Dist.CLIENT)
public class MobEffectsVFX {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "mob_effects_vfx";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public MobEffectsVFX() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MEVParticles.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,
                MEVConfig.CLIENT_SPEC);
    }


    // Map to track effects that were active on the previous tick for NEW effect detection.
    private static final Map<MobEffect, Integer> activeEffectsTracker = new HashMap<>();

    // List of currently playing 3D visual animations. Public for access by the renderer.
    public static final List<ActiveEffectVisual> activeVisuals = new ArrayList<>();

    public record ActiveEffectVisual(MobEffect effect, long startTime, MEVColor color) {}

    @Mod.EventBusSubscriber(modid = MobEffectsVFX.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeClientBusEvents {
        @SubscribeEvent
        public static void registerRenderers(TickEvent.ClientTickEvent event) {
            // Only run at the end of the client tick
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) {
                // Clear state if we leave a world
                if (!activeEffectsTracker.isEmpty())
                    activeEffectsTracker.clear();
                if (!activeVisuals.isEmpty())
                    activeVisuals.clear();
                return;
            }
            LocalPlayer player = mc.player;

            Map<MobEffect, Integer> currentEffects = new HashMap<>();

            // 1. Check for newly applied or REAPPLIED effects
            for (MobEffectInstance instance : player.getActiveEffects()) {
                MobEffect effect = instance.getEffect();
                int currentDuration = instance.getDuration();

                // Check if the effect is NOT present in the tracker
                if (!activeEffectsTracker.containsKey(effect)) {
                    triggerEffectVisual(effect);
                }
                // Check if the effect IS present, but the new duration is greater than the old one
                // (Scenario 2: Reapplied/Extended)
                // Use a small buffer (e.g., 2 ticks) to account for client-side timing.
                else if (currentDuration > (activeEffectsTracker.get(effect) + 2)) {
                    triggerEffectVisual(effect);

                    int color = effect.getColor();
                    float r = ((color >> 16) & 0xFF) / 255.0F;
                    float g = ((color >> 8) & 0xFF) / 255.0F;
                    float b = (color & 0xFF) / 255.0F;

                    mc.level.playLocalSound(player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1, 1, true);

                    spawnParticles(effect, player, r, g, b);
                }

                // Add the current effect and its duration to the map for the next tick's comparison
                currentEffects.put(effect, currentDuration);
            }

            // 2. Update the tracker for the next tick
            activeEffectsTracker.clear();
            activeEffectsTracker.putAll(currentEffects);
        }
    }

    private static void spawnParticles(MobEffect effect, LocalPlayer player, float r, float g, float b) {
        if (!MEVConfig.CLIENT.effect_type.get().equals(EffectTypes.RISING)) return;

        var particle = effect.isBeneficial() ? MEVParticles.RISING_PARTICLES.get() : MEVParticles.LOWERING_PARTICLES.get();
        var random = new Random();
        for (int i = 0; i < 3; i++) {
            player.level().addParticle(
                    particle,
                    player.getX() + randomRange(random, -0.8f, 0f),
                    player.getY() + 1 + randomRange(random, 0f, 0.6f),
                    player.getZ() + randomRange(random, 0f, 0.8f),
                    r, g, b
            );
        }
        for (int i = 0; i < 3; i++) {
            player.level().addParticle(
                    particle,
                    player.getX() + randomRange(random, 0f, 0.8f),
                    player.getY() + 1 + randomRange(random, -0.6f, 0f),
                    player.getZ() + randomRange(random, -0.8f, 0f),
                    r, g, b
            );
        }
    }

    private static void triggerEffectVisual(MobEffect effect) {
        ActiveEffectVisual existing = activeVisuals.stream()
                .filter(visual -> visual.effect().equals(effect))
                .findFirst()
                .orElse(null);

        MEVColor color = getEffectColor(effect);

        if (existing != null) {
            activeVisuals.remove(existing);
            activeVisuals.add(new ActiveEffectVisual(effect, Util.getMillis(), color));
        } else {
            // 3. If not found, add a new one
            activeVisuals.add(new ActiveEffectVisual(effect, Util.getMillis(), color));
        }
    }

    private static MEVColor getEffectColor(MobEffect effect) {
        // Get effect color (use the MobEffect's color for visual theming)
        int color = effect.getColor();
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;

        // Base Alpha value for opacity
        float a = MEVConfig.CLIENT.opacity.get().floatValue();

        return new MEVColor(r, g, b, a);
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
    }


    private static float randomRange(Random random, float min, float max) {
        return min + (max - min) * random.nextFloat();
    }
}
