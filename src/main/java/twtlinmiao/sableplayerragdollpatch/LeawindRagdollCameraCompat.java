package twtlinmiao.sableplayerragdollpatch;

import dev.leo.sableplayerragdoll.entity.RagdollSeatEntity;
import net.minecraft.client.Minecraft;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchClientConfig;

public final class LeawindRagdollCameraCompat {
   private static final long SUPPRESS_AFTER_RAGDOLL_TICKS = 20L;
   private static long lastRagdollGameTime = Long.MIN_VALUE;

   private LeawindRagdollCameraCompat() {
   }

   public static boolean shouldSuppressLeawindCamera() {
      if (!RagdollPatchClientConfig.LEAWIND_CAMERA_COMPAT_ENABLED.get()) {
         return false;
      }

      Minecraft minecraft = Minecraft.getInstance();
      if (minecraft.player == null) {
         return false;
      }

      if (minecraft.player.getVehicle() instanceof RagdollSeatEntity) {
         markRagdolled(minecraft);
         return true;
      }

      if (minecraft.level == null || lastRagdollGameTime == Long.MIN_VALUE) {
         return false;
      }

      long elapsed = minecraft.level.getGameTime() - lastRagdollGameTime;
      return elapsed >= 0L && elapsed <= SUPPRESS_AFTER_RAGDOLL_TICKS;
   }

   private static void markRagdolled(Minecraft minecraft) {
      if (minecraft.level != null) {
         lastRagdollGameTime = minecraft.level.getGameTime();
      }
   }
}
