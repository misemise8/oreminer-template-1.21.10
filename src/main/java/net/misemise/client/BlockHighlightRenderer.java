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
            }
        }
    }

    public static void clearHighlights() {
        synchronized (highlightedBlocks) {
            highlightedBlocks.clear();
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

            VertexConsumer vc = immediate.getBuffer(RenderLayer.getLines());
            int ri = (int)(RED * 255), gi = (int)(GREEN * 255), bi = (int)(BLUE * 255), ai = (int)(ALPHA * 255);

            Matrix4f mat = new Matrix4f(positionMatrix);
            mat.translate(-(float)camera.getPos().x, -(float)camera.getPos().y, -(float)camera.getPos().z);

            Set<Edge> edges = new HashSet<>();

            for (BlockPos p : blocksCopy) {
                float x0 = p.getX(), y0 = p.getY(), z0 = p.getZ();
                float x1 = x0 + 1, y1 = y0 + 1, z1 = z0 + 1;

                // 6方向チェック
                boolean down = blocksCopy.contains(p.down());
                boolean up = blocksCopy.contains(p.up());
                boolean north = blocksCopy.contains(p.north());
                boolean south = blocksCopy.contains(p.south());
                boolean west = blocksCopy.contains(p.west());
                boolean east = blocksCopy.contains(p.east());

                // 底面
                if (!down) {
                    if (!west && !north) edges.add(new Edge(x0,y0,z0, x1,y0,z0));
                    if (!east && !north) edges.add(new Edge(x1,y0,z0, x1,y0,z1));
                    if (!east && !south) edges.add(new Edge(x1,y0,z1, x0,y0,z1));
                    if (!west && !south) edges.add(new Edge(x0,y0,z1, x0,y0,z0));
                }
                // 上面
                if (!up) {
                    if (!west && !north) edges.add(new Edge(x0,y1,z0, x1,y1,z0));
                    if (!east && !north) edges.add(new Edge(x1,y1,z0, x1,y1,z1));
                    if (!east && !south) edges.add(new Edge(x1,y1,z1, x0,y1,z1));
                    if (!west && !south) edges.add(new Edge(x0,y1,z1, x0,y1,z0));
                }
                // 北面
                if (!north) {
                    if (!west && !down) edges.add(new Edge(x0,y0,z0, x1,y0,z0));
                    if (!east && !down) edges.add(new Edge(x1,y0,z0, x1,y1,z0));
                    if (!east && !up) edges.add(new Edge(x1,y1,z0, x0,y1,z0));
                    if (!west && !up) edges.add(new Edge(x0,y1,z0, x0,y0,z0));
                }
                // 南面
                if (!south) {
                    if (!west && !down) edges.add(new Edge(x0,y0,z1, x1,y0,z1));
                    if (!east && !down) edges.add(new Edge(x1,y0,z1, x1,y1,z1));
                    if (!east && !up) edges.add(new Edge(x1,y1,z1, x0,y1,z1));
                    if (!west && !up) edges.add(new Edge(x0,y1,z1, x0,y0,z1));
                }
                // 西面
                if (!west) {
                    if (!down && !north) edges.add(new Edge(x0,y0,z0, x0,y1,z0));
                    if (!up && !north) edges.add(new Edge(x0,y1,z0, x0,y1,z1));
                    if (!up && !south) edges.add(new Edge(x0,y1,z1, x0,y0,z1));
                    if (!down && !south) edges.add(new Edge(x0,y0,z1, x0,y0,z0));
                }
                // 東面
                if (!east) {
                    if (!down && !north) edges.add(new Edge(x1,y0,z0, x1,y1,z0));
                    if (!up && !north) edges.add(new Edge(x1,y1,z0, x1,y1,z1));
                    if (!up && !south) edges.add(new Edge(x1,y1,z1, x1,y0,z1));
                    if (!down && !south) edges.add(new Edge(x1,y0,z1, x1,y0,z0));
                }
            }

            for (Edge e : edges) {
                vc.vertex(mat, e.x1, e.y1, e.z1).color(ri,gi,bi,ai).texture(0f,0f).overlay(0).light(0xF000F0).normal(0f,1f,0f);
                vc.vertex(mat, e.x2, e.y2, e.z2).color(ri,gi,bi,ai).texture(0f,0f).overlay(0).light(0xF000F0).normal(0f,1f,0f);
            }

            immediate.draw(RenderLayer.getLines());
            GL11.glDepthMask(true);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
        } catch (Throwable t) {
            OreMiner.LOGGER.error("Error rendering highlights", t);
            try { GL11.glDepthMask(true); GL11.glEnable(GL11.GL_DEPTH_TEST); } catch (Throwable ignored) {}
        }
    }

    private static class Edge {
        final float x1, y1, z1, x2, y2, z2;
        private final int hash;

        Edge(float x1, float y1, float z1, float x2, float y2, float z2) {
            if (cmp(x1,y1,z1, x2,y2,z2) <= 0) {
                this.x1=x1; this.y1=y1; this.z1=z1; this.x2=x2; this.y2=y2; this.z2=z2;
            } else {
                this.x1=x2; this.y1=y2; this.z1=z2; this.x2=x1; this.y2=y1; this.z2=z1;
            }
            hash = Objects.hash(this.x1, this.y1, this.z1, this.x2, this.y2, this.z2);
        }

        int cmp(float ax,float ay,float az, float bx,float by,float bz) {
            int c = Float.compare(ax,bx); if(c!=0)return c;
            c = Float.compare(ay,by); if(c!=0)return c;
            return Float.compare(az,bz);
        }

        public int hashCode() { return hash; }
        public boolean equals(Object o) {
            if(!(o instanceof Edge))return false;
            Edge e=(Edge)o;
            return x1==e.x1 && y1==e.y1 && z1==e.z1 && x2==e.x2 && y2==e.y2 && z2==e.z2;
        }
    }
}