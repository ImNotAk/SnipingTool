package com.example.signsearcher.v1_8_9.mixins;

import com.example.signsearcher.SignSearcherAddon;
import com.example.signsearcher.v1_8_9.SignSearchRenderState;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockWallSign;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderGlobal.class)
public class MixinRenderGlobal {

  private static final float OUTLINE_ALPHA = 0.85F;
  private static final float OUTLINE_WIDTH = 1.6F;

  private static final double WALL_XZ_MARGIN = 0.008D;
  private static final double WALL_Y_OFFSET = 0.28D;
  private static final double WALL_HEIGHT = 0.48D;
  private static final double WALL_DEPTH_MIN = 0.110D;
  private static final double WALL_DEPTH_MAX = 0.125D;
  private static final double WALL_DEPTH_MIN_OPPOSITE = 0.875D;
  private static final double WALL_DEPTH_MAX_OPPOSITE = 0.890D;

  @Inject(
      method = "renderEntities(Lnet/minecraft/entity/Entity;Lnet/minecraft/client/renderer/culling/ICamera;F)V",
      at = @At("TAIL")
  )
  private void renderSigns(Entity entity, ICamera camera, float partialTicks, CallbackInfo ci) {
    Minecraft mc = Minecraft.getMinecraft();
    if (mc.theWorld == null || mc.thePlayer == null) {
      return;
    }

    List<TileEntitySign> signs = SignSearchRenderState.MATCH_SIGNS;
    if (signs.isEmpty()) {
      return;
    }

    SignSearcherAddon addon = SignSearcherAddon.instance();
    if (addon == null || !addon.configuration().enabled().get()) {
      return;
    }

    double vx = mc.getRenderManager().viewerPosX;
    double vy = mc.getRenderManager().viewerPosY;
    double vz = mc.getRenderManager().viewerPosZ;

    GlStateManager.pushMatrix();
    GlStateManager.disableTexture2D();
    GlStateManager.enableBlend();
    GlStateManager.disableDepth();
    GlStateManager.disableCull();
    GlStateManager.depthMask(false);

    GL11.glDisable(GL11.GL_LIGHTING);
    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

    for (TileEntitySign sign : signs) {
      float[] c = resolveColor(addon, sign);
      double x = sign.getPos().getX() - vx;
      double y = sign.getPos().getY() - vy;
      double z = sign.getPos().getZ() - vz;

      Block block = sign.getBlockType();
      int meta = sign.getBlockMetadata();

      if (block instanceof BlockWallSign) {
        drawWallGlow(x, y, z, meta, c);
      } else if (block instanceof BlockStandingSign) {
        drawStandingGlow(x, y, z, meta, c);
      }
    }

    GL11.glEnable(GL11.GL_LIGHTING);
    GlStateManager.depthMask(true);
    GlStateManager.enableDepth();
    GlStateManager.enableCull();
    GlStateManager.enableTexture2D();
    GlStateManager.popMatrix();

    if (!addon.configuration().showText().get()) {
      return;
    }

    for (TileEntitySign sign : signs) {
      float[] c = resolveColor(addon, sign);
      drawText(sign, vx, vy, vz, addon.configuration().textScale().get(), toARGB(c));
    }
  }

  private static float[] resolveColor(SignSearcherAddon addon, TileEntitySign sign) {
    String t = getText(sign);

    if (contains(t, addon.configuration().query1().get())) {
      return rgb(
          addon.configuration().red1().get(),
          addon.configuration().green1().get(),
          addon.configuration().blue1().get()
      );
    }

    if (contains(t, addon.configuration().query2().get())) {
      return rgb(
          addon.configuration().red2().get(),
          addon.configuration().green2().get(),
          addon.configuration().blue2().get()
      );
    }

    if (contains(t, addon.configuration().query3().get())) {
      return rgb(
          addon.configuration().red3().get(),
          addon.configuration().green3().get(),
          addon.configuration().blue3().get()
      );
    }

    if (contains(t, addon.configuration().query4().get())) {
      return rgb(
          addon.configuration().red4().get(),
          addon.configuration().green4().get(),
          addon.configuration().blue4().get()
      );
    }

    return new float[]{0F, 1F, 0F};
  }

  private static float[] rgb(int r, int g, int b) {
    return new float[]{r / 255F, g / 255F, b / 255F};
  }

  private static int toARGB(float[] c) {
    return 0xFF000000
        | ((int) (c[0] * 255) << 16)
        | ((int) (c[1] * 255) << 8)
        | (int) (c[2] * 255);
  }

  private static boolean contains(String t, String q) {
    return q != null && !q.trim().isEmpty() && t.contains(q.toLowerCase());
  }

  private static String getText(TileEntitySign s) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < 4; i++) {
      if (s.signText[i] != null) {
        sb.append(s.signText[i].getUnformattedText().toLowerCase()).append(" ");
      }
    }
    return sb.toString();
  }

  private static void drawWallGlow(double x, double y, double z, int meta, float[] c) {

    double minY = y + WALL_Y_OFFSET;
    double maxY = y + WALL_Y_OFFSET + WALL_HEIGHT;

    double minX, maxX, minZ, maxZ;

    switch (meta) {
      case 2: // NORTH (Z+)
        minX = x + WALL_XZ_MARGIN;
        maxX = x + 1.0 - WALL_XZ_MARGIN;
        minZ = z + WALL_DEPTH_MIN_OPPOSITE;
        maxZ = minZ; // 🔥 force plane
        break;

      case 3: // SOUTH (Z-)
        minX = x + WALL_XZ_MARGIN;
        maxX = x + 1.0 - WALL_XZ_MARGIN;
        minZ = z + WALL_DEPTH_MIN;
        maxZ = minZ; // 🔥 force plane
        break;

      case 4: // WEST (X+)
        minX = x + WALL_DEPTH_MIN_OPPOSITE;
        maxX = minX; // 🔥 force plane
        minZ = z + WALL_XZ_MARGIN;
        maxZ = z + 1.0 - WALL_XZ_MARGIN;
        break;

      default: // EAST (X-)
        minX = x + WALL_DEPTH_MIN;
        maxX = minX; // 🔥 force plane
        minZ = z + WALL_XZ_MARGIN;
        maxZ = z + 1.0 - WALL_XZ_MARGIN;
        break;
    }

    glow(minX, maxX, minY, maxY, minZ, maxZ, c);
    outline(minX, maxX, minY, maxY, minZ, maxZ, c);
  }

  private static void drawStandingGlow(double x, double y, double z, int meta, float[] c) {
    double cx = x + 0.5D;
    double cz = z + 0.5D;
    double y1 = y + 0.34D;
    double y2 = y + 0.78D;

    double w = 0.375D;
    double d = 0.015D;

    double yaw = -meta * 22.5D * Math.PI / 180.0D;

    double cos = Math.cos(yaw);
    double sin = Math.sin(yaw);

    double[] xs = new double[4];
    double[] zs = new double[4];

    double[][] pts = {
        {-w, -d},
        { w, -d},
        { w,  d},
        {-w,  d}
    };

    for (int i = 0; i < 4; i++) {
      xs[i] = cx + pts[i][0] * cos - pts[i][1] * sin;
      zs[i] = cz + pts[i][0] * sin + pts[i][1] * cos;
    }

    glowRot(xs, zs, y1, y2, c);
    outlineRot(xs, zs, y1, y2, c);
  }

  private static void glow(double minX, double maxX, double minY, double maxY, double minZ, double maxZ, float[] c) {
    color(c, 0.35F);
    quad(minX, maxX, minY, maxY, minZ, maxZ);

    color(c, 0.12F);
    if (Math.abs(maxX - minX) < 1.0E-6D) {
      quad(minX, maxX, minY - 0.01D, maxY + 0.01D, minZ - 0.01D, maxZ + 0.01D);
    } else if (Math.abs(maxZ - minZ) < 1.0E-6D) {
      quad(minX - 0.01D, maxX + 0.01D, minY - 0.01D, maxY + 0.01D, minZ, maxZ);
    }

    color(c, 0.05F);
    if (Math.abs(maxX - minX) < 1.0E-6D) {
      quad(minX, maxX, minY - 0.02D, maxY + 0.02D, minZ - 0.02D, maxZ + 0.02D);
    } else if (Math.abs(maxZ - minZ) < 1.0E-6D) {
      quad(minX - 0.02D, maxX + 0.02D, minY - 0.02D, maxY + 0.02D, minZ, maxZ);
    }
  }

  private static void glowRot(double[] xs, double[] zs, double y1, double y2, float[] c) {
    color(c, 0.35F);
    quadRot(xs, zs, y1, y2);

    color(c, 0.12F);
    quadRotExp(xs, zs, y1, y2, 0.01D);

    color(c, 0.05F);
    quadRotExp(xs, zs, y1, y2, 0.02D);
  }

  private static void outline(double minX, double maxX, double minY, double maxY, double minZ, double maxZ, float[] c) {
    GL11.glLineWidth(OUTLINE_WIDTH);
    color(c, OUTLINE_ALPHA);

    Tessellator t = Tessellator.getInstance();
    WorldRenderer w = t.getWorldRenderer();
    w.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);

    // X-plane (east/west)
    if (Math.abs(maxX - minX) < 1.0E-6D) {
      w.pos(minX, minY, minZ).endVertex();
      w.pos(minX, minY, maxZ).endVertex();
      w.pos(minX, maxY, maxZ).endVertex();
      w.pos(minX, maxY, minZ).endVertex();
    }

    // Z-plane (north/south)
    else if (Math.abs(maxZ - minZ) < 1.0E-6D) {
      w.pos(minX, minY, minZ).endVertex();
      w.pos(maxX, minY, minZ).endVertex();
      w.pos(maxX, maxY, minZ).endVertex();
      w.pos(minX, maxY, minZ).endVertex();
    }

    t.draw();
  }

  private static void outlineRot(double[] x, double[] z, double y1, double y2, float[] c) {
    GL11.glLineWidth(OUTLINE_WIDTH);
    color(c, OUTLINE_ALPHA);

    Tessellator t = Tessellator.getInstance();
    WorldRenderer w = t.getWorldRenderer();
    w.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION);

    for (int i = 0; i < 4; i++) {
      w.pos(x[i], y1, z[i]).endVertex();
    }

    t.draw();
  }

  private static void quad(double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
    Tessellator t = Tessellator.getInstance();
    WorldRenderer w = t.getWorldRenderer();
    w.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

    if (Math.abs(maxX - minX) < 1.0E-6D) {
      w.pos(minX, minY, minZ).endVertex();
      w.pos(minX, minY, maxZ).endVertex();
      w.pos(minX, maxY, maxZ).endVertex();
      w.pos(minX, maxY, minZ).endVertex();
    } else {
      w.pos(minX, minY, minZ).endVertex();
      w.pos(maxX, minY, minZ).endVertex();
      w.pos(maxX, maxY, minZ).endVertex();
      w.pos(minX, maxY, minZ).endVertex();
    }

    t.draw();
  }

  private static void quadRot(double[] x, double[] z, double y1, double y2) {
    Tessellator t = Tessellator.getInstance();
    WorldRenderer w = t.getWorldRenderer();
    w.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION);

    for (int i = 0; i < 4; i++) {
      int j = (i + 1) % 4;
      w.pos(x[i], y1, z[i]).endVertex();
      w.pos(x[j], y1, z[j]).endVertex();
      w.pos(x[j], y2, z[j]).endVertex();
      w.pos(x[i], y2, z[i]).endVertex();
    }

    t.draw();
  }

  private static void quadRotExp(double[] xs, double[] zs, double y1, double y2, double e) {
    double cx = 0.0D;
    double cz = 0.0D;
    for (int i = 0; i < 4; i++) {
      cx += xs[i];
      cz += zs[i];
    }
    cx /= 4.0D;
    cz /= 4.0D;

    double[] nx = new double[4];
    double[] nz = new double[4];

    for (int i = 0; i < 4; i++) {
      double dx = xs[i] - cx;
      double dz = zs[i] - cz;
      double len = Math.sqrt(dx * dx + dz * dz);
      nx[i] = xs[i] + (dx / len) * e;
      nz[i] = zs[i] + (dz / len) * e;
    }

    quadRot(nx, nz, y1, y2);
  }

  private static void color(float[] c, float a) {
    GlStateManager.color(c[0], c[1], c[2], a);
  }

  private static int brighten(int color, float factor) {
    int r = (int) (((color >> 16) & 255) * factor);
    int g = (int) (((color >> 8) & 255) * factor);
    int b = (int) ((color & 255) * factor);

    r = Math.min(255, r);
    g = Math.min(255, g);
    b = Math.min(255, b);

    return 0xFF000000 | (r << 16) | (g << 8) | b;
  }

  private static void drawText(TileEntitySign sign, double vx, double vy, double vz, int scaleSetting, int color) {
    Minecraft mc = Minecraft.getMinecraft();
    FontRenderer fr = mc.fontRendererObj;

    double x = sign.getPos().getX() - vx + 0.5D;
    double y = sign.getPos().getY() - vy + 0.9D;
    double z = sign.getPos().getZ() - vz + 0.5D;

    double dx = mc.thePlayer.posX - sign.getPos().getX();
    double dz = mc.thePlayer.posZ - sign.getPos().getZ();
    double dist = Math.sqrt(dx * dx + dz * dz);

    float scale = (scaleSetting / 1000F) + (float) (dist * 0.002F);

    GlStateManager.pushMatrix();
    GlStateManager.translate(x, y, z);
    GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0, 1, 0);
    GlStateManager.rotate(mc.getRenderManager().playerViewX, 1, 0, 0);
    GlStateManager.scale(-scale, -scale, scale);

    GlStateManager.disableDepth();

    // 🔥 brighten the color slightly
    int brightColor = brighten(color, 1.25F);

    for (int i = 0; i < 4; i++) {
      if (sign.signText[i] == null) continue;

      String s = sign.signText[i].getUnformattedText();
      int w = fr.getStringWidth(s);

      // 🔥 REAL OUTLINE (4 directions)
      fr.drawString(s, -w / 2 - 1, i * 10 - 20, 0xFF000000);
      fr.drawString(s, -w / 2 + 1, i * 10 - 20, 0xFF000000);
      fr.drawString(s, -w / 2, i * 10 - 21, 0xFF000000);
      fr.drawString(s, -w / 2, i * 10 - 19, 0xFF000000);

      // 🔥 MAIN TEXT
      fr.drawString(s, -w / 2, i * 10 - 20, brightColor);
    }

    GlStateManager.enableDepth();
    GlStateManager.popMatrix();
  }
}