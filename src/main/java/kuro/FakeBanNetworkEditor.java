package kuro;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class FakeBanNetworkEditor extends Screen {
   private final Screen parent;
   private TextFieldWidget title;
   private TextFieldWidget msg;
   private int fieldX;
   private int fieldY0;
   private static final int ROW = 36;

   public FakeBanNetworkEditor(Screen parent) {
      super(Text.literal("FakeBan Ağ Hatası"));
      this.parent = parent;
   }

   @Override
   protected void init() {
      int w = Math.min(360, this.width - 40);
      fieldX = this.width / 2 - w / 2;
      fieldY0 = Math.max(50, this.height / 2 - 60);

      title = field(0, w, Config.fnetTitle, "Connection Lost");
      msg = field(1, w, Config.fnetMsg, "Internal Exception: java.io.IOException: An existing connection was forcibly closed by the remote host");

      int by = fieldY0 + ROW * 2 + 12;
      int bw = (w - 16) / 3;
      this.addDrawableChild(ButtonWidget.builder(Text.literal("Önizle"), b -> {
         apply();
         Config.fbanMode = 3;
         FakeBan.preview(this);
      }).dimensions(fieldX, by, bw, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.literal("Göster"), b -> {
         apply();
         Config.fbanMode = 3;
         Config.save();
         FakeBan.show();
      }).dimensions(fieldX + bw + 8, by, bw, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.literal("Kaydet ve kapat"), b -> close())
         .dimensions(fieldX + (bw + 8) * 2, by, bw, 20).build());
   }

   private TextFieldWidget field(int row, int w, String value, String hint) {
      TextFieldWidget f = new TextFieldWidget(this.textRenderer, fieldX, fieldY0 + row * ROW + 14, w, 20, Text.literal(hint));
      f.setMaxLength(300);
      f.setPlaceholder(Text.literal(hint));
      f.setText(value == null ? "" : value);
      this.addDrawableChild(f);
      return f;
   }

   private void apply() {
      Config.fnetTitle = title.getText();
      Config.fnetMsg = msg.getText();
      Config.save();
   }

   @Override
   public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
      super.render(ctx, mouseX, mouseY, delta);
      ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Ağ Hatası / Kopma Mesajı Ayarları (Göster: sunucudan çıkar)"), this.width / 2, fieldY0 - 24, 0xFFFFFF);
      String[] labels = {"Başlık", "Hata Metni (Örn. Timed out, SocketException, Bad Packet)"};
      for (int i = 0; i < labels.length; i++) {
         ctx.drawTextWithShadow(this.textRenderer, labels[i], fieldX, fieldY0 + i * ROW + 1, 0xAAAAAA);
      }
   }

   @Override
   public void removed() {
      apply();
   }

   @Override
   public void close() {
      this.client.setScreen(parent);
   }
}
