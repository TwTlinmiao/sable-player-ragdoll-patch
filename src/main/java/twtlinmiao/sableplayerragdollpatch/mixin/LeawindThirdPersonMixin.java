package twtlinmiao.sableplayerragdollpatch.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twtlinmiao.sableplayerragdollpatch.LeawindRagdollCameraCompat;

@Pseudo
@Mixin(targets = "com.github.leawind.thirdperson.ThirdPerson", remap = false)
public class LeawindThirdPersonMixin {
   @Inject(method = "isAvailable()Z", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
   private static void spr$disableLeawindWhileSelfRagdolled(CallbackInfoReturnable<Boolean> cir) {
      if (LeawindRagdollCameraCompat.shouldSuppressLeawindCamera()) {
         cir.setReturnValue(false);
      }
   }
}
