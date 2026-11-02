package kuro.ui;

import static org.lwjgl.nanovg.NanoVG.*;

import kuro.Config;
import kuro.Kuro;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

/**
 * 3D HUD: Kamera dönüşü ve fare hareketine göre NanoVG HUD katmanını 3 boyutlu
 * eğim (skew/tilt), dönme ve katman paralaksı ile hareketlendirir.
 */
public final class Hud3D {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static float prevYaw;
   private static float prevPitch;
   private static boolean initialized;

   private static float currentTiltX;
   private static float currentTiltY;
   private static float targetTiltX;
   private static float targetTiltY;
   private static float roll;

   private Hud3D() {
   }

   public static void update(float dt) {
      if (mc.player == null) {
         initialized = false;
         currentTiltX = 0.0F;
         currentTiltY = 0.0F;
         roll = 0.0F;
         return;
      }

      float yaw = mc.player.getYaw();
      float pitch = mc.player.getPitch();

      if (!initialized) {
         prevYaw = yaw;
         prevPitch = pitch;
         initialized = true;
         return;
      }

      float dyaw = MathHelper.wrapDegrees(yaw - prevYaw);
      float dpitch = pitch - prevPitch;
      prevYaw = yaw;
      prevPitch = pitch;

      float power = Math.max(0.1F, Config.h3dTilt);
      targetTiltX = MathHelper.clamp(dyaw * -0.015F * power, -0.15F, 0.15F);
      targetTiltY = MathHelper.clamp(dpitch * 0.012F * power, -0.12F, 0.12F);

      float smooth = Math.max(0.05F, Math.min(1.0F, dt * 12.0F * Config.h3dSmooth));
      currentTiltX += (targetTiltX - currentTiltX) * smooth;
      currentTiltY += (targetTiltY - currentTiltY) * smooth;
      roll = currentTiltX * 0.35F;
   }

   public static void applyTransform(float lw, float lh, float dt) {
      update(dt);
      if (Math.abs(currentTiltX) < 1.0E-4F && Math.abs(currentTiltY) < 1.0E-4F) {
         return;
      }

      nvgSave(Nvg.ctx);
      float cx = lw * 0.5F;
      float cy = lh * 0.5F;
      nvgTranslate(Nvg.ctx, cx, cy);
      nvgSkewX(Nvg.ctx, currentTiltX);
      nvgSkewY(Nvg.ctx, currentTiltY);
      nvgRotate(Nvg.ctx, roll);
      nvgTranslate(Nvg.ctx, -cx, -cy);
   }

   public static void restoreTransform() {
      if (Math.abs(currentTiltX) < 1.0E-4F && Math.abs(currentTiltY) < 1.0E-4F) {
         return;
      }
      nvgRestore(Nvg.ctx);
   }

   public static void applyElementParallax(int id) {
      if (Config.h3dParallax <= 0.0F) {
         return;
      }

      // Her ögenin 3D uzaydaki algılanan derinlik katsayısı
      float depth = switch (id) {
         case 0 -> 0.7F; // Filigran
         case 1 -> 0.85F; // Kısayol paneli
         case 2 -> 0.9F; // Modül listesi
         case 3 -> 0.75F; // Bilgi çubuğu
         case 4 -> 1.7F; // TargetHUD (en önde yüzen 3D öğe)
         case 5 -> 0.8F; // Radar
         case 6 -> 1.0F; // Spotify
         case 7 -> 1.4F; // ArmorHUD (önde yüzen zırh paneli)
         default -> 1.0F;
      };

      float shiftX = currentTiltX * 70.0F * depth * Config.h3dParallax;
      float shiftY = currentTiltY * 70.0F * depth * Config.h3dParallax;
      nvgTranslate(Nvg.ctx, shiftX, shiftY);
   }
}
