package net.mika.mikamods.mixin.client;

import net.mika.mikamods.util.Constants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ClientBrandRetriever.class)
public class MixinClientBrandRetriever {
    @Overwrite
    public static String getClientModName() {
        return "MikaMods v" + Constants.MODLOADER_VERSION;
    }
}
