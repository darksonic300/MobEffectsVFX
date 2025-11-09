package com.github.darksonic300.effect_buff;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.living.PotionColorCalculationEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Mod(EffectBuff.MODID)
@OnlyIn(Dist.CLIENT)
public class EffectBuff {

    // Define mod id in a common place for everything to reference
    public static final String MODID = "effect_buff";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();

    public EffectBuff() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ParticleRegistry.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static final long ANIMATION_DURATION_MS = 350L; // 1 second duration for the rising cuboid

    public record ActiveEffectVisual(MobEffect effect, long startTime) {}

    // Map to track effects that were active on the previous tick for NEW effect detection.
    private static final Map<MobEffect, Integer> activeEffectsTracker = new HashMap<>();

    // List of currently playing 3D visual animations. Public for access by the renderer.
    public static final List<ActiveEffectVisual> activeVisuals = new ArrayList<>();


    @Mod.EventBusSubscriber(modid = EffectBuff.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ModEventClientBusEvents {
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

            // Use a new map to store the current tick's effects and durations
            Map<MobEffect, Integer> currentEffects = new HashMap<>();

            // 1. Check for newly applied or REAPPLIED effects
            for (MobEffectInstance instance : player.getActiveEffects()) {
                MobEffect effect = instance.getEffect();
                int currentDuration = instance.getDuration();

                // Check if the effect is NOT present in the tracker (Scenario 1: Truly New)
                if (!activeEffectsTracker.containsKey(effect)) {
                    triggerEffectVisual(effect);
                }
                // Check if the effect IS present, but the new duration is greater than the old one
                // (Scenario 2: Reapplied/Extended)
                // Use a small buffer (e.g., 2 ticks) to account for client-side timing.
                else if (currentDuration > (activeEffectsTracker.get(effect) + 2)) {
                    triggerEffectVisual(effect);
                    mc.level.playLocalSound(player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1, 1, true);
                }

                // Add the current effect and its duration to the map for the next tick's comparison
                currentEffects.put(effect, currentDuration);
            }

            // 2. Update the tracker for the next tick
            activeEffectsTracker.clear();
            activeEffectsTracker.putAll(currentEffects);
        }
    }

    private static void triggerEffectVisual(MobEffect effect) {
        // 1. Try to find an existing visual
        ActiveEffectVisual existing = activeVisuals.stream()
                .filter(visual -> visual.effect().equals(effect))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            // 2. If found, refresh its start time to restart the animation
            activeVisuals.remove(existing); // Remove the old instance
            activeVisuals.add(new ActiveEffectVisual(effect, Util.getMillis())); // Add a new one with current time
        } else {
            // 3. If not found, add a new one
            activeVisuals.add(new ActiveEffectVisual(effect, Util.getMillis()));
        }
    }
}
