package net.misemise.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.misemise.OreMiner;

import java.util.HashSet;
import java.util.Set;

/**
 * ブロックハイライトレンダラー
 * WorldRenderer.drawShapeOutlineを使用してブロックのアウトラインを描画
 */
public class BlockHighlightRenderer {
    private static final Set<BlockPos> highlightedBlocks = new HashSet<>();

    // ハイライトの色 (RGBA)
    private static final float RED = 0.0f;
    private static final float GREEN = 0.8f;
    private static final float BLUE = 1.0f;
    private static final float ALPHA = 0.5f;

    public static void register() {
        OreMiner.LOGGER.info("BlockHighlightRenderer initialized");
    }

    public static void setHighlightedBlocks(Set<BlockPos> blocks) {
        synchronized (highlightedBlocks) {
            highlightedBlocks.clear();
            if (blocks != null) {
                highlightedBlocks.addAll(blocks);
                if (!blocks.isEmpty()) {
                    OreMiner.LOGGER.info("Set {} blocks to highlight", blocks.size());
                }
            }
        }
    }

    public static void clearHighlights() {
        synchronized (highlightedBlocks) {
            if (!highlightedBlocks.isEmpty()) {
                highlightedBlocks.clear();
                OreMiner.LOGGER.info("Cleared highlights");
            }
        }
    }

    public static void render(Camera camera, VertexConsumerProvider.Immediate vertexConsumers) {
        Set<BlockPos> blocksCopy;
        synchronized (highlightedBlocks) {
            if (highlightedBlocks.isEmpty()) {
                return;
            }
            blocksCopy = new HashSet<>(highlightedBlocks);
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        try {
            MatrixStack matrices = new MatrixStack();
            Vec3d cameraPos = camera.getPos();

            matrices.push();
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            // RenderLayer.getLinesを使用してラインを描画
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());

            for (BlockPos pos : blocksCopy) {
                Box box = new Box(pos).expand(0.002);

                // WorldRendererのdrawShapeOutlineメソッドを使用
                WorldRenderer.drawShapeOutline(
                        matrices,
                        vertexConsumer,
                        box.shapes().toList().get(0),
                        0, 0, 0,
                        RED, GREEN, BLUE, ALPHA
                );
            }

            matrices.pop();

            // バッファをフラッシュ
            vertexConsumers.draw();

        } catch (Exception e) {
            OreMiner.LOGGER.error("Error rendering highlights", e);
        }
    }
}