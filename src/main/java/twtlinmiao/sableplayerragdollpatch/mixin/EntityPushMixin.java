package twtlinmiao.sableplayerragdollpatch.mixin;

import twtlinmiao.sableplayerragdollpatch.NoCollideState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityPushMixin {

   private static final String RAGDOLL_CLASS = "dev.leo.sableplayerragdoll.entity.RagdollDollEntity";

   private static boolean isRagdollDoll(Entity entity) {
      return entity != null && entity.getClass().getName().equals(RAGDOLL_CLASS);
   }

   @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true, remap = false)
   private void sprOnIsPushable(CallbackInfoReturnable<Boolean> cir) {
      if (NoCollideState.isEnabled() && isRagdollDoll((Entity) (Object) this)) {
         cir.setReturnValue(false);
      }
   }

   @Inject(method = "push", at = @At("HEAD"), cancellable = true, remap = false)
   private void sprOnPush(Entity other, CallbackInfo ci) {
      if (NoCollideState.isEnabled()) {
         Entity self = (Entity) (Object) this;
         if (isRagdollDoll(self) || isRagdollDoll(other)) {
            ci.cancel();
         }
      }
   }
}
