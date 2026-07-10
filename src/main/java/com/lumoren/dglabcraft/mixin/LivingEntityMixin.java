package com.lumoren.dglabcraft.mixin;

import com.lumoren.dglabcraft.DGLabCraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "hurt", at = @At("HEAD"), require = 0)
    private void dglabcraft$cacheDamageSource(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        dglabcraft$cacheDamageSource(source);
    }

    @Inject(method = "method_64397", at = @At("HEAD"), require = 0, remap = false)
    private void dglabcraft$cacheDamageSource12111(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        dglabcraft$cacheDamageSource(source);
    }

    private void dglabcraft$cacheDamageSource(DamageSource source) {
        Object self = this;
        if (self instanceof Player player) {
            DGLabCraft.DAMAGE_HANDLER.cacheDamageSource(player, source);
        }
    }
}
