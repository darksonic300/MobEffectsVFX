package com.github.darksonic300.mobeffectsvfx;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;


@Mod(value = MobEffectsVFX.MODID)
public class MobEffectsVFX {
    public static final String MODID = "mobeffectsvfx";
    public static final Logger LOGGER = LogUtils.getLogger();

    public MobEffectsVFX(FMLJavaModLoadingContext context) {
        context.registerConfig(ModConfig.Type.CLIENT, MEVConfig.CLIENT_SPEC);
    }

    public static ResourceLocation getResource(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
