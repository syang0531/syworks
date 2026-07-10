package com.syang.mcextraction.client.gui.screens;

import com.syang.mcextraction.world.inventory.ExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Extractor GUI. Reuses the vanilla furnace background panel and its 1.21 GUI sprites for
 * the flame (fuel remaining) and progress arrow, drawn at the vanilla slot coordinates.
 */
public class ExtractorScreen extends AbstractContainerScreen<ExtractorMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final ResourceLocation LIT_SPRITE =
            ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");
    private static final ResourceLocation BURN_SPRITE =
            ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");

    public ExtractorScreen(ExtractorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;

        guiGraphics.blit(TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        // Flame (14x14) above the fuel slot, revealed from the bottom by remaining fuel.
        int flame = this.menu.getScaledFuel();
        if (flame > 0) {
            guiGraphics.blitSprite(LIT_SPRITE, 14, 14, 0, 14 - flame, x + 56, y + 36 + 14 - flame, 14, flame);
        }

        // Progress arrow (24x16), revealed left-to-right by cook progress.
        int arrow = this.menu.getScaledProgress();
        if (arrow > 0) {
            guiGraphics.blitSprite(BURN_SPRITE, 24, 16, 0, 0, x + 79, y + 34, arrow, 16);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
