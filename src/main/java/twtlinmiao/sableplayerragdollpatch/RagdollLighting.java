package twtlinmiao.sableplayerragdollpatch;

import dev.ryanhcode.sable.Sable;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Vector3d;

public final class RagdollLighting {
   private static final int LIGHT_SAMPLE_HEIGHT = 3;
   private static final int BLOCK_LIGHT_SHIFT = 4;
   private static final int SKY_LIGHT_SHIFT = 20;
   private static final int LIGHT_MASK = 15;

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

      int light = fallbackLight;
      for (int yOffset = 0; yOffset <= LIGHT_SAMPLE_HEIGHT; yOffset++) {
         light = brightest(light, LevelRenderer.getLightColor(blockEntity.getLevel(), worldPos.above(yOffset)));
      }

      if (blockEntity instanceof RagdollPartBlockEntity ragdollPart) {
         light = withBlockLight(light, DynamicLightsCompat.modelLuminance(ragdollPart));
      }

      return light;
   }

   private static int brightest(int firstPackedLight, int secondPackedLight) {
      int blockLight = Math.max(component(firstPackedLight, BLOCK_LIGHT_SHIFT), component(secondPackedLight, BLOCK_LIGHT_SHIFT));
      int skyLight = Math.max(component(firstPackedLight, SKY_LIGHT_SHIFT), component(secondPackedLight, SKY_LIGHT_SHIFT));
      return pack(blockLight, skyLight);
   }

   private static int component(int packedLight, int shift) {
      return packedLight >> shift & LIGHT_MASK;
   }

   private static int pack(int blockLight, int skyLight) {
      return blockLight << BLOCK_LIGHT_SHIFT | skyLight << SKY_LIGHT_SHIFT;
   }

   private static int withBlockLight(int packedLight, int blockLight) {
      int brighterBlockLight = Math.max(component(packedLight, BLOCK_LIGHT_SHIFT), blockLight);
      return pack(brighterBlockLight, component(packedLight, SKY_LIGHT_SHIFT));
   }
}
