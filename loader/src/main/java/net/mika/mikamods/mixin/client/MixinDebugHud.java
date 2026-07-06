package net.mika.mikamods.mixin.client;

import net.mika.mikamods.loader.ModLoader;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public class MixinDebugHud {
    @Inject(method = "getRightText", at = @At("RETURN"))
    private void addCustomF3Text(CallbackInfoReturnable<List<String>> cir) {
        List<String> list = cir.getReturnValue();

        list.add("");
        list.add("§1[MikaMods]");
        list.add("§f" + ModLoader.getMods().size() + " mods loaded");
    }
}
