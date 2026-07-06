package net.mika.mikamods.mixin.client;

import net.mika.mikamods.loader.ModLoader;
import net.minecraft.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Bootstrap.class)
public class MixinBootstrap {
    @Inject(method = "<clinit>", at = @At("HEAD"))
    private static void onClinit(CallbackInfo ci) {
        ModLoader.loadMods();
    }
}
