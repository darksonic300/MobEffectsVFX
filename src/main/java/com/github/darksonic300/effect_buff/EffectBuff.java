package com.github.darksonic300.effect_buff;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
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

    public static final long ANIMATION_DURATION_MS = 1000L; // 1 second duration for the rising cuboid

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
                if (!activeEffectsTracker.isEmpty()) activeEffectsTracker.clear();
                if (!activeVisuals.isEmpty()) activeVisuals.clear();
                return;
            }

            LocalPlayer player = mc.player;
            long currentTime = Util.getMillis();

            Map<MobEffect, Integer> currentEffects = new HashMap<>();

            // 1. Check for newly applied effects
            for (MobEffectInstance instance : player.getActiveEffects()) {
                // Note: In 1.20.1, getEffect() returns the MobEffect, which is used directly.
                MobEffect effect = instance.getEffect();

                // Check if this effect is NEWLY APPLIED
                if (!activeEffectsTracker.containsKey(effect)) {
                    // Effect found! Trigger the visual animation.
                    triggerEffectVisual(effect);
                }

                // Add the current effect to the map for the next tick's comparison
                currentEffects.put(effect, instance.getDuration());
            }

            // 2. Update the tracker for the next tick
            activeEffectsTracker.clear();
            activeEffectsTracker.putAll(currentEffects);

            // 3. Remove finished visuals (Cleanup)
            activeVisuals.removeIf(visual -> (currentTime - visual.startTime) >= ANIMATION_DURATION_MS);
        }
    }

    private static void triggerEffectVisual(MobEffect effect) {
        boolean isAlreadyAnimating = activeVisuals.stream()
                .anyMatch(visual -> visual.effect().equals(effect)
                        && (Util.getMillis() - visual.startTime()) < ANIMATION_DURATION_MS);

        if (!isAlreadyAnimating) {
            // Add a new visual animation to the list, tracking its start time.
            activeVisuals.add(new ActiveEffectVisual(effect, Util.getMillis()));
        }
    }
}
