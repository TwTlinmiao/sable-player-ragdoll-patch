package twtlinmiao.sableplayerragdollpatch.mixin;

import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import twtlinmiao.sableplayerragdollpatch.ActiveGrabbers;
import twtlinmiao.sableplayerragdollpatch.RagdollGrabAccess;
import java.util.Map;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RagdollPartBlockEntity.class)
public class RagdollPartBlockEntityMixin implements RagdollGrabAccess {

   @Shadow
   private Map<UUID, ?> grabbers;

   @Inject(method = "startGrab", at = @At("TAIL"), remap = false)
   private void onStartGrab(UUID playerId, CallbackInfo ci) {
      if (playerId != null) ActiveGrabbers.PLAYERS.add(playerId);
   }

   @Inject(method = "stopGrab", at = @At("TAIL"), remap = false)
   private void onStopGrab(UUID playerId, CallbackInfo ci) {
      if (playerId != null) ActiveGrabbers.PLAYERS.remove(playerId);
   }

   @Override
   public boolean spr$isGrabbedBy(UUID playerId) {
      return this.grabbers != null && this.grabbers.containsKey(playerId);
   }
}
