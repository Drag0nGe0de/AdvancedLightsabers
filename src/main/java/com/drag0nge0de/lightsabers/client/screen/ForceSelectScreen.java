package com.drag0nge0de.lightsabers.client.screen;

import com.drag0nge0de.lightsabers.AL;
import com.drag0nge0de.lightsabers.client.ALShotsBot;
import com.drag0nge0de.lightsabers.client.ForceClientState;
import com.drag0nge0de.lightsabers.force.Power;
import com.drag0nge0de.lightsabers.force.PowerType;
import com.drag0nge0de.lightsabers.network.ALNetwork;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ForceSelectScreen extends Screen {
   public static final Identifier TEXTURE = AL.id("textures/gui/container/force_power_selector.png");
   public static final Identifier ICONS = AL.id("textures/gui/icons.png");
   private static final int X_SIZE = 176;
   private static final int Y_SIZE = 166;

   private List<Power> slots = new ArrayList<>();
   private int grabbedId = -1;
   private int grabbedOffsetX;
   private int grabbedOffsetY;

   public ForceSelectScreen() {
      super(Text.translatable("gui.forcePowers.select"));
   }

   @Override
   protected void init() {
      super.init();
      this.slots.clear();
      List<Power> castable = new ArrayList<>();

      for (Power power : Power.allCastable()) {
         if (ForceClientState.hasPower(power.getName()) && !this.hasUnlockedChild(power)) {
            castable.add(power);
         }
      }

      castable.sort(Power::compareTo);
      this.slots.addAll(castable.subList(0, Math.min(16, castable.size())));
   }

   private boolean hasUnlockedChild(Power power) {
      for (Power child : power.children) {
         if (ForceClientState.hasPower(child.getName())) {
            return true;
         }
      }

      return false;
   }

   private int left() {
      return (this.width - X_SIZE) / 2;
   }

   private int top() {
      return (this.height - Y_SIZE) / 2;
   }

   private Power slotPower(int id) {
      return id >= 0 && id < this.slots.size() ? this.slots.get(id) : null;
   }

   private boolean inRect(int mouseX, int mouseY, int x, int y, int w, int h) {
      return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      int x = this.left();
      int y = this.top();
      int mx = (int)mouseX;
      int my = (int)mouseY;
      boolean changed = false;

      if (button == 0 && this.hasShiftDown()) {
         List<Integer> empty = new ArrayList<>();
         for (int i = 0; i < 3; i++) {
            if (ForceClientState.getSlotPower(i) == null) {
               empty.add(i);
            }
         }

         if (!empty.isEmpty() && this.grabbedId < 0) {
            for (int id = 0; id < 16; id++) {
               Power power = this.slotPower(id);
               if (power != null) {
                  int x1 = x + 8 + id % 4 * 18;
                  int y1 = y + 8 + id / 4 * 18;
                  if (this.inRect(mx, my, x1 - 1, y1 - 1, 18, 18)) {
                     ForceClientState.setSlotPower(empty.get(0), power.getName());
                     changed = true;
                  }
               }
            }
         }

         if (this.grabbedId < 0) {
            for (int i = 0; i < 3; i++) {
               int x1 = x + 62 + i * 18;
               int y1 = y + 142;
               if (ForceClientState.getSlotPower(i) != null && this.inRect(mx, my, x1 - 1, y1 - 1, 18, 18)) {
                  ForceClientState.setSlotPower(i, "");
                  changed = true;
               }
            }
         }
      }

      if (changed) {
         this.sendSlots();
         return true;
      }

      return super.mouseClicked(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
      int x = this.left();
      int y = this.top();
      int mx = (int)mouseX;
      int my = (int)mouseY;

      if (button == 0 && this.grabbedId < 0) {
         for (int id = 0; id < 16; id++) {
            Power power = this.slotPower(id);
            if (power != null) {
               int x1 = x + 8 + id % 4 * 18;
               int y1 = y + 8 + id / 4 * 18;
               if (this.inRect(mx, my, x1 - 1, y1 - 1, 18, 18)) {
                  this.grabbedId = id;
                  this.grabbedOffsetX = x1 - mx;
                  this.grabbedOffsetY = y1 - my;
               }
            }
         }
      }

      return super.mouseDragged(mouseX, mouseY, button, dx, dy);
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      int x = this.left();
      int y = this.top();
      int mx = (int)mouseX;
      int my = (int)mouseY;

      if (this.grabbedId >= 0) {
         Power grabbed = this.slotPower(this.grabbedId);
         if (grabbed != null) {
            for (int i = 0; i < 3; i++) {
               int x1 = x + 62 + i * 18;
               int y1 = y + 142;
               if (this.inRect(mx, my, x1 - 1, y1 - 1, 18, 18)) {
                  ForceClientState.setSlotPower(i, grabbed.getName());
                  this.sendSlots();
               }
            }
         }
      }

      this.grabbedId = -1;
      return super.mouseReleased(mouseX, mouseY, button);
   }

   private void sendSlots() {
      List<String> names = new ArrayList<>();
      for (int i = 0; i < 3; i++) {
         Power power = ForceClientState.getSlotPower(i);
         names.add(power != null ? power.getName() : "");
      }

      net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new ALNetwork.AssignSlotsPayload(names));
   }

   @Override
   public void render(DrawContext context, int mouseX, int mouseY, float delta) {
      int[] dbg = ALShotsBot.selectHover;
      if (dbg != null) {
         mouseX = dbg[0];
         mouseY = dbg[1];
      }

      super.render(context, mouseX, mouseY, delta);
      int x = this.left();
      int y = this.top();
      context.drawTexture(TEXTURE, x, y, 0.0F, 0.0F, X_SIZE, Y_SIZE, 256, 256);

      Power grabPower = null;
      Power hoverPower = null;
      int hoverX = -1;
      int hoverY = -1;

      for (int id = 0; id < 16; id++) {
         Power power = this.slotPower(id);
         int x1 = x + 8 + id % 4 * 18;
         int y1 = y + 8 + id / 4 * 18;

         if (power != null) {
            context.drawTexture(ICONS, x1, y1, power.iconX * 16, power.iconY * 16, 16, 16, 256, 256);
            if (this.grabbedId == id) {
               grabPower = power;
            }
         }

         if (this.inRect(mouseX, mouseY, x1 - 1, y1 - 1, 18, 18)) {
            context.fillGradient(x1, y1, x1 + 16, y1 + 16, -2130706433, -2130706433);
            hoverPower = power;
            hoverX = x1;
            hoverY = y1;
         }
      }

      int grabX = (int)mouseX + this.grabbedOffsetX;
      int grabY = (int)mouseY + this.grabbedOffsetY;

      for (int i = 0; i < 3; i++) {
         Power power = ForceClientState.getSlotPower(i);
         int x1 = x + 62 + i * 18;
         int y1 = y + 142;
         boolean hovered = this.inRect(mouseX, mouseY, x1 - 1, y1 - 1, 18, 18);

         if (hovered) {
            grabX = x1;
            grabY = y1;
         }

         if (power != null && (!hovered || grabPower == null)) {
            context.drawTexture(ICONS, x1, y1, power.iconX * 16, power.iconY * 16, 16, 16, 256, 256);
         }

         if (hovered) {
            context.fillGradient(x1, y1, x1 + 16, y1 + 16, -2130706433, -2130706433);
            hoverPower = power;
            hoverX = x1;
            hoverY = y1;
         }
      }

      if (grabPower != null) {
         context.drawTexture(ICONS, grabX, grabY, grabPower.iconX * 16, grabPower.iconY * 16, 16, 16, 256, 256);
      }

      if (hoverPower != null) {
         this.renderPowerInfo(context, hoverPower);
      }
   }

   private void renderPowerInfo(DrawContext context, Power power) {
      int x = this.left();
      int y = this.top();

      context.drawTexture(ICONS, x + 91, y + 8, 70, 70,
            power.iconX * 16, power.iconY * 16, 16, 16, 256, 256);

      String name = power.getDisplayName().getString();
      List<Text> desc = com.drag0nge0de.lightsabers.force.PowerDescriptions.lines(power);
      int x1 = x + 8;
      int y1 = y + 84;
      int fieldWidth = Math.max(this.textRenderer.getWidth(name), 160);

      for (Text line : desc) {
         fieldWidth = Math.max(fieldWidth, this.textRenderer.getWidth(line));
      }

      int fieldHeight = Math.max((this.textRenderer.fontHeight + 2) * desc.size()
            + this.textRenderer.fontHeight * 2 + 12, 52);
      context.fillGradient(x1, y1, x1 + fieldWidth, y1 + fieldHeight, 0xA5222222, 0xA5222222);
      context.drawTextWithShadow(this.textRenderer, name, x1 + 3, y1 + 3, -1);
      int cursor = y1 + this.textRenderer.fontHeight + 4;

      if (power.stats.useCost > 0) {
         String key = power.stats.powerType == PowerType.PER_USE
               ? "forcepower.perUse" : "forcepower.perSecond";
         context.drawTextWithShadow(this.textRenderer,
               Text.translatable(key, this.formatDecimal(power.stats.useCost)),
               x1 + 3, cursor + 3, 0xA4A4A4);
         cursor += 5 + this.textRenderer.fontHeight;
      }

      for (Text line : desc) {
         context.drawTextWithShadow(this.textRenderer, line, x1 + 3, cursor + 3, 0xA4A4A4);
         cursor += 2 + this.textRenderer.fontHeight;
      }
   }

   private String formatDecimal(float value) {
      if (value == Math.floor(value)) {
         return String.valueOf((int) value);
      }

      String s = String.valueOf(value);
      return s.length() > 4 ? s.substring(0, 4) : s;
   }

   @Override
   public boolean shouldPause() {
      return false;
   }
}
