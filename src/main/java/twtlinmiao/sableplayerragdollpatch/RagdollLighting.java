package twtlinmiao.sableplayerragdollpatch;

import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Vector3d;

public final class RagdollLighting {
   private RagdollLighting() {
   }

   public static int worldLightFor(BlockEntity blockEntity, float partialTick, int fallbackLight) {
      if (blockEntity == null || blockEntity.getLevel() == null) {
         return fallbackLight;
      }

      ClientSubLevel subLevel = Sable.HELPER.getContainingClient(blockEntity);
      if (subLevel == null) {
         return fallbackLight;
      }

      BlockPos blockPos = blockEntity.getBlockPos();
      Vector3d localCenter = new Vector3d(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
      Vector3d worldCenter = subLevel.renderPose(partialTick).transformPosition(localCenter, new Vector3d());
      BlockPos worldPos = BlockPos.containing(worldCenter.x, worldCenter.y, worldCenter.z);

      return LevelRenderer.getLightColor(blockEntity.getLevel(), worldPos);
   }
}
