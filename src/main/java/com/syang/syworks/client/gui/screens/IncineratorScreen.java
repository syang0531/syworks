package com.syang.syworks.client.gui.screens;

import com.syang.syworks.world.inventory.IncineratorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * Incinerator GUI. Built out of the vanilla furnace panel rather than a texture of its own: the
 * furnace's slots and arrow are painted over with the panel colour, one slot is copied back into
 * the middle, and the furnace flame sits under it while something is burning.
 */
public class IncineratorScreen extends AbstractContainerScreen<IncineratorMenu> {

    private static final Identifier TEXTURE =
            Identifier.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final Identifier LIT_SPRITE =
            Identifier.withDefaultNamespace("container/furnace/lit_progress");

    /** Vanilla container panel grey. */
    private static final int PANEL = 0xFFC6C6C6;
    /** Where the furnace's input slot frame sits in furnace.png — borrowed as our one slot. */
    private static final int SLOT_U = 55;
    private static final int SLOT_V = 16;

    public IncineratorScreen(IncineratorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        this.titleLabelY = 6;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int x = this.leftPos;
        int y = this.topPos;

        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);

        // Wipe the furnace's input, fuel, arrow and output down to bare panel.
        graphics.fill(x + 8, y + 15, x + 168, y + 76, PANEL);

        // One slot in the middle.
        int slotX = x + IncineratorMenu.SLOT_X - 1;
        int slotY = y + IncineratorMenu.SLOT_Y - 1;
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, slotX, slotY, SLOT_U, SLOT_V, 18, 18, 256, 256);

        // A full flame under the slot while the last thing is still burning.
        if (this.menu.isBurning()) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LIT_SPRITE, x + IncineratorMenu.SLOT_X + 1, slotY + 20, 14, 14);
        }
    }
}
