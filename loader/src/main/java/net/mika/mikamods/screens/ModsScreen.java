package net.mika.mikamods.screens;

import net.mika.mikamods.loader.ModContainer;
import net.mika.mikamods.loader.ModLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.List;

public class ModsScreen extends Screen {

    private final Screen parent;
    private final List<ModContainer> mods;
    private int scrollOffset = 0;
    private final int lineHeight = 12;

    public ModsScreen(Screen parent) {
        super(Text.of("Installed Mods"));
        this.parent = parent;
        this.mods = ModLoader.getMods();
    }

    @Override
    protected void init() {
        // Back button
        this.addButton(new ButtonWidget(
                this.width / 2 - 100,
                this.height - 28,
                200,
                20,
                Text.of("Back"),
                button -> this.client.openScreen(parent)
        ));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        // Draw title
        String titleText = this.title.getString();
        int titleWidth = this.client.textRenderer.getWidth(titleText);
        int titleX = this.width / 2 - titleWidth / 2; // center manually
        this.client.textRenderer.drawWithShadow(matrices, titleText, titleX, 10, 0xFFFFFF);

        // Draw mods list
        int y = 30 - scrollOffset;
        for (ModContainer mod : mods) {
            this.client.textRenderer.drawWithShadow(matrices, mod.id + " " + mod.version, 20, y, 0xFFFFFF);
            y += lineHeight;
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    // Handle basic scrolling with mouse wheel
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        int visibleHeight = this.height - 32; // space for Back button
        int contentHeight = mods.size() * lineHeight;
        scrollOffset -= (int)(amount * lineHeight * 3); // scroll 3 lines per tick
        scrollOffset = Math.max(0, scrollOffset);
        scrollOffset = Math.min(Math.max(0, contentHeight - visibleHeight), scrollOffset);
        return true;
    }
}