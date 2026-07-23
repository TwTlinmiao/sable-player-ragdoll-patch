package twtlinmiao.sableplayerragdollpatch.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twtlinmiao.sableplayerragdollpatch.LeawindRagdollCameraCompat;

@Pseudo
@Mixin(targets = "com.github.leawind.thirdperson.ThirdPersonStatus", remap = false)
public class LeawindThirdPersonStatusMixin {
   @Inject(method = "isRenderingInThirdPerson()Z", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
   private static void spr$disableLeawindCameraWhileRagdolled(CallbackInfoReturnable<Boolean> cir) {
      if (LeawindRagdollCameraCompat.shouldSuppressLeawindCamera()) {
         cir.setReturnValue(false);
      }
   }

   @Inject(method = "shouldPickFromCamera()Z", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
   private static void spr$disableLeawindCameraPickWhileRagdolled(CallbackInfoReturnable<Boolean> cir) {
      if (LeawindRagdollCameraCompat.shouldSuppressLeawindCamera()) {
         cir.setReturnValue(false);
      }
   }
}
