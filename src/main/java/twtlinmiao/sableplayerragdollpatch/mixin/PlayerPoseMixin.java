package twtlinmiao.sableplayerragdollpatch.mixin;

import twtlinmiao.sableplayerragdollpatch.NoCollideState;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerPoseMixin {
   @Inject(method = "canPlayerFitWithinBlocksAndEntitiesWhen", at = @At("HEAD"), remap = false)
   private void sprOnPoseCheckStart(Pose pose, CallbackInfoReturnable<Boolean> cir) {
      NoCollideState.INSIDE_PLAYER_POSE_CHECK.set(true);
   }

   @Inject(method = "canPlayerFitWithinBlocksAndEntitiesWhen", at = @At("RETURN"), remap = false)
   private void sprOnPoseCheckEnd(Pose pose, CallbackInfoReturnable<Boolean> cir) {
      NoCollideState.INSIDE_PLAYER_POSE_CHECK.set(false);
   }
}
