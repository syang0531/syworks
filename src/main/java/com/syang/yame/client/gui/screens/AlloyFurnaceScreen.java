package com.syang.yame.client.gui.screens;

import com.syang.yame.Yame;
import com.syang.yame.world.inventory.AlloyFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * Alloy Furnace GUI. Custom background panel + the vanilla 1.21 furnace GUI sprites for the
 * flame (fuel remaining) and progress arrow, so it looks consistent with vanilla machines.
 */
public class AlloyFurnaceScreen extends AbstractContainerScreen<AlloyFurnaceMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Yame.MOD_ID, "textures/gui/alloy_furnace.png");
    private static final ResourceLocation LIT_SPRITE =
            ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");
    private static final ResourceLocation BURN_SPRITE =
            ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");

    public AlloyFurnaceScreen(AlloyFurnaceMenu menu, Inventory playerInventory, Component title) {
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

        // Flame (14x14) above the fuel slot (56,53), revealed from the bottom by remaining fuel.
        int flame = this.menu.getScaledFuel();
        if (flame > 0) {
            guiGraphics.blitSprite(LIT_SPRITE, 14, 14, 0, 14 - flame, x + 56, y + 36 + 14 - flame, 14, flame);
        }

        // Progress arrow (24x16) between inputs and output, revealed left-to-right.
        // x+79 aligns with the composited vanilla empty-arrow background in the texture.
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
