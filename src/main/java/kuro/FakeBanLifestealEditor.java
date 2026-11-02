package kuro;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public final class FakeBanLifestealEditor extends Screen {
   private final Screen parent;
   private TextFieldWidget title;
   private TextFieldWidget reason;
   private TextFieldWidget server;
   private TextFieldWidget expiry;
   private TextFieldWidget note;
   private TextFieldWidget link;
   private int fieldX;
   private int fieldY0;
   private static final int ROW = 30;

   public FakeBanLifestealEditor(Screen parent) {
      super(Text.literal("FakeBan Lifesteal SMP"));
      this.parent = parent;
   }

   @Override
   protected void init() {
      int w = Math.min(340, this.width - 40);
      fieldX = this.width / 2 - w / 2;
      fieldY0 = Math.max(30, this.height / 2 - 96);

      title = field(0, w, Config.flsTitle, "Connection Lost");
      reason = field(1, w, Config.flsReason, "You ran out of hearts!");
      server = field(2, w, Config.flsServer, "Lifesteal SMP");
      expiry = field(3, w, Config.flsExpiry, "24 hours");
      note = field(4, w, Config.flsNote, "Ask a teammate to revive you using a Heart / Beacon");
      link = field(5, w, Config.flsLink, "store.lifestealsmp.com");

      int by = fieldY0 + ROW * 6 + 4;
      int bw = (w - 16) / 3;
      this.addDrawableChild(ButtonWidget.builder(Text.literal("Önizle"), b -> {
         apply();
         Config.fbanMode = 2;
         FakeBan.preview(this);
      }).dimensions(fieldX, by, bw, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.literal("Göster"), b -> {
         apply();
         Config.fbanMode = 2;
         Config.save();
         FakeBan.show();
      }).dimensions(fieldX + bw + 8, by, bw, 20).build());
      this.addDrawableChild(ButtonWidget.builder(Text.literal("Kaydet ve kapat"), b -> close())
         .dimensions(fieldX + (bw + 8) * 2, by, bw, 20).build());
   }

   private TextFieldWidget field(int row, int w, String value, String hint) {
      TextFieldWidget f = new TextFieldWidget(this.textRenderer, fieldX, fieldY0 + row * ROW + 12, w, 20, Text.literal(hint));
      f.setMaxLength(200);
      f.setPlaceholder(Text.literal(hint));
      f.setText(value == null ? "" : value);
      this.addDrawableChild(f);
      return f;
   }

   private void apply() {
      Config.flsTitle = title.getText();
      Config.flsReason = reason.getText();
      Config.flsServer = server.getText();
      Config.flsExpiry = expiry.getText();
      Config.flsNote = note.getText();
      Config.flsLink = link.getText();
      Config.save();
   }

   @Override
   public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
      super.render(ctx, mouseX, mouseY, delta);
      ctx.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Lifesteal SMP ayarları (Göster: sunucudan çıkıp ana menüde gösterir)"), this.width / 2, fieldY0 - 22, 0xFFFFFF);
      String[] labels = {"Başlık", "Sebep (kırmızı kalın)", "Sunucu adı", "Ban süresi", "Canlandırma mesajı", "Mağaza / Link (mavi)"};
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
