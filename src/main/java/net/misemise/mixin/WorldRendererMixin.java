package net.misemise.mixin;

import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.misemise.client.BlockHighlightRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    private VertexConsumerProvider.Immediate vertexConsumerProvider;

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderEnd(RenderTickCounter tickCounter, boolean renderBlockOutline,
                             Camera camera, GameRenderer gameRenderer,
                             LightmapTextureManager lightmapTextureManager,
                             MatrixStack matrices, MatrixStack matrices2, CallbackInfo ci) {
        // レンダリングの最後にハイライトを描画
        if (vertexConsumerProvider != null) {
            BlockHighlightRenderer.render(camera, vertexConsumerProvider);
        }
    }
}