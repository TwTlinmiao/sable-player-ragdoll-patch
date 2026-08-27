package twtlinmiao.sableplayerragdollpatch.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnderpearlItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WindChargeItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import twtlinmiao.sableplayerragdollpatch.RagdollPlayerState;

@Mixin({EnderpearlItem.class, WindChargeItem.class})
public class RagdollProjectileItemMixin {
   @Inject(method = "use", at = @At("HEAD"), cancellable = true, remap = false)
   private void spr$blockUseWhileRagdolled(
         Level level,
         Player player,
         InteractionHand hand,
         CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
      if (RagdollPlayerState.isRagdolled(player)) {
         cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(hand)));
      }
   }
}
