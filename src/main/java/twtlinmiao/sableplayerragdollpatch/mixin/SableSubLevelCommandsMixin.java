package twtlinmiao.sableplayerragdollpatch.mixin;

import dev.leo.sableplayerragdoll.physics.RagdollAssemblyHelper;
import dev.leo.sableplayerragdoll.physics.RagdollExpireHelper;
import dev.leo.sableplayerragdoll.physics.RagdollSessionManager;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.command.SableSubLevelCommands;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.storage.SubLevelRemovalReason;
import java.util.UUID;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SableSubLevelCommands.class, remap = false)
public class SableSubLevelCommandsMixin {
    @Redirect(
        method = "executeRemoveSubLevelCommand",
        at = @At(
            value = "INVOKE",
            target = "Ldev/ryanhcode/sable/api/sublevel/SubLevelContainer;removeSubLevel(IILdev/ryanhcode/sable/sublevel/storage/SubLevelRemovalReason;)V"
        )
    )
    private static void spr$releaseRagdollBeforeCommandRemoval(
        SubLevelContainer container,
        int plotX,
        int plotZ,
        SubLevelRemovalReason reason
    ) {
        SubLevel selected = container.getSubLevel(plotX, plotZ);
        if (!(container instanceof ServerSubLevelContainer serverContainer)
            || !(selected instanceof ServerSubLevel selectedServer)
            || !RagdollAssemblyHelper.isRagdollPart(selectedServer.getUniqueId())) {
            if (selected != null) {
                container.removeSubLevel(plotX, plotZ, reason);
            }
            return;
        }

        UUID rootId = RagdollAssemblyHelper.linkedRoot(selectedServer.getUniqueId());
        SubLevel root = rootId == null ? selectedServer : container.getSubLevel(rootId);
        if (!(root instanceof ServerSubLevel ragdollRoot) || !RagdollSessionManager.isMarkedRagdoll(ragdollRoot)) {
            container.removeSubLevel(plotX, plotZ, reason);
            return;
        }

        RagdollExpireHelper.expireImmediate(
            serverContainer.physicsSystem(),
            serverContainer.getLevel(),
            ragdollRoot,
            "sable remove command",
            true
        );
    }
}
