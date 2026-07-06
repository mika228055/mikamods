package net.mika.mikamods.mixin.client;

import net.mika.mikamods.event.EventBus;
import net.mika.mikamods.event.events.ClientTickEvent;
import net.mika.mikamods.loader.ModLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        EventBus.post(new ClientTickEvent());
    }
}
