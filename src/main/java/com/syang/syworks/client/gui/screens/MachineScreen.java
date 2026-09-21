package com.syang.syworks.client.gui.screens;

import com.syang.syworks.world.inventory.MachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Machine GUI, shared by every machine. Reuses the vanilla furnace background panel and its GUI sprites for
 * the flame (fuel remaining) and progress arrow, drawn at the vanilla slot coordinates.
 */
public class MachineScreen extends AbstractContainerScreen<MachineMenu> {

    private static final Identifier TEXTURE =
            Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier LIT_SPRITE =
            Identifier.withDefaultNamespace("container/furnace/lit_progress");
    private static final Identifier BURN_SPRITE =
            Identifier.withDefaultNamespace("container/furnace/burn_progress");

    public MachineScreen(MachineMenu menu, Inventory playerInventory, Component title) {
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
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        // Flame (14x14) above the fuel slot, revealed from the bottom by remaining fuel.
        int flame = this.menu.getScaledFuel();
        if (flame > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_SPRITE, 14, 14, 0, 14 - flame,
                    x + 56, y + 36 + 14 - flame, 14, flame);
        }

        // Progress arrow (24x16), revealed left-to-right by cook progress.
        int arrow = this.menu.getScaledProgress();
        if (arrow > 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_SPRITE, 24, 16, 0, 0, x + 79, y + 34, arrow, 16);
        }
    }
}
