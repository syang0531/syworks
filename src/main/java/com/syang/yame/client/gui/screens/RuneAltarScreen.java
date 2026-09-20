package com.syang.yame.client.gui.screens;

import com.syang.yame.Yame;
import com.syang.yame.world.inventory.RuneAltarMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Rune Altar GUI. Custom background panel only — the altar is anvil-style (instant result
 * preview), so there is no progress/cast animation to render. The panel bakes in a static
 * "+"/arrow between the slots to convey base + catalyst → result.
 */
public class RuneAltarScreen extends AbstractContainerScreen<RuneAltarMenu> {

    private static final Identifier TEXTURE =
            Identifier.fromNamespaceAndPath(Yame.MOD_ID, "textures/gui/rune_altar.png");

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
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, this.leftPos, this.topPos, 0.0F, 0.0F,
                this.imageWidth, this.imageHeight, 256, 256);
    }
}
