package org.tweak.tweaks.mixin.client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.query.PingResultS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.tweak.tweaks.client.hud.PingMeter;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onPingResult", at = @At("TAIL"))
    private void tweaks$onPingResult(PingResultS2CPacket packet, CallbackInfo ci) {
        PingMeter.onPong(packet.startTime());
    }
}
