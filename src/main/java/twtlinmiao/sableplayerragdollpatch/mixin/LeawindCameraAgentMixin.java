package twtlinmiao.sableplayerragdollpatch.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twtlinmiao.sableplayerragdollpatch.LeawindRagdollCameraCompat;

@Pseudo
@Mixin(targets = "com.github.leawind.thirdperson.core.CameraAgent", remap = false)
public class LeawindCameraAgentMixin {
   @Inject(method = "onRenderTickStart(DDF)V", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
   private void spr$skipRenderTickWhileRagdolled(double time, double deltaTime, float partialTick, CallbackInfo ci) {
      if (LeawindRagdollCameraCompat.shouldSuppressLeawindCamera()) {
         ci.cancel();
      }
   }

   @Inject(method = "onClientTickStart()V", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
   private void spr$skipClientTickWhileRagdolled(CallbackInfo ci) {
      if (LeawindRagdollCameraCompat.shouldSuppressLeawindCamera()) {
         ci.cancel();
      }
   }

   @Inject(method = "pick(D)Lnet/minecraft/world/phys/HitResult;", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
   private void spr$skipPickWhileRagdolled(double range, CallbackInfoReturnable<HitResult> cir) {
      if (LeawindRagdollCameraCompat.shouldSuppressLeawindCamera()) {
         cir.setReturnValue(BlockHitResult.miss(Vec3.ZERO, Direction.EAST, BlockPos.ZERO));
      }
   }

   @Inject(method = "pickEntity(D)Lnet/minecraft/world/phys/EntityHitResult;", at = @At("HEAD"), cancellable = true, require = 0, remap = false)
   private void spr$skipEntityPickWhileRagdolled(double range, CallbackInfoReturnable<EntityHitResult> cir) {
      if (LeawindRagdollCameraCompat.shouldSuppressLeawindCamera()) {
         cir.setReturnValue(null);
      }
   }
}
