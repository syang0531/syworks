package com.syang.mcextraction.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.syang.mcextraction.world.level.block.entity.RuneAltarBlockEntity;
import net.minecraft.client.model.BookModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * Draws a floating, slowly spinning, page-flipping book above the Rune Altar — the iconic
 * enchanting-table look, since a cube model can't show a hovering book. Mirrors the vanilla
 * enchanting-table renderer (same book model + texture), but drives the animation from game time
 * rather than block-entity state, so no per-tick fields are needed.
 */
public class RuneAltarRenderer implements BlockEntityRenderer<RuneAltarBlockEntity> {

    private static final Material BOOK_TEXTURE = new Material(
            TextureAtlas.LOCATION_BLOCKS, ResourceLocation.withDefaultNamespace("entity/enchanting_table_book"));

    private final BookModel bookModel;

    public RuneAltarRenderer(BlockEntityRendererProvider.Context context) {
        this.bookModel = new BookModel(context.bakeLayer(ModelLayers.BOOK));
    }

    @Override
    public void render(RuneAltarBlockEntity be, float partialTick, PoseStack pose,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        float time = (be.getLevel() == null ? 0.0F : (float) be.getLevel().getGameTime()) + partialTick;

        pose.pushPose();
        pose.translate(0.5F, 0.75F + 0.1F + Mth.sin(time * 0.1F) * 0.01F, 0.5F);
        pose.mulPose(Axis.YP.rotation(time * 0.03F));
        pose.mulPose(Axis.ZP.rotationDegrees(80.0F));

        float pages = time * 0.02F;
        float flipLeft = Mth.clamp(Mth.frac(pages + 0.25F) * 1.6F - 0.3F, 0.0F, 1.0F);
        float flipRight = Mth.clamp(Mth.frac(pages + 0.75F) * 1.6F - 0.3F, 0.0F, 1.0F);
        this.bookModel.setupAnim(time, flipLeft, flipRight, 1.0F);

        VertexConsumer vc = BOOK_TEXTURE.buffer(buffer, RenderType::entitySolid);
        this.bookModel.render(pose, vc, packedLight, packedOverlay, -1);
        pose.popPose();
    }
}
