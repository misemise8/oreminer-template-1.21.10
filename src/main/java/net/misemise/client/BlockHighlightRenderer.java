package net.misemise.client;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.BlockPos;
import net.misemise.OreMiner;

import java.util.HashSet;
import java.util.Set;

/**
 * BlockHighlightRenderer - GL11 を使って深度テストを無効にし、アウトラインを常に見えるようにする版
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
     * @param camera カメラ（カメラ位置を取得するため）
     * @param positionMatrix WorldRenderer の positionMatrix（ワールド→ビュー→クリップ行列）
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

            // カメラ位置を取り、positionMatrix にカメラオフセットをかけた行列を使う
            float camX = (float) camera.getPos().x;
            float camY = (float) camera.getPos().y;
            float camZ = (float) camera.getPos().z;
            Matrix4f viewMatrix = new Matrix4f(positionMatrix);
            viewMatrix.translate(-camX, -camY, -camZ);

            // ===== 深度テストを無効にして描画（GL11 を使用） =====
            // Disable depth test so lines are always visible through blocks
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            // Don't write to depth buffer so we don't break other rendering
            GL11.glDepthMask(false);

            final float OUTSET = 0.0001f; // 必要なら 0 にしてピタリ合わせる

            for (BlockPos pos : blocksCopy) {
                float x0 = pos.getX() - OUTSET;
                float y0 = pos.getY() - OUTSET;
                float z0 = pos.getZ() - OUTSET;
                float x1 = pos.getX() + 1.0f + OUTSET;
                float y1 = pos.getY() + 1.0f + OUTSET;
                float z1 = pos.getZ() + 1.0f + OUTSET;

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

            // immediate.draw でバッファをフラッシュ
            immediate.draw(RenderLayer.getLines());

            // ===== 深度設定を元に戻す =====
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

        } catch (Throwable t) {
            OreMiner.LOGGER.error("Error rendering highlights", t);
            // 例外発生時も深度設定を元に戻す（念のため）
            try {
                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            } catch (Throwable ignored) {}
        }
    }

    // helper: 1本の辺を2頂点で送る
    private static void drawEdge(VertexConsumer vc, Matrix4f mat,
                                 float ax, float ay, float az, float bx, float by, float bz,
                                 int ri, int gi, int bi, int ai) {
        vc.vertex(mat, ax, ay, az).color(ri, gi, bi, ai).texture(0f, 0f).overlay(0).light(0xF000F0).normal(0f, 1f, 0f);
        vc.vertex(mat, bx, by, bz).color(ri, gi, bi, ai).texture(0f, 0f).overlay(0).light(0xF000F0).normal(0f, 1f, 0f);
    }
}
