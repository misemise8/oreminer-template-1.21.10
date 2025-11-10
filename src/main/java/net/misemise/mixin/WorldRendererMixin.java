package net.misemise.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import net.minecraft.client.render.*;
import net.minecraft.client.util.ObjectAllocator;
import net.misemise.OreMiner;
import net.misemise.client.BlockHighlightRenderer;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Shadow
    @Final
    private BufferBuilderStorage bufferBuilders;

    @Inject(
            method = "render",
            at = @At("TAIL")
    )
    private void onRenderEnd(
            ObjectAllocator allocator,
            RenderTickCounter tickCounter,
            boolean renderBlockOutline,
            Camera camera,
            Matrix4f positionMatrix,
            Matrix4f matrix4f,
            Matrix4f projectionMatrix,
            GpuBufferSlice fogBuffer,
            Vector4f fogColor,
            boolean renderSky,
            CallbackInfo ci
    ) {
        try {
            VertexConsumerProvider.Immediate immediate = this.bufferBuilders.getEntityVertexConsumers();

            // ここで positionMatrix をコピーしてカメラ分だけ translate(-cam) する
            Matrix4f adjusted = new Matrix4f(positionMatrix);
            // camera.getPos() は Vec3d
            adjusted.translate((float)-camera.getPos().x, (float)-camera.getPos().y, (float)-camera.getPos().z);

            // 調整行列を渡す（BlockHighlightRenderer 側は行列をそのまま使う実装）
            BlockHighlightRenderer.render(immediate, adjusted);
        } catch (Exception e) {
            OreMiner.LOGGER.error("Failed to render block highlights", e);
        }
    }
}
