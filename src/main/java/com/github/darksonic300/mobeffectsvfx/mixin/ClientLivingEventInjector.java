package com.github.darksonic300.mobeffectsvfx.mixin;

import com.github.darksonic300.mobeffectsvfx.MobEffectsHandlingEvents;
import com.mojang.logging.LogUtils;
import net.minecraft.client.multiplayer.ClientLevel;
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
            method = "forceAddEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)V",
            at = @At("TAIL")
    )
    private void injectClientEffectVisuals(MobEffectInstance instance, Entity entity, CallbackInfo ci) {
        System.out.println("Hey!");
        LivingEntity self = (LivingEntity) (Object) this;
        if (MobEffectsHandlingEvents.ENTITY_BLOCKLIST.contains(self.getType())) {
            return;
        }
        try {
            MobEffectsHandlingEvents.processLivingVisuals(self, (ClientLevel) this.level(), instance);
        } catch (Exception e) {
            LogUtils.getLogger().warn("MobEffectsVFX threw an exception", e);
        }
    }
}
