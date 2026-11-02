package kuro;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import org.joml.Matrix4f;

/**
 * PortalESP: Yüklenen chunk'lardaki Nether Portallarını ve obsidyen portalları bulup çerçeveler.
 */
public final class PortalESP {
   private static final MinecraftClient mc = MinecraftClient.getInstance();
   public static final List<BlockPos> hits = new ArrayList<>();
   private static final Set<Long> notified = new HashSet<>();
   private static int tick;

   private PortalESP() {
   }

   public static void tick() {
      if (mc.world == null || mc.player == null) {
         reset();
         return;
      }

      if (++tick < 30) {
         return;
      }
      tick = 0;

      hits.clear();
      ChunkPos cp = mc.player.getChunkPos();
      int r = Math.min((Integer)mc.options.getViewDistance().getValue(), 8);

      for (int dx = -r; dx <= r; dx++) {
         for (int dz = -r; dz <= r; dz++) {
            WorldChunk c = mc.world.getChunkManager().getWorldChunk(cp.x + dx, cp.z + dz);
            if (c == null) continue;

            ChunkSection[] secs = c.getSectionArray();
            int bottom = c.getBottomSectionCoord();
            for (int i = 0; i < secs.length; i++) {
               ChunkSection s = secs[i];
               if (s == null || s.isEmpty() || !s.hasAny(st -> st.isOf(Blocks.NETHER_PORTAL))) {
                  continue;
               }

               int base = (bottom + i) << 4;
               for (int y = 0; y < 16; y++) {
                  int wy = base + y;
                  for (int z = 0; z < 16; z++) {
                     for (int x = 0; x < 16; x++) {
                        if (s.getBlockState(x, y, z).isOf(Blocks.NETHER_PORTAL)) {
                           BlockPos p = new BlockPos(cp.x * 16 + dx * 16 + x, wy, cp.z * 16 + dz * 16 + z);
                           hits.add(p);

                           long key = p.asLong();
                           if (Config.ptNotify >= 0.5F && notified.add(key)) {
                              if (notified.size() > 500) notified.clear();
                              mc.player.sendMessage(Text.literal("§d[PortalESP] §fNether portalı bulundu: §e" + p.getX() + " " + p.getY() + " " + p.getZ()), false);
                           }
                        }
                     }
                  }
               }
            }
         }
      }
   }

   public static void render(BufferBuilder b, Matrix4f m, Vec3d cam) {
      if (hits.isEmpty()) return;
      for (BlockPos p : hits) {
         Box bb = new Box(p.getX(), p.getY(), p.getZ(), p.getX() + 1, p.getY() + 1, p.getZ() + 1);
         // Mor renk (0xBB, 0x33, 0xFF)
         Render.box(b, m, bb, 0.73F, 0.20F, 1.0F, 0.9F);
      }
   }

   public static void reset() {
      hits.clear();
      tick = 0;
   }
}
