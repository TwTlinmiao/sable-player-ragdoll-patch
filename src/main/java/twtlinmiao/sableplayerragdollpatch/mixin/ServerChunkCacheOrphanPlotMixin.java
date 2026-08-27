package twtlinmiao.sableplayerragdollpatch.mixin;

import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerChunkCache.class, priority = 1100)
public class ServerChunkCacheOrphanPlotMixin {
    @Inject(method = "blockChanged", at = @At("HEAD"), cancellable = true, remap = false)
    private void spr$ignoreUpdatesInRemovedPlots(BlockPos pos, CallbackInfo ci) {
        ServerChunkCache chunkCache = (ServerChunkCache) (Object) this;
        ServerSubLevelContainer container = SubLevelContainer.getContainer(chunkCache.level);
        if (container != null && container.inBounds(pos)
            && container.getChunkHolder(new ChunkPos(pos)) == null) {
            ci.cancel();
        }
    }
}
