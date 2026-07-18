package twtlinmiao.sableplayerragdollpatch.mixin;

import dev.leo.ragdollcorpse.corpse.CorpseInteractHandler;
import dev.leo.sableplayerragdoll.api.RagdollInteractEvent;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import dev.leo.sableplayerragdoll.physics.RagdollAssemblyHelper;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchConfig;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CorpseInteractHandler.class)
public class CorpseInteractHandlerMixin {

   @Inject(method = "onRagdollInteract", at = @At("HEAD"), cancellable = true, remap = false)
   private static void onInteractHead(RagdollInteractEvent event, CallbackInfo ci) {
      if (!RagdollPatchConfig.CORPSE_INTERCEPT_ENABLED.get()) return;

      UUID rootId = event.rootId();
      if (!rootId.equals(event.partId())) return;

      if (!isGrabbingAnyPart(event.level(), rootId, event.player())) return;

      event.setCanceled(true);
      ci.cancel();
   }

   private static boolean isGrabbingAnyPart(ServerLevel level, UUID rootId, ServerPlayer player) {
      List<UUID> partIds = RagdollAssemblyHelper.linkedParts(rootId);
      SubLevelContainer container = SubLevelContainer.getContainer(level);
      if (container == null) return false;

      UUID playerId = player.getUUID();
      for (UUID partId : partIds) {
         SubLevel subLevel = container.getSubLevel(partId);
         if (!(subLevel instanceof ServerSubLevel serverSubLevel) || serverSubLevel.isRemoved()) continue;
         BlockPos blockPos = serverSubLevel.getPlot().getCenterBlock();
         BlockEntity blockEntity = serverSubLevel.getLevel().getBlockEntity(blockPos);
         if (blockEntity instanceof RagdollPartBlockEntity part) {
            if (isGrabbedBy(part, playerId)) return true;
         }
      }
      return false;
   }

   private static boolean isGrabbedBy(RagdollPartBlockEntity part, UUID playerId) {
      return part.isGrabbedBy(playerId);
   }
}
