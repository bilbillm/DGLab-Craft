package com.lumoren.dglabcraft.mixin;

import com.lumoren.dglabcraft.DGLabCraft;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "hurt", at = @At("HEAD"))
    private void dglabcraft$cacheDamageSource(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Object self = this;
        if (self instanceof Player) {
            DGLabCraft.DAMAGE_HANDLER.cacheDamageSource((Player) self, source);
        }
    }
}
