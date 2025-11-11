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
import java.util.Iterator;

/**
 * BlockHighlightRenderer - クラスタの外周だけを描く版
 * - 内部エッジは相殺して描かない（外側だけ残る）
 * - GL11 で深度テストを無効化して「貫通表示」を実現
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

            // ===== collect edges for exposed faces, cancel duplicates =====
            // Edge set（重複は相殺する）
            Set<Edge> edges = new HashSet<>();

            // small outset to avoid z-fighting; set to 0f if you want exact block bounds
            final float OUTSET = 0.0001f;

            for (BlockPos pos : blocksCopy) {
                // check 6 neighbors: +X, -X, +Y, -Y, +Z, -Z
                boolean nx = blocksCopy.contains(pos.add(1, 0, 0));
                boolean px = blocksCopy.contains(pos.add(-1, 0, 0));
                boolean ny = blocksCopy.contains(pos.add(0, 1, 0));
                boolean py = blocksCopy.contains(pos.add(0, -1, 0));
                boolean nz = blocksCopy.contains(pos.add(0, 0, 1));
                boolean pz = blocksCopy.contains(pos.add(0, 0, -1));

                // compute block bounds with optional outset
                float x0 = pos.getX() - OUTSET;
                float y0 = pos.getY() - OUTSET;
                float z0 = pos.getZ() - OUTSET;
                float x1 = pos.getX() + 1.0f + OUTSET;
                float y1 = pos.getY() + 1.0f + OUTSET;
                float z1 = pos.getZ() + 1.0f + OUTSET;

                // For each exposed face, add its 4 edges (edges are normalized in Edge class)
                // -X face (left)
                if (!nx) {
                    addOrCancelEdge(edges, new Edge(x0, y0, z0, x0, y1, z0));
                    addOrCancelEdge(edges, new Edge(x0, y1, z0, x0, y1, z1));
                    addOrCancelEdge(edges, new Edge(x0, y1, z1, x0, y0, z1));
                    addOrCancelEdge(edges, new Edge(x0, y0, z1, x0, y0, z0));
                }
                // +X face (right)
                if (!px) {
                    addOrCancelEdge(edges, new Edge(x1, y0, z0, x1, y1, z0));
                    addOrCancelEdge(edges, new Edge(x1, y1, z0, x1, y1, z1));
                    addOrCancelEdge(edges, new Edge(x1, y1, z1, x1, y0, z1));
                    addOrCancelEdge(edges, new Edge(x1, y0, z1, x1, y0, z0));
                }
                // -Y face (bottom)
                if (!ny) {
                    addOrCancelEdge(edges, new Edge(x0, y0, z0, x1, y0, z0));
                    addOrCancelEdge(edges, new Edge(x1, y0, z0, x1, y0, z1));
                    addOrCancelEdge(edges, new Edge(x1, y0, z1, x0, y0, z1));
                    addOrCancelEdge(edges, new Edge(x0, y0, z1, x0, y0, z0));
                }
                // +Y face (top)
                if (!py) {
                    addOrCancelEdge(edges, new Edge(x0, y1, z0, x1, y1, z0));
                    addOrCancelEdge(edges, new Edge(x1, y1, z0, x1, y1, z1));
                    addOrCancelEdge(edges, new Edge(x1, y1, z1, x0, y1, z1));
                    addOrCancelEdge(edges, new Edge(x0, y1, z1, x0, y1, z0));
                }
                // -Z face (back)
                if (!nz) {
                    addOrCancelEdge(edges, new Edge(x0, y0, z0, x1, y0, z0));
                    addOrCancelEdge(edges, new Edge(x1, y0, z0, x1, y1, z0));
                    addOrCancelEdge(edges, new Edge(x1, y1, z0, x0, y1, z0));
                    addOrCancelEdge(edges, new Edge(x0, y1, z0, x0, y0, z0));
                }
                // +Z face (front)
                if (!pz) {
                    addOrCancelEdge(edges, new Edge(x0, y0, z1, x1, y0, z1));
                    addOrCancelEdge(edges, new Edge(x1, y0, z1, x1, y1, z1));
                    addOrCancelEdge(edges, new Edge(x1, y1, z1, x0, y1, z1));
                    addOrCancelEdge(edges, new Edge(x0, y1, z1, x0, y0, z1));
                }
            }

            // ===== draw remaining edges =====
            // Disable depth test and depth writes so lines show through blocks
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);

            Iterator<Edge> it = edges.iterator();
            while (it.hasNext()) {
                Edge e = it.next();
                drawEdge(vertexConsumer, viewMatrix, e.ax, e.ay, e.az, e.bx, e.by, e.bz, ri, gi, bi, ai);
            }

            // flush
            immediate.draw(RenderLayer.getLines());

            // restore depth
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_DEPTH_TEST);

        } catch (Throwable t) {
            OreMiner.LOGGER.error("Error rendering highlights", t);
            try { GL11.glDepthMask(true); GL11.glEnable(GL11.GL_DEPTH_TEST); } catch (Throwable ignored) {}
        }
    }

    // add edge to set; if already present, remove it (cancel)
    private static void addOrCancelEdge(Set<Edge> set, Edge e) {
        if (!set.add(e)) {
            // already present -> remove (cancel internal edge)
            set.remove(e);
        }
    }

    // draw one edge (two vertices)
    private static void drawEdge(VertexConsumer vc, Matrix4f mat,
                                 float ax, float ay, float az, float bx, float by, float bz,
                                 int ri, int gi, int bi, int ai) {
        vc.vertex(mat, ax, ay, az).color(ri, gi, bi, ai).texture(0f, 0f).overlay(0).light(0xF000F0).normal(0f, 1f, 0f);
        vc.vertex(mat, bx, by, bz).color(ri, gi, bi, ai).texture(0f, 0f).overlay(0).light(0xF000F0).normal(0f, 1f, 0f);
    }

    // Edge class: endpoints canonicalized (order-independent) and hashed by scaled ints to avoid float-hash issues
    private static class Edge {
        final int axi, ayi, azi, bxi, byi, bzi;
        // also keep floats for drawing convenience
        final float ax, ay, az, bx, by, bz;

        // scale factor for converting float coords to ints for hashing
        private static final float SCALE = 10000f;

        Edge(float ax, float ay, float az, float bx, float by, float bz) {
            // canonical order: compare (ax,ay,az) vs (bx,by,bz) lexicographically
            long aKey = keyInt(ax, ay, az);
            long bKey = keyInt(bx, by, bz);
            if (aKey <= bKey) {
                this.ax = ax; this.ay = ay; this.az = az;
                this.bx = bx; this.by = by; this.bz = bz;
                this.axi = (int) Math.round(ax * SCALE);
                this.ayi = (int) Math.round(ay * SCALE);
                this.azi = (int) Math.round(az * SCALE);
                this.bxi = (int) Math.round(bx * SCALE);
                this.byi = (int) Math.round(by * SCALE);
                this.bzi = (int) Math.round(bz * SCALE);
            } else {
                // swap
                this.ax = bx; this.ay = by; this.az = bz;
                this.bx = ax; this.by = ay; this.bz = az;
                this.axi = (int) Math.round(bx * SCALE);
                this.ayi = (int) Math.round(by * SCALE);
                this.azi = (int) Math.round(bz * SCALE);
                this.bxi = (int) Math.round(ax * SCALE);
                this.byi = (int) Math.round(ay * SCALE);
                this.bzi = (int) Math.round(az * SCALE);
            }
        }

        private static long keyInt(float x, float y, float z) {
            int xi = Math.round(x * 10000f);
            int yi = Math.round(y * 10000f);
            int zi = Math.round(z * 10000f);
            // combine into long
            return (((long) xi) & 0xffffffffL) << 32 | (((long) yi) & 0xffffL) << 16 | (((long) zi) & 0xffffL);
        }

        @Override
        public int hashCode() {
            int h = 31;
            h = h * 31 + axi;
            h = h * 31 + ayi;
            h = h * 31 + azi;
            h = h * 31 + bxi;
            h = h * 31 + byi;
            h = h * 31 + bzi;
            return h;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Edge)) return false;
            Edge e = (Edge) o;
            return this.axi == e.axi && this.ayi == e.ayi && this.azi == e.azi
                    && this.bxi == e.bxi && this.byi == e.byi && this.bzi == e.bzi;
        }
    }
}
