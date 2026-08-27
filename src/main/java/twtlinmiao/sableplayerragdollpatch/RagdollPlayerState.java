package twtlinmiao.sableplayerragdollpatch;

import dev.leo.sableplayerragdoll.entity.RagdollSeatEntity;
import dev.leo.sableplayerragdoll.physics.RagdollSessionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public final class RagdollPlayerState {
   private RagdollPlayerState() {
   }

   public static boolean isRagdolled(Player player) {
      if (player instanceof ServerPlayer serverPlayer
            && RagdollSessionManager.isPlayerCurrentlyRagdolled(serverPlayer)) {
         return true;
      }

      return player.getVehicle() instanceof RagdollSeatEntity;
   }
}
