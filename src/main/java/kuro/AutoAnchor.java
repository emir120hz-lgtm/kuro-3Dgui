package kuro;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * AutoAnchor: Hedef oyuncunun yakınına otomatik Respawn Anchor koyup glowstone ile şarj eder ve patlatır (Overworld/End).
 */
public final class AutoAnchor {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   private static int cd;

   private AutoAnchor() {
   }

   public static void tick() {
      ClientPlayerEntity p = mc.player;
      if (p == null || mc.world == null || mc.currentScreen != null || Freecam.active || mc.interactionManager == null) {
         reset();
         return;
      }

      if (cd > 0) {
         cd--;
         return;
      }

      // Sadece anchor'ın patladığı boyutlarda çalış (Nether'da spawn ayarlar, patlamaz)
      if (mc.world.getDimension().respawnAnchorWorks()) {
         return;
      }

      PlayerEntity target = findTarget(p);
      if (target == null) {
         return;
      }

      // 1. Etrafta mevcut bir Respawn Anchor var mı kontrol et
      BlockPos nearbyAnchor = findExistingAnchor(p, target);
      if (nearbyAnchor != null) {
         BlockState st = mc.world.getBlockState(nearbyAnchor);
         int charges = st.get(RespawnAnchorBlock.CHARGES);
         if (charges == 0) {
            // Şarj et: Glowstone tut ve tıkla
            int glowSlot = findHotbar(p, Items.GLOWSTONE);
            if (glowSlot >= 0) {
               int prev = p.getInventory().selectedSlot;
               p.getInventory().selectedSlot = glowSlot;
               interact(p, nearbyAnchor);
               p.getInventory().selectedSlot = prev;
               cd = Math.max(1, Math.round(Config.ancDelay));
               return;
            }
         } else {
            // Patlat: Glowstone OLMAYAN bir eşyayla (totem, kılıç, vb.) tıkla
            int safeSlot = findNonGlowstone(p);
            if (safeSlot >= 0) {
               int prev = p.getInventory().selectedSlot;
               p.getInventory().selectedSlot = safeSlot;
               interact(p, nearbyAnchor);
               p.getInventory().selectedSlot = prev;
               cd = Math.max(1, Math.round(Config.ancDelay));
               return;
            }
         }
      }

      // 2. Yeni anchor yerleştir
      BlockPos placePos = findPlacePos(target);
      if (placePos != null) {
         int anchorSlot = findHotbar(p, Items.RESPAWN_ANCHOR);
         if (anchorSlot >= 0) {
            int prev = p.getInventory().selectedSlot;
            p.getInventory().selectedSlot = anchorSlot;
            interact(p, placePos.down(), Direction.UP);
            p.getInventory().selectedSlot = prev;
            cd = Math.max(1, Math.round(Config.ancDelay));
         }
      }
   }

   private static PlayerEntity findTarget(ClientPlayerEntity p) {
      PlayerEntity best = null;
      double bestDist = (double)Config.ancRange * Config.ancRange;
      for (PlayerEntity o : mc.world.getPlayers()) {
         if (o != p && o.isAlive() && !o.isSpectator()) {
            double d = p.squaredDistanceTo(o);
            if (d < bestDist) {
               bestDist = d;
               best = o;
            }
         }
      }
      return best;
   }

   private static BlockPos findExistingAnchor(ClientPlayerEntity p, PlayerEntity target) {
      BlockPos center = target.getBlockPos();
      double maxD = (double)Config.ancRange * Config.ancRange;
      for (int x = -2; x <= 2; x++) {
         for (int y = -1; y <= 2; y++) {
            for (int z = -2; z <= 2; z++) {
               BlockPos pos = center.add(x, y, z);
               if (mc.world.getBlockState(pos).isOf(Blocks.RESPAWN_ANCHOR)) {
                  if (p.squaredDistanceTo(Vec3d.ofCenter(pos)) <= maxD) {
                     return pos;
                  }
               }
            }
         }
      }
      return null;
   }

   private static BlockPos findPlacePos(PlayerEntity target) {
      BlockPos t = target.getBlockPos();
      BlockPos[] candidates = new BlockPos[]{
         t.north(), t.south(), t.east(), t.west(),
         t.north().down(), t.south().down(), t.east().down(), t.west().down()
      };
      for (BlockPos cp : candidates) {
         if (mc.world.getBlockState(cp).isAir() && mc.world.getBlockState(cp.down()).isSolidBlock(mc.world, cp.down())) {
            return cp;
         }
      }
      return null;
   }

   private static void interact(ClientPlayerEntity p, BlockPos pos) {
      interact(p, pos, Direction.UP);
   }

   private static void interact(ClientPlayerEntity p, BlockPos pos, Direction side) {
      BlockHitResult bhr = new BlockHitResult(Vec3d.ofCenter(pos), side, pos, false);
      mc.interactionManager.interactBlock(p, Hand.MAIN_HAND, bhr);
      p.swingHand(Hand.MAIN_HAND);
   }

   private static int findHotbar(ClientPlayerEntity p, net.minecraft.item.Item item) {
      for (int i = 0; i < 9; i++) {
         if (p.getInventory().getStack(i).isOf(item)) {
            return i;
         }
      }
      return -1;
   }

   private static int findNonGlowstone(ClientPlayerEntity p) {
      for (int i = 0; i < 9; i++) {
         if (!p.getInventory().getStack(i).isOf(Items.GLOWSTONE)) {
            return i;
         }
      }
      return 0;
   }

   public static void reset() {
      cd = 0;
   }
}
