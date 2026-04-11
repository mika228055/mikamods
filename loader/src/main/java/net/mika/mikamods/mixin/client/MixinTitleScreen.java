package net.mika.mikamods.mixin.client;

import net.mika.mikamods.screens.ModsScreen;
import net.mika.mikamods.util.Constants;
import net.mika.mikamods.util.LoggerUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends Screen {
    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        TextRenderer renderer = mc.textRenderer;

        int screenWidth = mc.getWindow().getScaledWidth();
        int screenHeight = mc.getWindow().getScaledHeight();

        renderer.drawWithShadow(matrices, "MikaMods v" + Constants.MODLOADER_VERSION, 2, screenHeight - 19, 0xFFFFFF);
    }

    @ModifyVariable(
            method = "render",
            at = @At("STORE"),
            ordinal = 0
    )
    private String modifyVersionString(String original) {

        if (original.startsWith("Minecraft ")) {
            return original.replace(
                    "modified",
                    "mikamods"
            );
        }

        return original;
    }

    protected MixinTitleScreen(Text title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        mc.getWindow().setTitle("MikaMods v" + Constants.MODLOADER_VERSION);

        // Add button
        this.addButton(
                new ButtonWidget(
                        2,
                        2,
                        50,
                        20,
                        Text.of("Mods"),
                        button -> {
                            mc.openScreen(new ModsScreen(this));
                        }
                )
        );
    }
}
