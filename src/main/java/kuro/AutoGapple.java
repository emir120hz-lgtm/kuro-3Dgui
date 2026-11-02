package kuro;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * AutoGapple: Can Config.agHealth altina dusunce veya altin elma absorption (sari kalpler)
 * bittiginde hotbar'daki Enchanted Golden Apple veya Golden Apple'i otomatik secip yer.
 */
public final class AutoGapple {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static boolean eating;
   private static int prevSlot = -1;
   private static int waitTicks;

   private AutoGapple() {
   }

   public static void tick() {
      ClientPlayerEntity p = mc.player;
      if (p == null || mc.world == null || mc.currentScreen != null || Freecam.active) {
         reset();
         return;
      }

      if (eating) {
         waitTicks++;
         mc.options.useKey.setPressed(true);
         // Oyuncu esyayi kullanmayi biraktiysa (yedi veya iptal oldu)
         if ((waitTicks > 6 && !p.isUsingItem()) || !isHoldingGapple(p)) {
            reset();
         }
         return;
      }

      float hp = p.getHealth();
      float abs = p.getAbsorptionAmount();
      boolean lowHp = hp <= Config.agHealth;
      boolean needAbs = Config.agAbsorption >= 0.5F && abs <= 0.0F;

      if (!lowHp && !needAbs) {
         return;
      }

      // Hotbar'da once Enchanted Golden Apple, yoksa normal Golden Apple ara
      int slot = findGapple(p);
      if (slot < 0) {
         return;
      }

      prevSlot = p.getInventory().selectedSlot;
      p.getInventory().selectedSlot = slot;
      eating = true;
      waitTicks = 0;
      mc.options.useKey.setPressed(true);
   }

   private static boolean isHoldingGapple(ClientPlayerEntity p) {
      ItemStack main = p.getMainHandStack();
      return main.isOf(Items.ENCHANTED_GOLDEN_APPLE) || main.isOf(Items.GOLDEN_APPLE);
   }

   private static int findGapple(ClientPlayerEntity p) {
      for (int i = 0; i < 9; i++) {
         if (p.getInventory().getStack(i).isOf(Items.ENCHANTED_GOLDEN_APPLE)) {
            return i;
         }
      }
      for (int i = 0; i < 9; i++) {
         if (p.getInventory().getStack(i).isOf(Items.GOLDEN_APPLE)) {
            return i;
         }
      }
      return -1;
   }

   public static void reset() {
      if (eating && mc.player != null) {
         mc.options.useKey.setPressed(false);
         if (prevSlot >= 0) {
            mc.player.getInventory().selectedSlot = prevSlot;
         }
      }
      eating = false;
      prevSlot = -1;
      waitTicks = 0;
   }
}
