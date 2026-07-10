package com.syang.mcextraction.client.gui.screens;

import com.syang.mcextraction.MCExtraction;
import com.syang.mcextraction.world.inventory.RuneAltarMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Rune Altar GUI. Custom background panel only — the altar is anvil-style (instant result
 * preview), so there is no progress/cast animation to render. The panel bakes in a static
 * "+"/arrow between the slots to convey base + catalyst → result.
 */
public class RuneAltarScreen extends AbstractContainerScreen<RuneAltarMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MCExtraction.MOD_ID, "textures/gui/rune_altar.png");

    public RuneAltarScreen(RuneAltarMenu menu, Inventory playerInventory, Component title) {
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
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
