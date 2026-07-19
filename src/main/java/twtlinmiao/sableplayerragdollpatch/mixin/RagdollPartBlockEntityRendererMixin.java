package twtlinmiao.sableplayerragdollpatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import dev.leo.sableplayerragdoll.neoforge.client.RagdollPartBlockEntityRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import twtlinmiao.sableplayerragdollpatch.IrisTransparencyCompat;
import twtlinmiao.sableplayerragdollpatch.ModelPartVisibilityAccess;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RagdollPartBlockEntityRenderer.class)
public class RagdollPartBlockEntityRendererMixin {
    @Shadow(remap = false)
    private PlayerModel<?> model;

    @Redirect(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/blockentity/BlockEntityRendererProvider$Context;bakeLayer(Lnet/minecraft/client/model/geom/ModelLayerLocation;)Lnet/minecraft/client/model/geom/ModelPart;"
        ),
        remap = false
    )
    private ModelPart spr$redirectBakeLayer(BlockEntityRendererProvider.Context context, ModelLayerLocation layer) {
        boolean slim = layer == ModelLayers.PLAYER_SLIM;
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, slim);
        return mesh.getRoot().bake(64, 64);
    }

    @Redirect(
        method = "renderLayers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/RenderType;entityTranslucent(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"
        ),
        remap = false
    )
    private RenderType spr$irisSafeSkinRenderType(ResourceLocation texture) {
        return IrisTransparencyCompat.ragdollSkinRenderType(texture);
    }

    @Inject(
        method = "render(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At(
            value = "INVOKE",
            target = "Ldev/leo/sableplayerragdoll/neoforge/client/RagdollPartBlockEntityRenderer;showOnly(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity$BodyPart;)V",
            shift = At.Shift.AFTER
        ),
        remap = false
    )
    private void spr$applyStoredModelPartVisibility(
        RagdollPartBlockEntity blockEntity,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        int packedOverlay,
        CallbackInfo ci
    ) {
        ModelPartVisibilityAccess visibility = (ModelPartVisibilityAccess) (Object) blockEntity;
        if (!visibility.spr$hasModelPartMask()) return;

        model.hat.visible &= visibility.spr$isModelPartShown(PlayerModelPart.HAT);
        model.jacket.visible &= visibility.spr$isModelPartShown(PlayerModelPart.JACKET);
        model.leftSleeve.visible &= visibility.spr$isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        model.rightSleeve.visible &= visibility.spr$isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        model.leftPants.visible &= visibility.spr$isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        model.rightPants.visible &= visibility.spr$isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
    }

    @Inject(
        method = "renderCape",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void spr$hideCapeWhenDisabled(
        RagdollPartBlockEntity blockEntity,
        LivingEntity entity,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        CallbackInfo ci
    ) {
        ModelPartVisibilityAccess visibility = (ModelPartVisibilityAccess) (Object) blockEntity;
        if (visibility.spr$hasModelPartMask() && !visibility.spr$isModelPartShown(PlayerModelPart.CAPE)) {
            ci.cancel();
        }
    }
}
