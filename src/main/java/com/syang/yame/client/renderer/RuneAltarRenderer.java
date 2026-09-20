package com.syang.yame.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.syang.yame.world.level.block.entity.RuneAltarBlockEntity;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.book.BookModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * Draws a floating, slowly spinning, page-flipping book above the Rune Altar — the iconic
 * enchanting-table look, since a cube model can't show a hovering book. Mirrors the vanilla
 * {@link EnchantTableRenderer} (same book model + texture), but drives the animation from game
 * time rather than block-entity state, so no per-tick fields are needed.
 */
public class RuneAltarRenderer implements BlockEntityRenderer<RuneAltarBlockEntity, RuneAltarRenderState> {

    private final SpriteGetter sprites;
    private final BookModel bookModel;

    public RuneAltarRenderer(BlockEntityRendererProvider.Context context) {
        this.sprites = context.sprites();
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public RuneAltarRenderState createRenderState() {
        return new RuneAltarRenderState();
    }

    @Override
    public void extractRenderState(RuneAltarBlockEntity blockEntity, RuneAltarRenderState state, float partialTick,
                                   Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPosition, breakProgress);
        state.time = (blockEntity.getLevel() == null ? 0.0F : (float) blockEntity.getLevel().getGameTime()) + partialTick;
    }

    @Override
    public void submit(RuneAltarRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float time = state.time;

        pose.pushPose();
        pose.translate(0.5F, 0.75F + 0.1F + Mth.sin(time * 0.1F) * 0.01F, 0.5F);
        pose.mulPose(Axis.YP.rotation(time * 0.03F));
        pose.mulPose(Axis.ZP.rotationDegrees(80.0F));

        float pages = time * 0.02F;
        float flipLeft = Mth.clamp(Mth.frac(pages + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float flipRight = Mth.clamp(Mth.frac(pages + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        BookModel.State bookState = BookModel.State.forAnimation(time, flipLeft, flipRight, 1.0F);

        collector.submitModel(this.bookModel, bookState, pose, state.lightCoords, OverlayTexture.NO_OVERLAY, -1,
                EnchantTableRenderer.BOOK_TEXTURE, this.sprites, 0, state.breakProgress);
        pose.popPose();
    }

    /** The book floats above the block, so the culling box must reach past the top face. */
    @Override
    public AABB getRenderBoundingBox(RuneAltarBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.5, pos.getZ() + 1.0);
    }
}
