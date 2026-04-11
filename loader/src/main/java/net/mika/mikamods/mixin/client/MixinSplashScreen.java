package net.mika.mikamods.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.SplashScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SplashScreen.class)
public class MixinSplashScreen {

    private static final Identifier LOGO =
            new Identifier("mikamods", "textures/gui/loading.png"); // 256x256 image

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        int renderW = 256; // how big you want it on screen
        int renderH = 256; // keep same as width for square 256x256 texture

        // put in bottom-right corner
        int x = (screenWidth / 2) - renderW / 2;
        int y = (screenHeight / 2) - renderH / 2;

        RenderSystem.enableBlend();
        mc.getTextureManager().bindTexture(LOGO);

        // 6-arg drawTexture for 256x256 texture
        DrawableHelper.drawTexture(
                matrices,
                x, y,
                0, 0,
                renderW, renderH,
                renderW, renderH
        );
    }
}