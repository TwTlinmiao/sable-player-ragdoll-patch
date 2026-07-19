package twtlinmiao.sableplayerragdollpatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.leo.sableplayerragdoll.mob.block.entity.MobRagdollPartBlockEntity;
import dev.leo.sableplayerragdoll.mob.client.MobRagdollPartBlockEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twtlinmiao.sableplayerragdollpatch.RagdollLighting;

@Mixin(value = MobRagdollPartBlockEntityRenderer.class)
public class MobRagdollPartBlockEntityRendererMixin {
   @Unique
   private int spr$packedLight;

   @Inject(
       method = "render(Ldev/leo/sableplayerragdoll/mob/block/entity/MobRagdollPartBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
       at = @At("HEAD"),
       remap = false
   )
   private void spr$captureWorldLightForSubLevelMobPart(
       MobRagdollPartBlockEntity blockEntity,
       float partialTick,
       PoseStack poseStack,
       MultiBufferSource bufferSource,
       int packedLight,
       int packedOverlay,
       CallbackInfo ci
   ) {
      this.spr$packedLight = RagdollLighting.worldLightFor(blockEntity, partialTick, packedLight);
   }

   @ModifyArg(
       method = "render(Ldev/leo/sableplayerragdoll/mob/block/entity/MobRagdollPartBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
       at = @At(
           value = "INVOKE",
           target = "Ldev/leo/sableplayerragdoll/mob/client/MobRagdollPartBlockEntityRenderer;renderReplay(Ldev/leo/sableplayerragdoll/mob/block/entity/MobRagdollPartBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)Z"
       ),
       index = 4,
       remap = false
   )
   private int spr$renderReplayWithWorldLight(int packedLight) {
      return this.spr$packedLight;
   }

   @ModifyArg(
       method = "render(Ldev/leo/sableplayerragdoll/mob/block/entity/MobRagdollPartBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
       at = @At(
           value = "INVOKE",
           target = "Ldev/leo/sableplayerragdoll/mob/client/MobRagdollQuadRenderer;render(Ldev/leo/sableplayerragdoll/mob/block/entity/MobRagdollPartBlockEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
       ),
       index = 3,
       remap = false
   )
   private int spr$renderQuadsWithWorldLight(int packedLight) {
      return this.spr$packedLight;
   }

   @ModifyArg(
       method = "render(Ldev/leo/sableplayerragdoll/mob/block/entity/MobRagdollPartBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
       at = @At(
           value = "INVOKE",
           target = "Lnet/minecraft/client/renderer/block/BlockRenderDispatcher;renderSingleBlock(Lnet/minecraft/world/level/block/state/BlockState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V"
       ),
       index = 3,
       remap = false
   )
   private int spr$renderFallbackBlockWithWorldLight(int packedLight) {
      return this.spr$packedLight;
   }
}
