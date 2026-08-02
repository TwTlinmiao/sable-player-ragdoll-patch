package twtlinmiao.sableplayerragdollpatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity.BodyPart;
import dev.leo.sableplayerragdoll.entity.RagdollDollEntity;
import dev.leo.sableplayerragdoll.neoforge.client.RagdollPartBlockEntityRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import twtlinmiao.sableplayerragdollpatch.IrisTransparencyCompat;
import twtlinmiao.sableplayerragdollpatch.ModelPartVisibilityAccess;
import twtlinmiao.sableplayerragdollpatch.RagdollLighting;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import twtlinmiao.sableplayerragdollpatch.BeltborneLanternAccess;
import twtlinmiao.sableplayerragdollpatch.BeltborneLanternRenderCompat;
import twtlinmiao.sableplayerragdollpatch.ArmorRenderAccess;
import twtlinmiao.sableplayerragdollpatch.ArmorRenderCompat;
import twtlinmiao.sableplayerragdollpatch.DynamicLightsCompat;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchClientConfig;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RagdollPartBlockEntityRenderer.class)
public class RagdollPartBlockEntityRendererMixin {
    @Shadow(remap = false)
    private PlayerModel<?> model;

    @Shadow(remap = false)
    private void renderLayers(RagdollPartBlockEntity blockEntity, BodyPart bodyPart, LivingEntity entity, PoseStack poseStack, MultiBufferSource buffer, int packedLight, float partialTick) {
        throw new AssertionError();
    }

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

    @Redirect(
        method = "renderLayers",
        at = @At(
            value = "INVOKE",
            target = "Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity;itemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"
        ),
        remap = false
    )
    private ItemStack spr$useArmorRenderOverride(RagdollPartBlockEntity blockEntity, EquipmentSlot slot) {
        ItemStack original = blockEntity.itemBySlot(slot);
        if (!RagdollPatchClientConfig.COSMETIC_ARMOR_COMPAT_ENABLED.get()) {
            return original;
        }

        ArmorRenderAccess access = (ArmorRenderAccess) (Object) blockEntity;

        if (access.spr$isArmorSlotHidden(slot)) {
            return ItemStack.EMPTY;
        }

        ItemStack override = access.spr$getArmorRenderOverride(slot);
        ItemStack stack = override.isEmpty() ? original : override;
        if ((access.spr$getSkinlessArmorMask() & ArmorRenderCompat.slotMask(slot)) != 0) {
            return ArmorRenderCompat.withoutArmourersWorkshopSkin(stack);
        }
        return stack;
    }

    @Redirect(
        method = "renderLayers",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"
        ),
        remap = false
    )
    private void spr$skipArmorLayerForAwCoveredBody(
        RenderLayer layer,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        Entity entity,
        float limbSwing,
        float limbSwingAmount,
        float partialTick,
        float ageInTicks,
        float netHeadYaw,
        float headPitch,
        RagdollPartBlockEntity blockEntity,
        BodyPart bodyPart,
        LivingEntity renderEntity,
        PoseStack originalPoseStack,
        MultiBufferSource originalBuffer,
        int originalPackedLight,
        float originalPartialTick
    ) {
        if (RagdollPatchClientConfig.COSMETIC_ARMOR_COMPAT_ENABLED.get()
            && spr$isVanillaArmorLayer(layer)
            && ArmorRenderCompat.bodyPartCoveredByMask(bodyPart, ((ArmorRenderAccess) (Object) blockEntity).spr$getHiddenBodyMask())) {
            return;
        }

        layer.render(poseStack, buffer, packedLight, entity, limbSwing, limbSwingAmount, partialTick, ageInTicks, netHeadYaw, headPitch);
    }

    @Redirect(
        method = "render(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At(
            value = "INVOKE",
            target = "Ldev/leo/sableplayerragdoll/neoforge/client/RagdollPartBlockEntityRenderer;renderLayers(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity;Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity$BodyPart;Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V"
        ),
        remap = false
    )
    private void spr$renderLayersWithWorldLight(
        RagdollPartBlockEntityRenderer renderer,
        RagdollPartBlockEntity blockEntity,
        BodyPart bodyPart,
        LivingEntity entity,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        float partialTick
    ) {
        this.renderLayers(blockEntity, bodyPart, entity, poseStack, buffer, RagdollLighting.worldLightFor(blockEntity, partialTick, packedLight), partialTick);
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
        ArmorRenderAccess armorAccess = (ArmorRenderAccess) (Object) blockEntity;
        if (RagdollPatchClientConfig.COSMETIC_ARMOR_COMPAT_ENABLED.get()
            && ArmorRenderCompat.bodyPartCoveredByMask(blockEntity.bodyPart(), armorAccess.spr$getHiddenBodyMask())) {
            spr$hideCurrentBodyPart(blockEntity.bodyPart());
            return;
        }

        if (!visibility.spr$hasModelPartMask()) return;

        model.hat.visible &= visibility.spr$isModelPartShown(PlayerModelPart.HAT);
        model.jacket.visible &= visibility.spr$isModelPartShown(PlayerModelPart.JACKET);
        model.leftSleeve.visible &= visibility.spr$isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        model.rightSleeve.visible &= visibility.spr$isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        model.leftPants.visible &= visibility.spr$isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        model.rightPants.visible &= visibility.spr$isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
    }

    private static boolean spr$isVanillaArmorLayer(RenderLayer layer) {
        return layer != null && layer.getClass().getName().equals("net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer");
    }

    private void spr$hideCurrentBodyPart(BodyPart bodyPart) {
        switch (bodyPart) {
            case HEAD -> {
                model.head.visible = false;
                model.hat.visible = false;
            }
            case TORSO -> {
                model.body.visible = false;
                model.jacket.visible = false;
            }
            case LEFT_ARM -> {
                model.leftArm.visible = false;
                model.leftSleeve.visible = false;
            }
            case RIGHT_ARM -> {
                model.rightArm.visible = false;
                model.rightSleeve.visible = false;
            }
            case LEFT_LEG -> {
                model.leftLeg.visible = false;
                model.leftPants.visible = false;
            }
            case RIGHT_LEG -> {
                model.rightLeg.visible = false;
                model.rightPants.visible = false;
            }
        }
    }

    @Inject(
        method = "render(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At(
            value = "INVOKE",
            target = "Ldev/leo/sableplayerragdoll/neoforge/client/RagdollPartBlockEntityRenderer;renderLayers(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity;Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity$BodyPart;Lnet/minecraft/world/entity/LivingEntity;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IF)V",
            shift = At.Shift.AFTER
        ),
        remap = false
    )
    private void spr$renderStoredBeltborneLantern(
        RagdollPartBlockEntity blockEntity,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        int packedOverlay,
        CallbackInfo ci
    ) {
        BodyPart bodyPart = blockEntity.bodyPart();
        if (bodyPart != BodyPart.TORSO) return;

        BeltborneLanternAccess access = (BeltborneLanternAccess) (Object) blockEntity;
        ItemStack stack = access.spr$getBeltborneLanternStack();
        if (stack.isEmpty()) return;
        if (BeltborneLanternRenderCompat.hasStoredAccessoriesLantern(blockEntity, bodyPart)) return;

        BeltborneLanternRenderCompat.renderBeltStateLantern(
            stack,
            (RenderLayerParent<RagdollDollEntity, PlayerModel<RagdollDollEntity>>) (Object) this,
            poseStack,
            buffer,
            RagdollLighting.worldLightFor(blockEntity, partialTick, packedLight)
        );
    }

    @Inject(
        method = "render(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
        at = @At("HEAD"),
        remap = false
    )
    private void spr$updateDynamicLightSource(
        RagdollPartBlockEntity blockEntity,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        int packedOverlay,
        CallbackInfo ci
    ) {
        DynamicLightsCompat.updateRenderedPart(blockEntity, partialTick);
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
        if (blockEntity.isCorpse() && RagdollPatchClientConfig.HIDE_CORPSE_CAPE_ENABLED.get()) {
            ci.cancel();
            return;
        }

        if (visibility.spr$hasModelPartMask() && !visibility.spr$isModelPartShown(PlayerModelPart.CAPE)) {
            ci.cancel();
        }
    }
}
