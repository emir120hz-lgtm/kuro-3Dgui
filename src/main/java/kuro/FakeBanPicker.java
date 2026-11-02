package kuro;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * FakeBan mod secici: FakeBan'a sag tiklayinca acilir. Alt alta iki kutu: ChickenSMP ve DonutSMP.
 * Birine tiklayinca o mod secilir (tusa basinca bu mod gosterilir) ve o modun metin duzenleyicisi acilir.
 */
public final class FakeBanPicker extends Screen {
   private final Screen parent;

   public FakeBanPicker(Screen parent) {
      super(Text.literal("FakeBan"));
      this.parent = parent;
   }

   /** Modun duzenleme ekrani (0 = ChickenSMP, 1 = DonutSMP, 2 = Lifesteal SMP, 3 = Ag Hatasi). */
   public static Screen editorFor(int mode, Screen parent) {
      return switch (mode) {
         case 1 -> new FakeBanDonutEditor(parent);
         case 2 -> new FakeBanLifestealEditor(parent);
         case 3 -> new FakeBanNetworkEditor(parent);
         default -> new FakeBanEditor(parent);
      };
   }

   @Override
   protected void init() {
      int w = Math.min(230, this.width - 40);
      int x = this.width / 2 - w / 2;
      int y = this.height / 2 - 62;
      this.addDrawableChild(ButtonWidget.builder(label("ChickenSMP", 0), b -> choose(0)).dimensions(x, y, w, 22).build());
      this.addDrawableChild(ButtonWidget.builder(label("DonutSMP", 1), b -> choose(1)).dimensions(x, y + 25, w, 22).build());
      this.addDrawableChild(ButtonWidget.builder(label("Lifesteal SMP", 2), b -> choose(2)).dimensions(x, y + 50, w, 22).build());
      this.addDrawableChild(ButtonWidget.builder(label("Ağ Hatası (Timed Out)", 3), b -> choose(3)).dimensions(x, y + 75, w, 22).build());
      int bw = (w - 6) / 2;
      this.addDrawableChild(ButtonWidget.builder(Text.literal("Canlı Önizle"), b -> FakeBan.preview(this)).dimensions(x, y + 104, bw, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.literal("Geri"), b -> close()).dimensions(x + bw + 6, y + 104, bw, 20).build());
   }

   private static Text label(String name, int mode) {
      boolean sel = Config.fbanMode == mode;
      return sel ? Text.literal("> " + name + " <").formatted(Formatting.GREEN) : Text.literal(name);
   }

   private void choose(int mode) {
      Config.fbanMode = mode;
      Config.save();
      this.client.setScreen(editorFor(mode, parent));
   }

   @Override
   public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
      super.render(ctx, mouseX, mouseY, delta);
      ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("FakeBan modu seç"), this.width / 2, this.height / 2 - 48, 0xFFFFFF);
      ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Tuşa basınca seçili mod gösterilir"), this.width / 2, this.height / 2 + 94, 0xAAAAAA);
   }

   @Override
   public void close() {
      this.client.setScreen(parent);
   }
}
