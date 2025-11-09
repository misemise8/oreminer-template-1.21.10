package net.misemise.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.misemise.OreMiner;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.HashSet;
import java.util.Set;

public class BlockHighlightRenderer {
    private static final Set<BlockPos> highlightedBlocks = new HashSet<>();

    private static final float RED = 0.0f;
    private static final float GREEN = 0.8f;
    private static final float BLUE = 1.0f;
    private static final float ALPHA = 0.5f;

    public static void register() {
        OreMiner.LOGGER.info("BlockHighlightRenderer initialized (using Mixin)");
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

    public static void render(Camera camera) {
        synchronized (highlightedBlocks) {
            if (highlightedBlocks.isEmpty()) {
                return;
            }
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.world == null) {
            return;
        }

        try {
            MatrixStack matrices = new MatrixStack();
            Vec3d cameraPos = camera.getPos();

            matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

            // OpenGL状態を設定
            RenderSystem.enableBlend();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableDepthTest();
            RenderSystem.disableCull();
            GL11.glLineWidth(2.0f);

            // シェーダーを設定
            RenderSystem.setShader(GameRenderer::getPositionColorProgram);

            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.begin(
                    VertexFormat.DrawMode.DEBUG_LINES,
                    VertexFormats.POSITION_COLOR
            );

            Matrix4f posMatrix = matrices.peek().getPositionMatrix();

            synchronized (highlightedBlocks) {
                for (BlockPos pos : highlightedBlocks) {
                    drawBox(buffer, posMatrix, pos);
                }
            }

            BufferedRenderer.drawWithGlobalProgram(buffer.end());

            // 元に戻す
            RenderSystem.enableDepthTest();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            GL11.glLineWidth(1.0f);

        } catch (Exception e) {
            OreMiner.LOGGER.error("Error rendering highlights", e);
        }
    }

    private static void drawBox(BufferBuilder buffer, Matrix4f matrix, BlockPos pos) {
        float x = pos.getX();
        float y = pos.getY();
        float z = pos.getZ();

        float x1 = x - 0.002f;
        float y1 = y - 0.002f;
        float z1 = z - 0.002f;
        float x2 = x + 1.002f;
        float y2 = y + 1.002f;
        float z2 = z + 1.002f;

        // 底面
        line(buffer, matrix, x1, y1, z1, x2, y1, z1);
        line(buffer, matrix, x2, y1, z1, x2, y1, z2);
        line(buffer, matrix, x2, y1, z2, x1, y1, z2);
        line(buffer, matrix, x1, y1, z2, x1, y1, z1);

        // 上面
        line(buffer, matrix, x1, y2, z1, x2, y2, z1);
        line(buffer, matrix, x2, y2, z1, x2, y2, z2);
        line(buffer, matrix, x2, y2, z2, x1, y2, z2);
        line(buffer, matrix, x1, y2, z2, x1, y2, z1);

        // 縦
        line(buffer, matrix, x1, y1, z1, x1, y2, z1);
        line(buffer, matrix, x2, y1, z1, x2, y2, z1);
        line(buffer, matrix, x2, y1, z2, x2, y2, z2);
        line(buffer, matrix, x1, y1, z2, x1, y2, z2);
    }

    private static void line(BufferBuilder buffer, Matrix4f matrix,
                             float x1, float y1, float z1, float x2, float y2, float z2) {
        buffer.vertex(matrix, x1, y1, z1).color(RED, GREEN, BLUE, ALPHA);
        buffer.vertex(matrix, x2, y2, z2).color(RED, GREEN, BLUE, ALPHA);
    }
}