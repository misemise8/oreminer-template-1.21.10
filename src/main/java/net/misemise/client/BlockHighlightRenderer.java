package net.misemise.client;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.BlockPos;
import net.misemise.OreMiner;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class BlockHighlightRenderer {
    private static final Set<BlockPos> highlightedBlocks = new HashSet<>();

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

    public static void render(VertexConsumerProvider.Immediate immediate, Camera camera, Matrix4f positionMatrix) {
        Set<BlockPos> blocksCopy;
        synchronized (highlightedBlocks) {
            if (highlightedBlocks.isEmpty()) return;
            blocksCopy = new HashSet<>(highlightedBlocks);
        }

        try {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);

            VertexConsumer vertexConsumer = immediate.getBuffer(RenderLayer.getLines());

            int ri = (int) (RED * 255.0f);
            int gi = (int) (GREEN * 255.0f);
            int bi = (int) (BLUE * 255.0f);
            int ai = (int) (ALPHA * 255.0f);

            float camX = (float) camera.getPos().x;
            float camY = (float) camera.getPos().y;
            float camZ = (float) camera.getPos().z;

            Matrix4f viewMatrix = new Matrix4f(positionMatrix);
            viewMatrix.translate(-camX, -camY, -camZ);

            // エッジを収集（重複を排除）
            Set<Edge> edges = new HashSet<>();

            for (BlockPos pos : blocksCopy) {
                float x0 = pos.getX();
                float y0 = pos.getY();
                float z0 = pos.getZ();
                float x1 = pos.getX() + 1.0f;
                float y1 = pos.getY() + 1.0f;
                float z1 = pos.getZ() + 1.0f;

                // 各エッジについて、外周エッジかチェック
                // 12本のエッジ（ブロックの各辺）

                // 底面の4本のエッジ
                checkAndAddEdge(edges, blocksCopy, pos, x0, y0, z0, x1, y0, z0, 0, -1, 0);
                checkAndAddEdge(edges, blocksCopy, pos, x1, y0, z0, x1, y0, z1, 1, -1, 0);
                checkAndAddEdge(edges, blocksCopy, pos, x1, y0, z1, x0, y0, z1, 0, -1, 1);
                checkAndAddEdge(edges, blocksCopy, pos, x0, y0, z1, x0, y0, z0, -1, -1, 0);

                // 上面の4本のエッジ
                checkAndAddEdge(edges, blocksCopy, pos, x0, y1, z0, x1, y1, z0, 0, 1, 0);
                checkAndAddEdge(edges, blocksCopy, pos, x1, y1, z0, x1, y1, z1, 1, 1, 0);
                checkAndAddEdge(edges, blocksCopy, pos, x1, y1, z1, x0, y1, z1, 0, 1, 1);
                checkAndAddEdge(edges, blocksCopy, pos, x0, y1, z1, x0, y1, z0, -1, 1, 0);

                // 縦の4本のエッジ
                checkAndAddEdge(edges, blocksCopy, pos, x0, y0, z0, x0, y1, z0, -1, 0, 0);
                checkAndAddEdge(edges, blocksCopy, pos, x1, y0, z0, x1, y1, z0, 1, 0, 0);
                checkAndAddEdge(edges, blocksCopy, pos, x1, y0, z1, x1, y1, z1, 1, 0, 1);
                checkAndAddEdge(edges, blocksCopy, pos, x0, y0, z1, x0, y1, z1, -1, 0, 1);
            }

            // エッジを描画
            for (Edge e : edges) {
                drawEdge(vertexConsumer, viewMatrix, e.x1, e.y1, e.z1, e.x2, e.y2, e.z2, ri, gi, bi, ai);
            }

            immediate.draw(RenderLayer.getLines());

            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

        } catch (Throwable t) {
            OreMiner.LOGGER.error("Error rendering highlights", t);
            try {
                GL11.glDepthMask(true);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            } catch (Throwable ignored) {}
        }
    }

    /**
     * エッジが外周にあるかチェックして追加
     */
    private static void checkAndAddEdge(Set<Edge> edges, Set<BlockPos> blocks, BlockPos pos,
                                        float x1, float y1, float z1, float x2, float y2, float z2,
                                        int dx, int dy, int dz) {
        // このエッジに隣接する可能性のあるブロックをチェック
        boolean isOutline = !blocks.contains(pos.add(dx, dy, dz));

        if (isOutline) {
            edges.add(new Edge(x1, y1, z1, x2, y2, z2));
        }
    }

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

    /**
     * エッジクラス：2つの頂点を持つ線分
     */
    private static class Edge {
        final float x1, y1, z1, x2, y2, z2;
        private final int hash;

        Edge(float x1, float y1, float z1, float x2, float y2, float z2) {
            // 座標を正規化（小さい方を先に）
            if (compare(x1, y1, z1, x2, y2, z2) <= 0) {
                this.x1 = x1; this.y1 = y1; this.z1 = z1;
                this.x2 = x2; this.y2 = y2; this.z2 = z2;
            } else {
                this.x1 = x2; this.y1 = y2; this.z1 = z2;
                this.x2 = x1; this.y2 = y1; this.z2 = z1;
            }
            this.hash = computeHash();
        }

        private int compare(float ax, float ay, float az, float bx, float by, float bz) {
            int cmp = Float.compare(ax, bx);
            if (cmp != 0) return cmp;
            cmp = Float.compare(ay, by);
            if (cmp != 0) return cmp;
            return Float.compare(az, bz);
        }

        private int computeHash() {
            int result = Float.floatToIntBits(x1);
            result = 31 * result + Float.floatToIntBits(y1);
            result = 31 * result + Float.floatToIntBits(z1);
            result = 31 * result + Float.floatToIntBits(x2);
            result = 31 * result + Float.floatToIntBits(y2);
            result = 31 * result + Float.floatToIntBits(z2);
            return result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Edge)) return false;
            Edge other = (Edge) obj;
            return Float.compare(x1, other.x1) == 0 &&
                    Float.compare(y1, other.y1) == 0 &&
                    Float.compare(z1, other.z1) == 0 &&
                    Float.compare(x2, other.x2) == 0 &&
                    Float.compare(y2, other.y2) == 0 &&
                    Float.compare(z2, other.z2) == 0;
        }
    }
}