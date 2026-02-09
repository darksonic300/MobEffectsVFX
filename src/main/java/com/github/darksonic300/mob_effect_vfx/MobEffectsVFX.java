package com.github.darksonic300.mob_effect_vfx;

import com.github.darksonic300.mob_effect_vfx.particle.LoweringParticles;
import com.github.darksonic300.mob_effect_vfx.registry.MEVParticles;
import com.github.darksonic300.mob_effect_vfx.particle.RisingParticles;
import com.github.darksonic300.mob_effect_vfx.util.ActivationTriggers;
import com.github.darksonic300.mob_effect_vfx.util.EffectTypes;
import com.github.darksonic300.mob_effect_vfx.util.MEVColor;
import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.ForgeRegistries;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;


@Mod(MobEffectsVFX.MODID)
@OnlyIn(Dist.CLIENT)
public class MobEffectsVFX {
    public static final String MODID = "mob_effects_vfx";
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final Map<MobEffect, Integer> activeEffectsTracker = new HashMap<>();

    public static final List<ActiveEffectVisual> activeVisuals = new ArrayList<>();

    public record ActiveEffectVisual(MobEffect effect, long startTime, MEVColor color) {}

    public MobEffectsVFX() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        MEVParticles.register(modEventBus);
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT,
                MEVConfig.CLIENT_SPEC);
    }

    @Mod.EventBusSubscriber(modid = MobEffectsVFX.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
    public static class ForgeClientBusEvents {
        private static final List<MobEffect> blacklist = Lists.newArrayList();
        private static final List<MobEffect> potions = Lists.newArrayList();

        /* TODO: Filtering Effect Activation (potions, active, passive...)
        @SubscribeEvent
        public static void registerRenderers(LivingEntityUseItemEvent event) {
            if(!(event.getItem().getItem() instanceof PotionItem)) return;

            if(MEVConfig.CLIENT.action.get() == ActivationTriggers.PASSIVE)
                potions.addAll(
                        PotionUtils.getMobEffects(event.getItem())
                        .stream().map(MobEffectInstance::getEffect).toList()
            );

        }

         */

        @SubscribeEvent
        public static void registerRenderers(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END) {
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null || mc.player == null) {
                if (!activeEffectsTracker.isEmpty())
                    activeEffectsTracker.clear();
                if (!activeVisuals.isEmpty())
                    activeVisuals.clear();
                return;
            }
            LocalPlayer player = mc.player;

            Map<MobEffect, Integer> currentEffects = new HashMap<>();

            blacklist.addAll(
                MEVConfig.CLIENT.blacklist.get().stream().map(
                (entry) ->
                        ForgeRegistries.MOB_EFFECTS.getHolder(ResourceLocation.parse(entry)).get().get()
                ).toList()
            );

            for (MobEffectInstance instance : player.getActiveEffects()) {
                MobEffect effect = instance.getEffect();
                int currentDuration = instance.getDuration();

                if(blacklist.contains(effect)
                        //|| potions.contains(effect)
                ) continue;

                if (!activeEffectsTracker.containsKey(effect)) {
                    triggerEffectVFX(effect);
                    triggerSoundAndParticles(mc, player, effect);
                }
                else if (currentDuration > (activeEffectsTracker.get(effect) + MEVConfig.CLIENT.refresh_cooldown.get())) {
                    triggerEffectVFX(effect);
                    triggerSoundAndParticles(mc, player, effect);
                }

                currentEffects.put(effect, currentDuration);
            }

            //potions.clear();

            activeEffectsTracker.clear();
            activeEffectsTracker.putAll(currentEffects);
        }
    }

    private static void triggerSoundAndParticles(Minecraft mc, LocalPlayer player, MobEffect effect) {
        mc.level.playLocalSound(player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1, 1, true);
        spawnParticles(effect, player, getEffectColor(effect));
    }

    private static void spawnParticles(MobEffect effect, LocalPlayer player, MEVColor color) {
        if (!MEVConfig.CLIENT.effect_type.get().equals(EffectTypes.RISING)) return;

        var particle = effect.isBeneficial() ? MEVParticles.RISING_PARTICLES.get() : MEVParticles.LOWERING_PARTICLES.get();
        var random = new Random();
        for (int i = 0; i < 3; i++) {
            player.level().addParticle(
                    particle,
                    player.getX() + randomRange(random, -0.8f, 0f),
                    player.getY() + 1 + randomRange(random, 0f, 0.6f),
                    player.getZ() + randomRange(random, 0f, 0.8f),
                    color.r(), color.g(), color.b()
            );
        }
        for (int i = 0; i < 3; i++) {
            player.level().addParticle(
                    particle,
                    player.getX() + randomRange(random, 0f, 0.8f),
                    player.getY() + 1 + randomRange(random, -0.6f, 0f),
                    player.getZ() + randomRange(random, -0.8f, 0f),
                    color.r(), color.g(), color.b()
            );
        }
    }

    private static void triggerEffectVFX(MobEffect effect) {
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

    private static float randomRange(Random random, float min, float max) {
        return min + (max - min) * random.nextFloat();
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
}
