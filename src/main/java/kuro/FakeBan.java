package kuro;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * FakeBan: sunucudan temiz sekilde cikip ana menude, yazdigin metinle vanilla "Baglanti Kesildi"
 * ekranini gosterir (gercek bir kopma/ban ile ayni akis). Ekrandaki "Sunucu listesine don" dugmesi
 * normal cok oyunculu ekrana goturur.
 *
 * Onemli: Bu islem GERCEKTEN sunucudan ayrilmani saglar (yeniden girmen/siraya girmen gerekir). Sunucuya
 * ban/komut gondermez, baska oyunculari etkilemez; gosterilen metin tamamen senin yazdigin metindir.
 * Tek oyunculu dunyada dunyadan cikmaz, ekran oyunun ustunde acilir.
 */
public final class FakeBan {
   private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

   private FakeBan() {
   }

   public static Text buildTitle() {
      return switch (Config.fbanMode) {
         case 1 -> Text.literal(blankOr(Config.fdTitle, "Failed to connect to the server"));
         case 2 -> Text.literal(blankOr(Config.flsTitle, "Connection Lost"));
         case 3 -> Text.literal(blankOr(Config.fnetTitle, "Connection Lost"));
         default -> Text.literal(blankOr(Config.fbanTitle, "Bağlantı koptu"));
      };
   }

   public static MutableText buildBody() {
      return switch (Config.fbanMode) {
         case 1 -> donutBody();
         case 2 -> lifestealBody();
         case 3 -> networkBody();
         default -> chickenBody();
      };
   }

   /** Canli onizleme: sunucudan AYRILMADAN ban ekranini test etmek icin DisconnectedScreen'i acar. */
   public static void preview(net.minecraft.client.gui.screen.Screen parent) {
      MinecraftClient mc = Kuro.mc;
      mc.setScreen(new DisconnectedScreen(parent, buildTitle(), buildBody()));
   }

   public static void show() {
      MinecraftClient mc = Kuro.mc;
      if (mc.player == null || mc.world == null) {
         return;
      }

      Text title = buildTitle();
      MutableText body = buildBody();

      if (mc.isInSingleplayer() || mc.getNetworkHandler() == null) {
         // tek oyunculu: dunyadan cikma, ekrani oyunun ustunde goster
         mc.setScreen(new DisconnectedScreen(null, title, body));
         return;
      }

      // cok oyunculu: vanilla "Ayril" ile ayni akis (GameMenuScreen.disconnect), sonra ana menuden goster
      mc.world.disconnect();
      mc.disconnect();
      mc.setScreen(new DisconnectedScreen(new MultiplayerScreen(new TitleScreen()), title, body));
   }

   /** ChickenSMP modu: eski sahte ban ekrani. */
   private static MutableText chickenBody() {
      String headline = blankOr(Config.fbanReason, "Sunucudan yasaklandınız");
      String cause = blankOr(Config.fbanCause, "Hile Şüphesi");
      String date = blankOr(Config.fbanDate, LocalDate.now().format(DATE_FMT));
      String expiry = blankOr(Config.fbanExpiry, "10 dakika");

      // "Banlayan Kisi" her zaman Konsol; itiraz / adres satiri yok; Ban ID yok.
      MutableText body = Text.literal(headline).formatted(Formatting.RED);
      body = body.append(line("Banlanma Tarihi", date));
      body = body.append(line("Banlayan Kişi", "Konsol"));
      body = body.append(line("Sebep", cause));
      body = body.append(line("Açılma Tarihi", expiry));
      String link = Config.fbanLink == null ? "" : Config.fbanLink.trim();
      if (!link.isEmpty()) {
         String note = Config.fbanNote == null ? "" : Config.fbanNote.trim();
         if (!note.isEmpty()) {
            body = body.append(Text.literal("\n\n" + note).formatted(Formatting.YELLOW));
         }

         body = body.append(Text.literal("\n" + link).formatted(Formatting.RED));
      }

      return body;
   }

   /** DonutSMP modu: kirmizi sebep, gri "Date:" + beyaz tarih, gri itiraz satiri + beyaz link (Ban ID yok). */
   private static MutableText donutBody() {
      String reason = blankOr(Config.fdReason, "You are banned for botting DonutSMP.");
      String label = blankOr(Config.fdDateLabel, "Date:");
      String date = blankOr(Config.fdDate, LocalDate.now().format(DATE_FMT));
      String appeal = Config.fdAppeal == null ? "" : Config.fdAppeal.trim();
      String link = Config.fdLink == null ? "" : Config.fdLink.trim();

      MutableText body = Text.literal(reason).formatted(Formatting.RED);
      body = body.append(Text.literal("\n\n" + label + " ").formatted(Formatting.GRAY));
      body = body.append(Text.literal(date).formatted(Formatting.WHITE));
      if (!appeal.isEmpty()) {
         body = body.append(Text.literal("\n\n" + appeal).formatted(Formatting.GRAY));
         if (!link.isEmpty()) {
            body = body.append(Text.literal("\n" + link).formatted(Formatting.WHITE));
         }
      } else if (!link.isEmpty()) {
         body = body.append(Text.literal("\n\n" + link).formatted(Formatting.WHITE));
      }

      return body;
   }

   /** Lifesteal SMP modu: kalp bitmesi / elenme ban ekrani. */
   private static MutableText lifestealBody() {
      String reason = blankOr(Config.flsReason, "You ran out of hearts!");
      String server = blankOr(Config.flsServer, "Lifesteal SMP");
      String expiry = blankOr(Config.flsExpiry, "24 hours");
      String note = Config.flsNote == null ? "" : Config.flsNote.trim();
      String link = Config.flsLink == null ? "" : Config.flsLink.trim();

      MutableText body = Text.literal(reason).formatted(Formatting.RED, Formatting.BOLD);
      body = body.append(Text.literal("\n\nServer: ").formatted(Formatting.GRAY))
                 .append(Text.literal(server).formatted(Formatting.WHITE));
      body = body.append(Text.literal("\nBan Duration: ").formatted(Formatting.GRAY))
                 .append(Text.literal(expiry).formatted(Formatting.YELLOW));
      if (!note.isEmpty()) {
         body = body.append(Text.literal("\n\n" + note).formatted(Formatting.GRAY));
      }
      if (!link.isEmpty()) {
         body = body.append(Text.literal("\n" + link).formatted(Formatting.AQUA));
      }

      return body;
   }

   /** Ag Hatasi modu: gercekci baglanti kopmasi / timed out ekrani. */
   private static MutableText networkBody() {
      String msg = blankOr(Config.fnetMsg, "Internal Exception: java.io.IOException: An existing connection was forcibly closed by the remote host");
      return Text.literal(msg).formatted(Formatting.WHITE);
   }

   private static MutableText line(String label, String value) {
      return Text.literal("\n" + label + ": ").formatted(Formatting.GRAY).append(Text.literal(value).formatted(Formatting.RED));
   }

   private static String blankOr(String v, String fallback) {
      return v == null || v.isBlank() ? fallback : v;
   }
}
