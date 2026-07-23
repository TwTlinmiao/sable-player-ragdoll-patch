package twtlinmiao.sableplayerragdollpatch.mixin;

import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "team.teampotato.ruok.util.entity.block.BlockEntityUtil", remap = false)
public class RuokBlockEntityUtilMixin {
   @Inject(method = "getVisibleBlockEntitiesSet", at = @At("RETURN"), cancellable = true, require = 0)
   private static void spr$keepRagdollsVisible(CallbackInfoReturnable<Set<BlockEntity>> cir) {
      Set<BlockEntity> visible = cir.getReturnValue();
      if (visible instanceof RagdollVisibleSet) return;

      cir.setReturnValue(new RagdollVisibleSet(visible));
   }

   private static final class RagdollVisibleSet extends AbstractSet<BlockEntity> {
      private final Set<BlockEntity> delegate;

      private RagdollVisibleSet(Set<BlockEntity> delegate) {
         this.delegate = delegate == null ? Set.of() : delegate;
      }

      @Override
      public boolean contains(Object object) {
         return object instanceof RagdollPartBlockEntity || this.delegate.contains(object);
      }

      @Override
      public Iterator<BlockEntity> iterator() {
         return this.delegate.iterator();
      }

      @Override
      public int size() {
         return this.delegate.size();
      }
   }
}
