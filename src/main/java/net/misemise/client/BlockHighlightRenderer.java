package net.misemise.client;

import org.joml.Matrix4f;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.misemise.OreMiner;

import java.util.HashSet;
import java.util.Set;

/**
 * BlockHighlightRenderer - ブロックにぴったり合うアウトラインを描く版
 * 注意: Mixin 側から immediate と poseMatrix を渡す実装を想定しています
 */
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
            if (blocks != null) highlightedBlocks.addAll(blocks);
        }
    }

    public static void clearHighlights() {
        synchronized (highlightedBlocks) {
            highlightedBlocks.clear();
        }
    }

    /**
     * Mixin から呼ばれるレンダリング関数
     * @param immediate VertexConsumerProvider.Immediate
     * @param poseMatrix WorldRenderer の positionMatrix（ワールド→ビュー→クリップ行列）
     */
    public static void render(VertexConsumerProvider.Immediate immediate, Matrix4f poseMatrix) {
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

            // 微小な外側オフセット（必要なければ 0f にする）
            final float OUTSET = 0.0001f; // ← これを 0 にすれば "正確にブロック境界" に合わせられる

            for (BlockPos pos : blocksCopy) {
                // 明示的に pos -> pos+1 の範囲で作る（expand は使わない）
                float x0 = pos.getX() - OUTSET;
                float y0 = pos.getY() - OUTSET;
                float z0 = pos.getZ() - OUTSET;
                float x1 = pos.getX() + 1.0f + OUTSET;
                float y1 = pos.getY() + 1.0f + OUTSET;
                float z1 = pos.getZ() + 1.0f + OUTSET;

                // 12 本のエッジを描く（各辺を2頂点で送る）
                drawEdge(vertexConsumer, poseMatrix, x0, y0, z0, x1, y0, z0, ri, gi, bi, ai); // bottom edge 1
                drawEdge(vertexConsumer, poseMatrix, x1, y0, z0, x1, y0, z1, ri, gi, bi, ai);
                drawEdge(vertexConsumer, poseMatrix, x1, y0, z1, x0, y0, z1, ri, gi, bi, ai);
                drawEdge(vertexConsumer, poseMatrix, x0, y0, z1, x0, y0, z0, ri, gi, bi, ai);

                drawEdge(vertexConsumer, poseMatrix, x0, y1, z0, x1, y1, z0, ri, gi, bi, ai); // top
                drawEdge(vertexConsumer, poseMatrix, x1, y1, z0, x1, y1, z1, ri, gi, bi, ai);
                drawEdge(vertexConsumer, poseMatrix, x1, y1, z1, x0, y1, z1, ri, gi, bi, ai);
                drawEdge(vertexConsumer, poseMatrix, x0, y1, z1, x0, y1, z0, ri, gi, bi, ai);

                drawEdge(vertexConsumer, poseMatrix, x0, y0, z0, x0, y1, z0, ri, gi, bi, ai); // vertical
                drawEdge(vertexConsumer, poseMatrix, x1, y0, z0, x1, y1, z0, ri, gi, bi, ai);
                drawEdge(vertexConsumer, poseMatrix, x1, y0, z1, x1, y1, z1, ri, gi, bi, ai);
                drawEdge(vertexConsumer, poseMatrix, x0, y0, z1, x0, y1, z1, ri, gi, bi, ai);
            }
        } catch (Throwable t) {
            OreMiner.LOGGER.error("Error rendering highlights", t);
        }
    }

    // helper: 1本の辺を2頂点で送る
    private static void drawEdge(VertexConsumer vc, Matrix4f mat,
                                 float ax, float ay, float az, float bx, float by, float bz,
                                 int ri, int gi, int bi, int ai) {
        // 法線は線なので適当に 0 を渡しても OK
        vc.vertex(mat, ax, ay, az).color(ri, gi, bi, ai).texture(0f, 0f).overlay(0).light(0xF000F0).normal(0f, 1f, 0f);
        vc.vertex(mat, bx, by, bz).color(ri, gi, bi, ai).texture(0f, 0f).overlay(0).light(0xF000F0).normal(0f, 1f, 0f);
    }
}
