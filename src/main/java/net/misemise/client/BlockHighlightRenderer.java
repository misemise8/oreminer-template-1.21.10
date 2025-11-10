package net.misemise.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.misemise.OreMiner;
import org.joml.Matrix4f;

import java.util.HashSet;
import java.util.Set;

public class BlockHighlightRenderer {
    private static final Set<BlockPos> highlightedBlocks = new HashSet<>();

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
            Vec3d cameraPos = camera.getPos();

            // 全ブロックに対して共通のMatrixStackを使用
            MatrixStack matrices = new MatrixStack();
            matrices.push();

            // カメラ位置を原点として座標系を変換
            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            Matrix4f matrix = matrices.peek().getPositionMatrix();

            // RenderLayer.getLinesを使用
            VertexConsumer vertexConsumer = vertexConsumers.getBuffer(RenderLayer.getLines());

            // 各ブロックのアウトラインを描画
            for (BlockPos pos : blocksCopy) {
                drawBoxOutline(vertexConsumer, matrix, pos);
            }

            matrices.pop();

            // バッファを描画
            vertexConsumers.draw(RenderLayer.getLines());

        } catch (Exception e) {
            OreMiner.LOGGER.error("Error rendering highlights", e);
        }
    }

    /**
     * ボックスのアウトラインを直接描画（ワールド座標を使用）
     */
    private static void drawBoxOutline(VertexConsumer vertexConsumer, Matrix4f matrix, BlockPos pos) {
        // ワールド座標でのブロックの境界
        float x1 = pos.getX() - 0.002f;
        float y1 = pos.getY() - 0.002f;
        float z1 = pos.getZ() - 0.002f;
        float x2 = pos.getX() + 1.002f;
        float y2 = pos.getY() + 1.002f;
        float z2 = pos.getZ() + 1.002f;

        // 底面の4辺
        drawLine(vertexConsumer, matrix, x1, y1, z1, x2, y1, z1);
        drawLine(vertexConsumer, matrix, x2, y1, z1, x2, y1, z2);
        drawLine(vertexConsumer, matrix, x2, y1, z2, x1, y1, z2);
        drawLine(vertexConsumer, matrix, x1, y1, z2, x1, y1, z1);

        // 上面の4辺
        drawLine(vertexConsumer, matrix, x1, y2, z1, x2, y2, z1);
        drawLine(vertexConsumer, matrix, x2, y2, z1, x2, y2, z2);
        drawLine(vertexConsumer, matrix, x2, y2, z2, x1, y2, z2);
        drawLine(vertexConsumer, matrix, x1, y2, z2, x1, y2, z1);

        // 縦の4辺
        drawLine(vertexConsumer, matrix, x1, y1, z1, x1, y2, z1);
        drawLine(vertexConsumer, matrix, x2, y1, z1, x2, y2, z1);
        drawLine(vertexConsumer, matrix, x2, y1, z2, x2, y2, z2);
        drawLine(vertexConsumer, matrix, x1, y1, z2, x1, y2, z2);
    }

    /**
     * 2点間にラインを描画
     */
    private static void drawLine(VertexConsumer vertexConsumer, Matrix4f matrix,
                                 float x1, float y1, float z1, float x2, float y2, float z2) {
        // 法線ベクトルを計算
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        float nx = length > 0 ? dx / length : 0;
        float ny = length > 0 ? dy / length : 0;
        float nz = length > 0 ? dz / length : 0;

        // 始点
        vertexConsumer.vertex(matrix, x1, y1, z1)
                .color(RED, GREEN, BLUE, ALPHA)
                .normal(nx, ny, nz);

        // 終点
        vertexConsumer.vertex(matrix, x2, y2, z2)
                .color(RED, GREEN, BLUE, ALPHA)
                .normal(nx, ny, nz);
    }
}