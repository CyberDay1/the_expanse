package com.yourorg.worldrise.vendor.litho.mixin.common;

import com.yourorg.worldrise.vendor.litho.worldgen.modifier.Modifier;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameTestServer.class)
public final class GameTestServerMixin {
        @Inject(method = "initServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/gametest/framework/GameTestServer;loadLevel()V", shift = At.Shift.BEFORE), allow = 1)
        private void applyModdedBiomeSlices(CallbackInfoReturnable<Boolean> info) {
                Modifier.applyModifiers((MinecraftServer) (Object) this);
        }
}
