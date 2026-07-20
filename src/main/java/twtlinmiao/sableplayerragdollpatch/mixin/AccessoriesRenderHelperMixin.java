package twtlinmiao.sableplayerragdollpatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity.BodyPart;
import dev.leo.sableplayerragdoll.entity.RagdollDollEntity;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twtlinmiao.sableplayerragdollpatch.BeltborneLanternRenderCompat;

@Mixin(targets = "dev.leo.sableplayerragdoll.neoforge.client.AccessoriesRenderHelper")
public class AccessoriesRenderHelperMixin {
    private AccessoriesRenderHelperMixin() {}

    @Inject(method = "renderFromStored", at = @At("HEAD"), remap = false)
    private static void spr$renderBeltborneLanternFromStored(
        BodyPart bodyPart,
        RagdollPartBlockEntity blockEntity,
        LivingEntity entity,
        RenderLayerParent<RagdollDollEntity, PlayerModel<RagdollDollEntity>> parent,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        float partialTick,
        CallbackInfo ci
    ) {
        if (bodyPart != BodyPart.TORSO) return;

        BeltborneLanternRenderCompat.renderStoredAccessoriesLantern(bodyPart, blockEntity, parent, poseStack, buffer, packedLight);
    }
}
