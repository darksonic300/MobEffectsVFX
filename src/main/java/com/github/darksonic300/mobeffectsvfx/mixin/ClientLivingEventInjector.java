package com.github.darksonic300.mobeffectsvfx.mixin;

import com.github.darksonic300.mobeffectsvfx.MEVDataManager;
import com.github.darksonic300.mobeffectsvfx.MEVEvents;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ClientLivingEventInjector extends Entity {

    public ClientLivingEventInjector(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(
            method = "onEffectAdded",
            at = @At("TAIL")
    )
    private void mobeffectsvfx$injectEffectAdded(MobEffectInstance instance, Entity entity, CallbackInfo ci) {
        this.dispatchEffectVisuals(instance);
    }

    @Inject(
            method = "onEffectUpdated",
            at = @At("TAIL")
    )
    private void mobeffectsvfx$injectEffectUpdated(MobEffectInstance instance, boolean forced, Entity entity, CallbackInfo ci) {
        this.dispatchEffectVisuals(instance);
    }

    private void dispatchEffectVisuals(MobEffectInstance instance) {
        LivingEntity self = (LivingEntity) (Object) this;
        final var level = Minecraft.getInstance().level;

        if (level == null || !level.isClientSide()
                || MEVDataManager.ENTITY_BLOCKLIST.contains(self.getType()))
            return;

        try {
            MEVEvents.processLivingVisual(self, level, instance);
        } catch (Exception e) {
            LogUtils.getLogger().warn("MobEffectsVFX threw an exception", e);
        }
    }
}
