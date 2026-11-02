package kuro.mixin;

import kuro.Kuro;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {
   // NoSlow: yemek yerken, yay ve kalkan kullanırken hareket yavaşlamasını engeller.
   @Inject(method = "tickMovement", at = @At("TAIL"))
   private void kuro$noSlow(CallbackInfo ci) {
      ClientPlayerEntity self = (ClientPlayerEntity)(Object)this;
      if (Kuro.on[Kuro.NOSLOW] && self.isUsingItem() && !self.hasVehicle()) {
         self.input.movementForward /= 0.2F;
         self.input.movementSideways /= 0.2F;
      }
   }
}
