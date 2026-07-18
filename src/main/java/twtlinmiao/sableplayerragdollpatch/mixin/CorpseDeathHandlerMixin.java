package twtlinmiao.sableplayerragdollpatch.mixin;

import dev.leo.ragdollcorpse.corpse.CorpseDeathHandler;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import dev.leo.sableplayerragdoll.physics.RagdollAssemblyHelper;
import twtlinmiao.sableplayerragdollpatch.ModelPartVisibilityAccess;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CorpseDeathHandler.class)
public class CorpseDeathHandlerMixin {
    @Inject(method = "onPlayerDeath", at = @At("TAIL"), remap = false)
    private static void spr$copyModelPartVisibility(LivingDeathEvent event, CallbackInfo ci) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID rootId = CorpseDeathHandler.peekPendingCorpse(player.getUUID());
        if (rootId == null) return;

        int mask = ModelPartVisibilityAccess.captureMask(player);
        copyMaskToParts(player.serverLevel(), rootId, mask);
    }

    private static void copyMaskToParts(ServerLevel level, UUID rootId, int mask) {
        List<UUID> partIds = RagdollAssemblyHelper.linkedParts(rootId);
        SubLevelContainer container = SubLevelContainer.getContainer(level);
        if (container == null) return;

        for (UUID partId : partIds) {
            SubLevel subLevel = container.getSubLevel(partId);
            if (!(subLevel instanceof ServerSubLevel serverSubLevel) || serverSubLevel.isRemoved()) continue;

            BlockPos blockPos = serverSubLevel.getPlot().getCenterBlock();
            BlockEntity blockEntity = serverSubLevel.getLevel().getBlockEntity(blockPos);
            if (blockEntity instanceof RagdollPartBlockEntity part) {
                ((ModelPartVisibilityAccess) (Object) part).spr$setModelPartMask(mask);
            }
        }
    }
}
