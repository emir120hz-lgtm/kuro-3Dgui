package kuro.mixin;

import kuro.SilentRotate;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// sendPacket(Packet) 1.21.4'te ClientPlayNetworkHandler'da degil, ust sinifi
// ClientCommonNetworkHandler'da tanimli.
@Mixin(ClientCommonNetworkHandler.class)
public abstract class ClientCommonNetworkHandlerMixin {
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

            ((ClientCommonNetworkHandler)(Object)this).sendPacket(modified);
         } finally {
            kuro$silentSending = false;
         }
      }
   }
}
