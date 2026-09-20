package com.drag0nge0de.lightsabers.client.screen;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.client.ALPerf;
import com.drag0nge0de.lightsabers.client.ForceClientState;
import com.drag0nge0de.lightsabers.force.Power;
import com.drag0nge0de.lightsabers.network.ALNetwork;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix4f;

public class ForcePowersScreen extends Screen {
   private static final Identifier ICONS = AL.id("textures/gui/icons.png");

   private static final int X_SIZE = 256;
   private static final int Y_SIZE = 202;
   private static final int PANE_X = 16;
   private static final int PANE_Y = 17;
   private static final int PANE_W = 224;
   private static final int PANE_H = 155;
   private final List<Power> powers = new ArrayList<>(Power.POWERS);
   private final Random random = new Random();
   private ButtonWidget doneButton;
   private float zoom = 1.0F;
   private double camX;
   private double camY;
   private double camXS;
   private double camYS;
   private double prevMouseX;
   private double prevMouseY;
   private int dragState;
   private Power hovered;
   private double camMinX;
   private double camMinY;
   private double camMaxX;
   private double camMaxY;
   private final BlockPos holocronPos;
   private boolean sentClose;

   public ForcePowersScreen(BlockPos holocronPos) {
      super(Text.translatable("gui.forcePowers"));
      this.holocronPos = holocronPos;
      double minX = 0.0;
      double maxX = 0.0;
      double minY = 0.0;
      double maxY = 0.0;

      for (Power p : this.powers) {
         minX = Math.min(minX, (double)p.x);
         maxX = Math.max(maxX, (double)p.x);
         minY = Math.min(minY, (double)p.y);
         maxY = Math.max(maxY, (double)p.y);
      }

      this.camMinX = minX * 24.0 - 112.0;
      this.camMaxX = maxX * 24.0 - 77.0;
      this.camMinY = minY * 24.0 - 112.0;
      this.camMaxY = maxY * 24.0 - 77.0;
      this.camX = this.camXS = -101.0;
      this.camY = this.camYS = -90.0;
   }

   protected void init() {
      this.doneButton = (ButtonWidget)this.addDrawableChild(
         ButtonWidget.builder(Text.translatable("gui.done"), b -> this.close())
            .dimensions(this.width / 2 + 24, this.height / 2 + 74, 80, 20)
            .build()
      );
   }

   public void close() {
      this.client.setScreen(null);
   }

   @Override
   public void removed() {

      if (this.holocronPos != null && !this.sentClose && this.client.world != null) {
         this.sentClose = true;
         ClientPlayNetworking.send(new ALNetwork.CloseHolocronPayload(this.holocronPos));
      }
   }

   @Override
   public void tick() {

      if (this.dragState == 1 && this.hovered != null && !this.unlocked(this.hovered)
            && this.canUnlock(this.hovered)) {
         int cost = this.xpCost(this.hovered);
         boolean hasXp = ForceClientState.xp > 0 || cost == 0;
         boolean baseOk = this.hovered.stats.baseRequirement == 0
               || ForceClientState.basePower() >= this.hovered.stats.baseRequirement;
         if (hasXp && baseOk) {
            ClientPlayNetworking.send(new ALNetwork.DrainXpPayload(this.hovered.getName()));
         }
      }
   }

   public boolean shouldPause() {
      return false;
   }

   private int panelX() {
      return (this.width - 256) / 2;
   }

   private int panelY() {
      return (this.height - 202) / 2;
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
      float prev = this.zoom;
      if (vertical < 0.0) {
         this.zoom += 0.25F;
      } else if (vertical > 0.0) {
         this.zoom -= 0.25F;
      }

      this.zoom = MathHelper.clamp(this.zoom, 1.0F, 2.0F);
      if (this.zoom != prev) {
         this.camX = this.camX + (double)(prev - this.zoom) * 112.0;
         this.camY = this.camY + (double)(prev - this.zoom) * 77.5;
         this.clampCam();
      }

      return true;
   }

   private void clampCam() {
      this.camX = MathHelper.clamp(this.camX, this.camMinX, this.camMaxX);
      this.camY = MathHelper.clamp(this.camY, this.camMinY, this.camMaxY);
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
      if (button == 0 && this.dragState == 1) {
         this.camX = this.camX - (mouseX - this.prevMouseX) * (double)this.zoom;
         this.camY = this.camY - (mouseY - this.prevMouseY) * (double)this.zoom;
         this.clampCam();
         this.prevMouseX = mouseX;
         this.prevMouseY = mouseY;
         return true;
      } else {
         return super.mouseDragged(mouseX, mouseY, button, dx, dy);
      }
   }

   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int x = this.panelX();
      int y = this.panelY();
      if (button == 0 && mouseX >= (double)(x + 8) && mouseX < (double)(x + 8 + 224) && mouseY >= (double)(y + 17) && mouseY < (double)(y + 17 + 155)) {
         this.dragState = 1;
         this.prevMouseX = mouseX;
         this.prevMouseY = mouseY;

         return true;
      } else {
         return super.mouseClicked(mouseX, mouseY, button);
      }
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      this.dragState = 0;
      return super.mouseReleased(mouseX, mouseY, button);
   }

   private boolean unlocked(Power p) {
      return ForceClientState.hasPower(p.getName());
   }

   private boolean canUnlock(Power p) {
      return p.parent == null || this.unlocked(p.parent);
   }

   private int hierarchy(Power p) {
      if (this.unlocked(p)) {
         return 0;
      } else {
         int i = 0;

         for (Power a = p.parent; a != null && !this.unlocked(a); i++) {
            a = a.parent;
         }

         return i + 1;
      }
   }

   private int xpCost(Power p) {
      return p.getActualXpCost(ForceClientState.completionOf(p.side.getOpposite()));
   }

   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      ALPerf.frameBegin("ForcePowersScreen");
      ALPerf.section("gradient");
      context.fillGradient(0, 0, this.width, this.height, -1072689128, -1072689128);
      double f = Math.min(1.0, (double)delta * 10.0);
      this.camXS = this.camXS + (this.camX - this.camXS) * f;
      this.camYS = this.camYS + (this.camY - this.camYS) * f;
      int x = this.panelX();
      int y = this.panelY();
      int paneX = x + 16;
      int paneY = y + 17;
      ALPerf.section("pane-fill");
      context.fill(x + 8, y + 17 - 1, x + 8 + 224 + 8, y + 17 + 155 + 1, -16777216);

      context.draw();
      context.enableScissor(x + 8, y + 17, x + 8 + 224, y + 17 + 155);
      double viewW = 224.0 * (double)this.zoom;
      double viewH = 155.0 * (double)this.zoom;

      int c0 = MathHelper.floor(this.camXS / 16.0) - 1;
      int r0 = MathHelper.floor(this.camYS / 16.0) - 1;
      int c1 = MathHelper.floor((this.camXS + viewW) / 16.0) + 1;
      int r1 = MathHelper.floor((this.camYS + viewH) / 16.0) + 1;
      int tileSize = (int)Math.ceil(16.0 / (double)this.zoom) + 1;

      MinecraftClient mc = this.client;
      String playerId = mc.getSession().getUuidOrNull() != null
            ? mc.getSession().getUuidOrNull().toString().replace("-", "")
            : mc.getSession().getUsername();

      RenderSystem.setShaderTexture(0, SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE);
      RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
      ALPerf.section("forcestone-tiles");
      Matrix4f tileMatrix = context.getMatrices().peek().getPositionMatrix();
      BufferBuilder tiles = Tessellator.getInstance().begin(
              VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      for (int row = r0; row <= r1; row++) {

         float brightness = MathHelper.clamp(0.6F - (float)(row + 18) / 25.0F * 0.3F, 0.0F, 1.0F);
         for (int col = c0; col <= c1; col++) {
            this.random.setSeed((long) playerId.hashCode() + (col + 18) + (long)(row + 18) * 16L);
            int k3 = this.random.nextInt(Math.max(1 + (col + 18 + 10) / 6, 1)) + (col + 18 + 10) - 11;
            boolean dark = k3 < 20;
            k3 = this.random.nextInt(50);
            int variant = k3 > 32 ? (k3 > 40 ? 2 : 1) : 0;
            String base = dark ? "dark_forcestone" : "light_forcestone";
            String tex = variant == 0 ? base : base + (variant == 1 ? "_cracked" : "_mossy");
            Sprite sprite = mc.getSpriteAtlas(SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE)
                  .apply(AL.id("block/" + tex));
            int sx = (int)Math.round((double)paneX + ((double)(col * 16) - this.camXS) / (double)this.zoom);
            int sy = (int)Math.round((double)paneY + ((double)(row * 16) - this.camYS) / (double)this.zoom);
            tiles.vertex(tileMatrix, (float)sx, (float)sy, 0.0F).texture(sprite.getMinU(), sprite.getMinV()).color(brightness, brightness, brightness, 1.0F);
            tiles.vertex(tileMatrix, (float)sx, (float)(sy + tileSize), 0.0F).texture(sprite.getMinU(), sprite.getMaxV()).color(brightness, brightness, brightness, 1.0F);
            tiles.vertex(tileMatrix, (float)(sx + tileSize), (float)(sy + tileSize), 0.0F).texture(sprite.getMaxU(), sprite.getMaxV()).color(brightness, brightness, brightness, 1.0F);
            tiles.vertex(tileMatrix, (float)(sx + tileSize), (float)sy, 0.0F).texture(sprite.getMaxU(), sprite.getMinV()).color(brightness, brightness, brightness, 1.0F);
         }
      }
      BufferRenderer.drawWithGlobalProgram(tiles.end());

      ALPerf.section("lines");

      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      Matrix4f flatMatrix = context.getMatrices().peek().getPositionMatrix();
      BufferBuilder flat = Tessellator.getInstance().begin(
              VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
      for (Power p : this.powers) {
         if (p.parent != null && this.hierarchy(p) <= 4) {
            int color;
            if (this.unlocked(p)) {

               color = -6250336;
            } else if (this.canUnlock(p)) {
               color = -16711936;
            } else {
               color = -16777216;
            }

            this.drawLine(flat, flatMatrix,
               this.sx((double)(p.x * 24 + 11)),
               this.sy((double)(p.y * 24 + 11)),
               this.sx((double)(p.parent.x * 24 + 11)),
               this.sy((double)(p.parent.y * 24 + 11)),
               color
            );
         }
      }

      ALPerf.section("hover-scan");
      double wx0 = this.camXS - 24.0;
      double wy0 = this.camYS - 24.0;
      double wx1 = this.camXS + viewW + 24.0;
      double wy1 = this.camYS + viewH + 24.0;
      this.hovered = null;
      double mwx = (double)((float)(mouseX - paneX) * this.zoom) + this.camXS;
      double mwy = (double)((float)(mouseY - paneY) * this.zoom) + this.camYS;
      int nodeSize = (int)Math.ceil(16.0 / (double)this.zoom);
      int frameSize = (int)Math.round(26.0 / (double)this.zoom);

      ALPerf.section("node-frames");
      java.util.List<Power> visible = new ArrayList<>();
      java.util.Map<Power, int[]> framePos = new java.util.HashMap<>();
      for (Power px : this.powers) {
         double ox = (double)(px.x * 24);
         double oy = (double)(px.y * 24);
         if (!(ox < wx0) && !(oy < wy0) && !(ox > wx1) && !(oy > wy1)) {
            int hier = this.hierarchy(px);
            if (this.unlocked(px)) {
            } else if (this.canUnlock(px)) {
            } else if (hier < 3) {
            } else if (hier == 3) {
            } else if (hier == 4) {

            } else {
               continue;
            }

            int sx = (int)Math.round(this.sx(ox));
            int sy = (int)Math.round(this.sy(oy));
            int fx = (int)Math.round((double)sx - 2.0 / (double)this.zoom);
            int fy = (int)Math.round((double)sy - 2.0 / (double)this.zoom);

            boolean un = this.unlocked(px);
            flatRect(flat, flatMatrix, fx, fy, fx + frameSize, fy + frameSize, -16777216);
            flatRect(flat, flatMatrix, fx + 1, fy + 1, fx + frameSize - 1, fy + 2, un ? -3750202 : -5000202);
            flatRect(flat, flatMatrix, fx + 1, fy + 1, fx + 2, fy + frameSize - 1, un ? -3750202 : -5000202);
            flatRect(flat, flatMatrix, fx + 1, fy + frameSize - 2, fx + frameSize - 1, fy + frameSize - 1, -1);
            flatRect(flat, flatMatrix, fx + frameSize - 2, fy + 1, fx + frameSize - 1, fy + frameSize - 1, -13421773);
            flatRect(flat, flatMatrix, fx + 2, fy + 2, fx + frameSize - 2, fy + frameSize - 2, un ? -8421504 : -7631989);
            visible.add(px);
            framePos.put(px, new int[]{sx, sy});
         }
      }
      BufferRenderer.drawWithGlobalProgram(flat.end());

      ALPerf.section("node-icons");
      RenderSystem.setShaderTexture(0, ICONS);
      RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
      Matrix4f iconMatrix = context.getMatrices().peek().getPositionMatrix();
      BufferBuilder iconBuf = Tessellator.getInstance().begin(
              VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
      for (Power px : visible) {
         double ox = (double)(px.x * 24);
         double oy = (double)(px.y * 24);
         int hier = this.hierarchy(px);
         float bright;
         if (this.unlocked(px)) {
            bright = 0.75F;
         } else if (this.canUnlock(px)) {
            bright = 1.0F;
         } else if (hier < 3) {
            bright = 0.3F;
         } else if (hier == 3) {
            bright = 0.2F;
         } else {
            bright = 0.1F;
         }

         int sx = framePos.get(px)[0];
         int sy = framePos.get(px)[1];

         int ix = (int)Math.round((double)sx + 3.0 / (double)this.zoom);
         int iy = (int)Math.round((double)sy + 3.0 / (double)this.zoom);
         float u0 = (float)(px.iconX * 16) / 256.0F;
         float v0 = (float)(px.iconY * 16) / 256.0F;
         float u1 = (float)(px.iconX * 16 + 16) / 256.0F;
         float v1 = (float)(px.iconY * 16 + 16) / 256.0F;
         iconQuad(iconBuf, iconMatrix, ix, iy, nodeSize, u0, v0, u1, v1, bright);
         if (!this.canUnlock(px)) {
            iconQuad(iconBuf, iconMatrix, ix, iy, nodeSize, u0, v0, u1, v1, bright * 0.35F);
         }

         if (mwx >= ox && mwx <= ox + 22.0 && mwy >= oy && mwy <= oy + 22.0) {
            this.hovered = px;
         }
      }
      BufferRenderer.drawWithGlobalProgram(iconBuf.end());

      context.disableScissor();
      ALPerf.section("panel-text");

      RenderSystem.setShader(GameRenderer::getPositionColorProgram);
      Matrix4f bandMatrix = context.getMatrices().peek().getPositionMatrix();
      BufferBuilder bands = Tessellator.getInstance().begin(
              VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

      flatRect(bands, bandMatrix, x, y, x + 256, y + 17, -3750202);
      flatRect(bands, bandMatrix, x, y + 17 + 155, x + 256, y + 202, -3750202);
      flatRect(bands, bandMatrix, x, y + 17, x + 8, y + 17 + 155, -3750202);
      flatRect(bands, bandMatrix, x + 8 + 224 + 8, y + 17, x + 256, y + 17 + 155, -3750202);
      flatRect(bands, bandMatrix, x, y, x + 256, y + 1, -1);
      flatRect(bands, bandMatrix, x, y, x + 1, y + 202, -1);
      flatRect(bands, bandMatrix, x, y + 202 - 1, x + 256, y + 202, -11184811);
      flatRect(bands, bandMatrix, x + 256 - 1, y, x + 256, y + 202, -11184811);
      flatRect(bands, bandMatrix, x + 8, y + 17 - 1, x + 8 + 224 + 8, y + 17, -13158601);
      flatRect(bands, bandMatrix, x + 8, y + 17 + 155, x + 8 + 224 + 8, y + 17 + 155 + 1, -1);
      flatRect(bands, bandMatrix, x + 8, y + 17 - 1, x + 9, y + 17 + 155, -13158601);
      flatRect(bands, bandMatrix, x + 7 + 224 + 8, y + 17 - 1, x + 8 + 224 + 8, y + 17 + 155, -13158601);
      BufferRenderer.drawWithGlobalProgram(bands.end());
      context.drawText(this.textRenderer, this.title, x + 15, y + 5, 4210752, false);
      int xp = ForceClientState.xp;
      int bp = ForceClientState.basePower();
      this.drawOutlined(context, Text.translatable("gui.forcePowers.xp", new Object[]{xp}).getString(), x + 15, y + 202 - 25, xp > 0 ? 8453920 : 14108744);
      this.drawOutlined(
         context, Text.translatable("gui.forcePowers.basePower", new Object[]{bp}).getString(), x + 15, y + 202 - 15, bp > 0 ? 8453920 : 14108744
      );
      this.doneButton.render(context, mouseX, mouseY, delta);
      if (this.hovered != null) {
         this.renderTooltip(context, this.hovered, mouseX, mouseY);
      }
      ALPerf.frameEnd();
   }

   private double sx(double worldX) {
      return (double)(this.panelX() + 16) + (worldX - this.camXS) / (double)this.zoom;
   }

   private double sy(double worldY) {
      return (double)(this.panelY() + 17) + (worldY - this.camYS) / (double)this.zoom;
   }

   private void drawLine(BufferBuilder buf, Matrix4f m, double x1, double y1, double x2, double y2, int color) {
      float dx = (float)(x2 - x1);
      float dy = (float)(y2 - y1);
      float length = MathHelper.sqrt(dx * dx + dy * dy);
      if (length < 1.0E-4F) {
         return;
      }

      float half = Math.max(0.5F, 1.0F / this.zoom);
      float nx = -dy / length * half;
      float ny = dx / length * half;
      buf.vertex(m, (float)x1 + nx, (float)y1 + ny, 0.0F).color(color);
      buf.vertex(m, (float)x2 + nx, (float)y2 + ny, 0.0F).color(color);
      buf.vertex(m, (float)x2 - nx, (float)y2 - ny, 0.0F).color(color);
      buf.vertex(m, (float)x1 - nx, (float)y1 - ny, 0.0F).color(color);
   }

   private void flatRect(BufferBuilder buf, Matrix4f m, int x1, int y1, int x2, int y2, int color) {
      buf.vertex(m, (float)x1, (float)y1, 0.0F).color(color);
      buf.vertex(m, (float)x1, (float)y2, 0.0F).color(color);
      buf.vertex(m, (float)x2, (float)y2, 0.0F).color(color);
      buf.vertex(m, (float)x2, (float)y1, 0.0F).color(color);
   }

   private void drawOutlined(DrawContext context, String s, int x, int y, int color) {
      context.drawText(this.textRenderer, s, x + 1, y, 0, false);
      context.drawText(this.textRenderer, s, x - 1, y, 0, false);
      context.drawText(this.textRenderer, s, x, y + 1, 0, false);
      context.drawText(this.textRenderer, s, x, y - 1, 0, false);
      context.drawText(this.textRenderer, s, x, y, color, false);
   }

   private void renderTooltip(DrawContext context, Power power, int mouseX, int mouseY) {
      List<Text> lines = new ArrayList<>();
      int cost = this.xpCost(power);
      boolean afford = ForceClientState.xp >= cost;
      Text title = power.getDisplayName();
      if (!this.canUnlock(power)) {

         if (this.hierarchy(power) >= 4) {
            return;
         }

         if (this.hierarchy(power) == 3) {
            title = Text.translatable("forcepower.unknown");
         }

         lines.add(Text.translatable("forcepower.requires", new Object[]{power.parent.getDisplayName()}).formatted(Formatting.RED));
      } else {
         if (cost != 0) {
            MutableText line = Text.translatable("forcepower.cost", new Object[]{cost});

            lines.add(!this.unlocked(power) ? line.formatted(Formatting.RED) : line);
         }

         if (power.stats.baseRequirement != 0) {
            boolean ok = ForceClientState.basePower() >= power.stats.baseRequirement;
            MutableText req = Text.translatable("forcepower.basePowerReq", new Object[]{power.stats.baseRequirement});
            lines.add(!ok && !this.unlocked(power) ? req.formatted(Formatting.RED) : req);
         }

         if (power.stats.useCost != 0.0F) {
            String key = switch (power.stats.powerType) {
               case PER_SECOND -> "forcepower.perSecond";
               case PASSIVE -> "forcepower.passive";
               default -> "forcepower.perUse";
            };
            String amount = this.formatDecimal(power.stats.useCost);
            lines.add(Text.translatable(key, new Object[]{amount}));
         }

         if (power.stats.baseBonus != 0) {
            lines.add(
               Text.translatable("forcepower.basePower", new Object[]{(power.stats.baseBonus < 0 ? "-" : "+") + Math.abs(power.stats.baseBonus)})
            );
         }

         if (power.stats.forceBonus != 0) {
            lines.add(
               Text.translatable("forcepower.forcePower", new Object[]{(power.stats.forceBonus < 0 ? "-" : "+") + Math.abs(power.stats.forceBonus)})
            );
         }

         if (power.stats.regen != 0) {
            String suffix = power.isRegenPercent() ? "%" : "";
            lines.add(
               Text.translatable("forcepower.forceRegen", new Object[]{(power.stats.regen < 0 ? "-" : "+") + Math.abs(power.stats.regen) + suffix})
            );
         }

         lines.add(Text.empty());
         lines.addAll(com.drag0nge0de.lightsabers.force.PowerDescriptions.lines(power));
      }

      int width = this.textRenderer.getWidth(title);

      for (Text line : lines) {
         width = Math.max(width, this.textRenderer.getWidth(line));
      }

      int tx = mouseX + 12;
      int w = width;
      int ty = mouseY - 4;

      int h = 6 + (2 + this.textRenderer.fontHeight) * (lines.size() + 1) + 12;

      context.fillGradient(tx - 3, ty - 3, tx + w + 3, ty + h, -805306368, -805306368);

      context.drawTextWithShadow(this.textRenderer, title, tx, ty, this.canUnlock(power) ? 16777215 : 8421504);
      int height = ty + this.textRenderer.fontHeight + 4;

      for (int i = 0; i < lines.size(); i++) {
         context.drawTextWithShadow(this.textRenderer, lines.get(i), tx, height, 0xA4A4A4);
         height += 2 + this.textRenderer.fontHeight;
      }

      if (this.unlocked(power)) {
         context.drawTextWithShadow(this.textRenderer, Text.translatable("forcepower.unlocked"), tx, height + 3, -7302913);
      } else if (this.canUnlock(power) || this.hierarchy(power) < 3 || ForceClientState.xp >= cost) {

         int invested = ForceClientState.xpInvested.getOrDefault(power.getName(), 0);
         context.drawTextWithShadow(
            this.textRenderer,
            Text.translatable("forcepower.xpLeft", new Object[]{Math.max(0, cost - invested)}),
            tx,
            height + 3,
            -7302913
         );
      }
   }

   private void iconQuad(BufferBuilder buf, Matrix4f m, float x, float y, float size,
         float u0, float v0, float u1, float v1, float bright) {
      buf.vertex(m, x, y, 0.0F).texture(u0, v0).color(bright, bright, bright, 1.0F);
      buf.vertex(m, x, y + size, 0.0F).texture(u0, v1).color(bright, bright, bright, 1.0F);
      buf.vertex(m, x + size, y + size, 0.0F).texture(u1, v1).color(bright, bright, bright, 1.0F);
      buf.vertex(m, x + size, y, 0.0F).texture(u1, v0).color(bright, bright, bright, 1.0F);
   }

   private String formatDecimal(float value) {
      if (value == Math.floor(value)) {
         return String.valueOf((int)value);
      } else {
         String s = String.valueOf(value);
         return s.length() > 4 ? s.substring(0, 4) : s;
      }
   }
}
