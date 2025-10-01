package com.yourorg.worldrise.vendor.litho.mixin.client;

import com.yourorg.worldrise.vendor.litho.worldgen.modifier.Modifier;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IntegratedServer.class)
public final class IntegratedServerMixin {
        @Inject(method = "initServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/server/IntegratedServer;loadLevel()V", shift = At.Shift.BEFORE), allow = 1)
        private void applyModdedBiomeSlices(CallbackInfoReturnable<Boolean> info) {
                Modifier.applyModifiers((MinecraftServer) (Object) this);
        }
}
