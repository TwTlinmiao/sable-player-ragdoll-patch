package twtlinmiao.sableplayerragdollpatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.leo.sableplayerragdoll.entity.RagdollDollEntity;
import dev.leo.sableplayerragdoll.neoforge.client.RagdollDollEntityRenderer;
import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RagdollDollEntityRenderer.class)
public class RagdollDollEntityRendererMixin {

   @Inject(
       method = "render(Ldev/leo/sableplayerragdoll/entity/RagdollDollEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
       at = @At(
           value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/LivingEntityRenderer;render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
        ),
       remap = false
   )
   private void spr$fixCosmeticsBeforeSuperRender(RagdollDollEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
      this.applySkinSettings(entity.getBodyPart());
   }

   private void applySkinSettings(RagdollDollEntity.BodyPart bodyPart) {
      PlayerModel<?> model = this.resolveModel();
      if (model == null) return;
      switch (bodyPart) {
         case HEAD -> model.hat.visible = Minecraft.getInstance().options.isModelPartEnabled(PlayerModelPart.HAT);
         case LEFT_ARM -> model.leftSleeve.visible = Minecraft.getInstance().options.isModelPartEnabled(PlayerModelPart.LEFT_SLEEVE);
         case RIGHT_ARM -> model.rightSleeve.visible = Minecraft.getInstance().options.isModelPartEnabled(PlayerModelPart.RIGHT_SLEEVE);
         case LEFT_LEG -> model.leftPants.visible = Minecraft.getInstance().options.isModelPartEnabled(PlayerModelPart.LEFT_PANTS_LEG);
         case RIGHT_LEG -> model.rightPants.visible = Minecraft.getInstance().options.isModelPartEnabled(PlayerModelPart.RIGHT_PANTS_LEG);
         default -> model.jacket.visible = Minecraft.getInstance().options.isModelPartEnabled(PlayerModelPart.JACKET);
      }
   }

   private PlayerModel<?> resolveModel() {
      Class<?> cls = this.getClass();
      while (cls != null) {
         try {
            Field f = cls.getDeclaredField("model");
            f.setAccessible(true);
            return (PlayerModel<?>) f.get(this);
         } catch (NoSuchFieldException e) {
            cls = cls.getSuperclass();
         } catch (Exception e) {
            return null;
         }
      }
      return null;
   }
}
