package twtlinmiao.sableplayerragdollpatch.mixin;

import twtlinmiao.sableplayerragdollpatch.NoCollideState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMoveMixin {
   @Inject(method = "move", at = @At("HEAD"), remap = false)
   private void sprOnMoveStart(MoverType type, Vec3 pos, CallbackInfo ci) {
      NoCollideState.INSIDE_ENTITY_MOVE.set(true);
   }

   @Inject(method = "move", at = @At("RETURN"), remap = false)
   private void sprOnMoveEnd(MoverType type, Vec3 pos, CallbackInfo ci) {
      NoCollideState.INSIDE_ENTITY_MOVE.set(false);
   }
}
