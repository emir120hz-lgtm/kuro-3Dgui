package kuro.mixin;

import kuro.Config;
import kuro.Kuro;
import kuro.SilentRotate;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
   @Unique
   private static boolean kuro$silentSending;

   // SilentRotate: hareket paketlerindeki bakis acisini sunucuya hedefe yonlendir
   @Inject(method = "sendPacket(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
   private void kuro$silentRotate(Packet<?> packet, CallbackInfo ci) {
      if (kuro$silentSending || !SilentRotate.hasRotation()) {
         return;
      }

      if (packet instanceof PlayerMoveC2SPacket move && move.changesLook()) {
         ci.cancel();
         kuro$silentSending = true;
         try {
            MinecraftClient mc = MinecraftClient.getInstance();
            double x = move.getX(mc.player != null ? mc.player.getX() : 0.0);
            double y = move.getY(mc.player != null ? mc.player.getY() : 0.0);
            double z = move.getZ(mc.player != null ? mc.player.getZ() : 0.0);
            boolean onGround = move.isOnGround();
            boolean collision = mc.player != null && mc.player.horizontalCollision;
            float yaw = SilentRotate.getYaw();
            float pitch = SilentRotate.getPitch();

            Packet<?> modified;
            if (move.changesPosition()) {
               modified = new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, onGround, collision);
            } else {
               modified = new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, onGround, collision);
            }

            ((ClientPlayNetworkHandler)(Object)this).sendPacket(modified);
         } finally {
            kuro$silentSending = false;
         }
      }
   }
   // Velocity: sunucunun bize verdiği itme (knockback) paketini engelle / azalt.
   @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
   private void kuro$velocity(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
      MinecraftClient mc = MinecraftClient.getInstance();
      // ag thread'inde dokunma; vanilla paketi ana thread'e yeniden kuyruklar ve burasi tekrar cagrilir
      if (!Kuro.on[Kuro.VELOCITY] || !mc.isOnThread() || mc.player == null || packet.getEntityId() != mc.player.getId()) {
         return;
      }

      ci.cancel();
      double keep = Config.velocityKeep / 100.0;
      if (keep > 0.0) {
         mc.player.setVelocityClient(packet.getVelocityX() * keep, packet.getVelocityY() * keep, packet.getVelocityZ() * keep);
      }
   }
}
