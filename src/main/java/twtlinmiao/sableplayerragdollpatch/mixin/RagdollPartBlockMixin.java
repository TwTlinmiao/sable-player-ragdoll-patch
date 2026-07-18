package twtlinmiao.sableplayerragdollpatch.mixin;

import twtlinmiao.sableplayerragdollpatch.NoCollideState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {
    "dev.leo.sableplayerragdoll.block.RagdollPartBlock",
    "dev.leo.sableplayerragdoll.mob.block.MobRagdollPartBlock"
})
public class RagdollPartBlockMixin {

   @Inject(method = "getCollisionShape", at = @At("HEAD"), cancellable = true, remap = false)
   private void sprOnGetCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context, CallbackInfoReturnable<VoxelShape> cir) {
      if (NoCollideState.isEnabled() && Boolean.TRUE.equals(NoCollideState.INSIDE_ENTITY_MOVE.get())) {
         cir.setReturnValue(Shapes.empty());
      }
   }
}
