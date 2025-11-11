package net.misemise.client;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.BlockPos;
import net.misemise.OreMiner;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Set;

public class BlockHighlightRenderer {
    private static final Set<BlockPos> highlightedBlocks = new HashSet<>();

    // 色（0.0 - 1.0）
    private static final float RED = 0.0f;
    private static final float GREEN = 0.8f;
    private static final float BLUE = 1.0f;
    private static final float ALPHA = 0.6f;

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

    /**
     * Mixinから呼ばれるレンダリング関数
     * @param immediate VertexConsumerProvider.Immediate
     * @param camera カメラ（カメラ位置を取得するため）
     * @param positionMatrix WorldRendererのpositionMatrix
     */
    public static void render(VertexConsumerProvider.Immediate immediate, Camera camera, Matrix4f positionMatrix) {
        Set<BlockPos> blocksCopy;
        synchronized (highlightedBlocks) {
            if (highlightedBlocks.isEmpty()) return;
            blocksCopy = new HashSet<>(highlightedBlocks);
        }

        try {
            VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayer.getLines());

            int ri = (int) (RED * 255.0f);
            int gi = (int) (GREEN * 255.0f);
            int bi = (int) (BLUE * 255.0f);
            int ai = (int) (ALPHA * 255.0f);

            // カメラ位置を取得
            float camX = (float) camera.getPos().x;
            float camY = (float) camera.getPos().y;
            float camZ = (float) camera.getPos().z;

            // 調整行列を作成（positionMatrixをコピーしてカメラ位置分だけtranslate）
            Matrix4f viewMatrix = new Matrix4f(positionMatrix);
            viewMatrix.translate(-camX, -camY, -camZ);

            for (BlockPos pos : blocksCopy) {
                // ブロックの正確な境界（オフセットなし）
                float x0 = pos.getX();
                float y0 = pos.getY();
                float z0 = pos.getZ();
                float x1 = pos.getX() + 1.0f;
                float y1 = pos.getY() + 1.0f;
                float z1 = pos.getZ() + 1.0f;

                // 12本のエッジを描画
                // 底面
                drawEdge(vertexConsumer, viewMatrix, x0, y0, z0, x1, y0, z0, ri, gi, bi, ai);
                drawEdge(vertexConsumer, viewMatrix, x1, y0, z0, x1, y0, z1, ri, gi, bi, ai);
                drawEdge(vertexConsumer, viewMatrix, x1, y0, z1, x0, y0, z1, ri, gi, bi, ai);
                drawEdge(vertexConsumer, viewMatrix, x0, y0, z1, x0, y0, z0, ri, gi, bi, ai);

                // 上面
                drawEdge(vertexConsumer, viewMatrix, x0, y1, z0, x1, y1, z0, ri, gi, bi, ai);
                drawEdge(vertexConsumer, viewMatrix, x1, y1, z0, x1, y1, z1, ri, gi, bi, ai);
                drawEdge(vertexConsumer, viewMatrix, x1, y1, z1, x0, y1, z1, ri, gi, bi, ai);
                drawEdge(vertexConsumer, viewMatrix, x0, y1, z1, x0, y1, z0, ri, gi, bi, ai);

                // 縦
                drawEdge(vertexConsumer, viewMatrix, x0, y0, z0, x0, y1, z0, ri, gi, bi, ai);
                drawEdge(vertexConsumer, viewMatrix, x1, y0, z0, x1, y1, z0, ri, gi, bi, ai);
                drawEdge(vertexConsumer, viewMatrix, x1, y0, z1, x1, y1, z1, ri, gi, bi, ai);
                drawEdge(vertexConsumer, viewMatrix, x0, y0, z1, x0, y1, z1, ri, gi, bi, ai);
            }

            // バッファを描画
            immediate.draw(RenderLayer.getLines());

        } catch (Throwable t) {
            OreMiner.LOGGER.error("Error rendering highlights", t);
        }
    }

    /**
     * 1本の辺を2頂点で描画
     */
    private static void drawEdge(VertexConsumer vc, Matrix4f mat,
                                 float ax, float ay, float az,
                                 float bx, float by, float bz,
                                 int ri, int gi, int bi, int ai) {
        vc.vertex(mat, ax, ay, az)
                .color(ri, gi, bi, ai)
                .texture(0f, 0f)
                .overlay(0)
                .light(0xF000F0)
                .normal(0f, 1f, 0f);

        vc.vertex(mat, bx, by, bz)
                .color(ri, gi, bi, ai)
                .texture(0f, 0f)
                .overlay(0)
                .light(0xF000F0)
                .normal(0f, 1f, 0f);
    }
}