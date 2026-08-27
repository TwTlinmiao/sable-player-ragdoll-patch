package twtlinmiao.sableplayerragdollpatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import twtlinmiao.sableplayerragdollpatch.RagdollPlayerState;

/** Prevents Punchy's custom first-person arms from bypassing Sable's ragdoll hand cancellation. */
@Mixin(targets = "punchy.client.render.PunchyArmRenderer", remap = false)
@Pseudo
public class PunchyArmRendererMixin {
    @Inject(method = "renderFirstPerson", at = @At("HEAD"), cancellable = true, remap = false)
    private static void spr$hideArmsWhileRagdolled(
        ItemInHandRenderer handRenderer,
        LocalPlayer player,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        CallbackInfo ci
    ) {
        if (player != null && RagdollPlayerState.isRagdolled(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderFirstPersonInternal", at = @At("HEAD"), cancellable = true, remap = false)
    private static void spr$hideInternalArmsWhileRagdolled(
        ItemInHandRenderer handRenderer,
        LocalPlayer player,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        CallbackInfo ci
    ) {
        if (player != null && RagdollPlayerState.isRagdolled(player)) {
            ci.cancel();
        }
    }

    @Inject(method = "renderPlayerArm", at = @At("HEAD"), cancellable = true, remap = false)
    private static void spr$hidePlayerArmWhileRagdolled(
        AbstractClientPlayer player,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        HumanoidArm arm,
        CallbackInfo ci
    ) {
        if (player != null && RagdollPlayerState.isRagdolled(player)) {
            ci.cancel();
        }
    }
}
