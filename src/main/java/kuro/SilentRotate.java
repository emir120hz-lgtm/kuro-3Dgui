package kuro;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * SilentRotate: Sunucuya giden hareket ve vuruş paketlerindeki bakış açısını (yaw/pitch)
 * hedefe yönlendirirken oyuncunun ekran kamerasını sabit tutar.
 */
public final class SilentRotate {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static boolean active;
   private static float serverYaw;
   private static float serverPitch;
   private static int stayTicks;

   private SilentRotate() {
   }

   public static boolean hasRotation() {
      return Kuro.on[Kuro.SILENTROTATE] && active && stayTicks > 0;
   }

   public static float getYaw() {
      return serverYaw;
   }

   public static float getPitch() {
      return serverPitch;
   }

   public static void aimAt(Vec3d target, int ticks) {
      ClientPlayerEntity p = mc.player;
      if (p == null) return;
      Vec3d eye = p.getEyePos();
      double dx = target.x - eye.x;
      double dy = target.y - eye.y;
      double dz = target.z - eye.z;
      float yaw = (float)Math.toDegrees(Math.atan2(dz, dx)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz))));
      set(yaw, pitch, ticks);
   }

   public static void set(float yaw, float pitch, int ticks) {
      serverYaw = yaw;
      serverPitch = MathHelper.clamp(pitch, -90.0F, 90.0F);
      stayTicks = Math.max(1, ticks);
      active = true;
   }

   public static void tick() {
      if (stayTicks > 0) {
         stayTicks--;
         if (stayTicks <= 0) {
            active = false;
         }
      }
   }

   public static void reset() {
      active = false;
      stayTicks = 0;
   }
}
