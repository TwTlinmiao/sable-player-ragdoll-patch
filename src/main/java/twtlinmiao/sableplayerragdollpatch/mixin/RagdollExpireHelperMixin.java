package twtlinmiao.sableplayerragdollpatch.mixin;

import dev.leo.sableplayerragdoll.physics.RagdollExpireHelper;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RagdollExpireHelper.class, remap = false)
public class RagdollExpireHelperMixin {
   @Redirect(
      method = "expire",
      at = @At(
         value = "INVOKE",
         target = "Ldev/leo/sableplayerragdoll/physics/RagdollExpireHelper;unseatRider(Lnet/minecraft/server/level/ServerLevel;Ldev/ryanhcode/sable/sublevel/ServerSubLevel;)V"
      ),
      remap = false
   )
   private static void spr$releasePlayerAtRagdollPosition(ServerLevel level, ServerSubLevel subLevel) {
      spr$unseatRider(level, subLevel, true);
   }

   @Invoker(value = "unseatRider", remap = false)
   public static void spr$unseatRider(ServerLevel level, ServerSubLevel subLevel, boolean placePlayerAtRagdoll) {
      throw new AssertionError();
   }
}
