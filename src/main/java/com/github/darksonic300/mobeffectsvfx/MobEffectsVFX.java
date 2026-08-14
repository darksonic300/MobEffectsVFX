package com.github.darksonic300.mobeffectsvfx;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

@Mod(value = MobEffectsVFX.MODID)
public class MobEffectsVFX {
	public static final String MODID = "mobeffectsvfx";
	public static final Logger LOGGER = LogUtils.getLogger();

	public MobEffectsVFX(IEventBus bus, ModContainer modContainer) {
		modContainer.registerConfig(ModConfig.Type.CLIENT, MEVConfig.CLIENT_SPEC);
	}

    public static ResourceLocation getResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}

