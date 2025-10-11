package com.aftertime.aftertimefault.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/**
 * Render utilities ported to Fabric 1.21.8 (Yarn mappings).
 * These helpers avoid direct GL state changes and use RenderLayer/VertexConsumer flows.
 */
public final class RenderUtils {
    private static final MinecraftClient MC = MinecraftClient.getInstance();
    private static final Identifier BEACON_BEAM = Identifier.of("minecraft", "textures/entity/beacon_beam.png");

    private RenderUtils() {}

    // =============================
    // Box / ESP helpers
    // =============================

    /**
     * Draws an outlined box (ESP style) in world-space.
     */
    public static void drawEspBox(Box box,
                                  float red, float green, float blue, float alpha,
                                  float lineWidth,
                                  boolean depthTest) {
        if (MC.world == null || MC.player == null) return;

        Camera camera = MC.gameRenderer.getCamera();
        Vec3d camPos = camera.getPos();

        MatrixStack matrices = new MatrixStack();
        matrices.translate(-camPos.x, -camPos.y, -camPos.z);

        RenderSystem.lineWidth(lineWidth);

        VertexConsumerProvider.Immediate immediate = MC.getBufferBuilders().getEntityVertexConsumers();
        // Fallback to standard line layer; depth toggle not available in this mapping
        RenderLayer layer = RenderLayer.getLines();
        VertexConsumer lines = immediate.getBuffer(layer);
        emitBoxLines(matrices.peek().getPositionMatrix(), lines, box, red, green, blue, alpha);
        immediate.draw();
    }

    /**
     * Draws a world-space box outline (filled currently not supported in this mapping-safe helper).
     */
    public static void drawBox(Box box,
                               float r, float g, float b, float a,
                               boolean depthTest,
                               float lineWidth,
                               boolean filled,
                               boolean outline) {
        if (!outline) return;
        drawEspBox(box, r, g, b, a, lineWidth, depthTest);
    }

    /**
     * Convenience for drawing an ESP box around an entity.
     */
    public static void drawEntityBox(Entity entity,
                                     float red, float green, float blue, float alpha,
                                     float lineWidth,
                                     boolean depthTest,
                                     float partialTicks) {
        if (entity == null) return;
        drawEspBox(entity.getBoundingBox().expand(0.05, 0.15, 0.05), red, green, blue, alpha, lineWidth, depthTest);
    }

    public static void drawEntityEspBox(double x, double y, double z,
                                        double width, double height,
                                        float red, float green, float blue, float alpha,
                                        float lineWidth,
                                        boolean depthTest) {
        double hw = width / 2.0;
        Box box = new Box(x - hw, y, z - hw, x + hw, y + height, z + hw);
        drawEspBox(box, red, green, blue, alpha, lineWidth, depthTest);
    }

    // =============================
    // Beacon beam (deferred simple stub)
    // =============================

    public static void renderBeaconBeam(Vec3d position, Color color, boolean depthCheck, float height, float partialTicks) {
        // Mapping changes in 1.21.8 make low-level beam rendering brittle.
        // This stub keeps API compatibility; implement via a proper WorldRender event with RenderLayer-managed quads if needed.
    }

    // =============================
    // Block hitbox (outline)
    // =============================

    public static void renderBlockHitbox(BlockPos pos,
                                         float r, float g, float b, float a,
                                         boolean depthTest,
                                         float lineWidth,
                                         boolean filled) {
        World world = MC.world;
        if (world == null || pos == null) return;

        VoxelShape shape = world.getBlockState(pos).getOutlineShape(world, pos);
        if (shape.isEmpty()) return;

        Box bb = shape.getBoundingBox().offset(pos);
        drawBox(bb, r, g, b, a, depthTest, lineWidth, false, true);
    }

    // =============================
    // Floating text (billboarded)
    // =============================

    public static void renderFloatingText(String text, double x, double y, double z, float scale, int color, boolean depthTest) {
        if (text == null || text.isEmpty() || MC.world == null) return;
        EntityRenderDispatcher dispatcher = MC.getEntityRenderDispatcher();
        TextRenderer tr = MC.textRenderer;
        Camera camera = MC.gameRenderer.getCamera();
        Vec3d cam = camera.getPos();

        MatrixStack matrices = new MatrixStack();
        matrices.translate(x - cam.x, y - cam.y, z - cam.z);
        Quaternionf rot = dispatcher.getRotation();
        matrices.multiply(rot);
        matrices.scale(-0.025f * scale, -0.025f * scale, 0.025f * scale);

        float half = tr.getWidth(text) / 2f;
        VertexConsumerProvider.Immediate immediate = MC.getBufferBuilders().getEntityVertexConsumers();
        TextRenderer.TextLayerType layer = depthTest ? TextRenderer.TextLayerType.NORMAL : TextRenderer.TextLayerType.SEE_THROUGH;
        Matrix4f mat = matrices.peek().getPositionMatrix();

        tr.draw(Text.of(text), -half, 0, color, false, mat, immediate, layer, 0, 0x00F000F0);
        immediate.draw();
    }

    public static void renderFloatingTextConstant(String text, double x, double y, double z, float pixelScale, int color, boolean depthTest) {
        if (text == null || text.isEmpty() || MC.world == null) return;
        Camera camera = MC.gameRenderer.getCamera();
        Vec3d cam = camera.getPos();
        float dist = (float) Math.sqrt(cam.squaredDistanceTo(x, y, z));
        float s = pixelScale * Math.max(dist, 1.0f);
        renderFloatingText(text, x, y, z, s, color, depthTest);
    }

    // =============================
    // Interpolation helpers (safe fallbacks)
    // =============================

    public static Vec3d getInterpolatedPosition(Entity entity, float partialTicks) {
        return entity.getPos();
    }

    public static Box getInterpolatedBoundingBox(Entity entity, float partialTicks) {
        return entity.getBoundingBox();
    }

    // =============================
    // Internals
    // =============================

    private static void emitBoxLines(Matrix4f mat, VertexConsumer consumer, Box b, float r, float g, float bl, float a) {
        float x1 = (float) b.minX, y1 = (float) b.minY, z1 = (float) b.minZ;
        float x2 = (float) b.maxX, y2 = (float) b.maxY, z2 = (float) b.maxZ;
        // Bottom rectangle
        line(consumer, mat, x1, y1, z1, x2, y1, z1, r, g, bl, a);
        line(consumer, mat, x2, y1, z1, x2, y1, z2, r, g, bl, a);
        line(consumer, mat, x2, y1, z2, x1, y1, z2, r, g, bl, a);
        line(consumer, mat, x1, y1, z2, x1, y1, z1, r, g, bl, a);
        // Top rectangle
        line(consumer, mat, x1, y2, z1, x2, y2, z1, r, g, bl, a);
        line(consumer, mat, x2, y2, z1, x2, y2, z2, r, g, bl, a);
        line(consumer, mat, x2, y2, z2, x1, y2, z2, r, g, bl, a);
        line(consumer, mat, x1, y2, z2, x1, y2, z1, r, g, bl, a);
        // Vertical edges
        line(consumer, mat, x1, y1, z1, x1, y2, z1, r, g, bl, a);
        line(consumer, mat, x2, y1, z1, x2, y2, z1, r, g, bl, a);
        line(consumer, mat, x2, y1, z2, x2, y2, z2, r, g, bl, a);
        line(consumer, mat, x1, y1, z2, x1, y2, z2, r, g, bl, a);
    }

    private static void line(VertexConsumer vc, Matrix4f mat, float x1, float y1, float z1, float x2, float y2, float z2, float r, float g, float b, float a) {
        // Set color then emit vertex; this mapping doesn't require explicit next/end
        vc.color(r, g, b, a);
        vc.vertex(mat, x1, y1, z1);
        vc.color(r, g, b, a);
        vc.vertex(mat, x2, y2, z2);
    }

    // Simple Color holder (RGBA 0..255)
    public static class Color {
        public final int r, g, b, a;
        public Color(int r, int g, int b, int a) {
            this.r = r; this.g = g; this.b = b; this.a = a;
        }
    }
}
