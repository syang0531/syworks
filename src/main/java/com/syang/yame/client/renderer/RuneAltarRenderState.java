package com.syang.yame.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

/**
 * Per-frame render state for {@link RuneAltarRenderer} (26.x block-entity rendering is
 * extract-then-submit: the renderer fills this on the game thread and draws from it later).
 * The altar's book animation is driven purely by game time, so this is the only extra field.
 */
public class RuneAltarRenderState extends BlockEntityRenderState {
    public float time;
}
