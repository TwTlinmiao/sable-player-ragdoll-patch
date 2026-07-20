package twtlinmiao.sableplayerragdollpatch.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity.BodyPart;
import dev.leo.sableplayerragdoll.entity.RagdollDollEntity;
import twtlinmiao.sableplayerragdollpatch.DynamicLanternCompat;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchClientConfig;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

@Mixin(targets = "dev.leo.sableplayerragdoll.neoforge.client.CuriosRenderHelper")
public class CuriosRenderHelperMixin {

    private static final Set<String> SPLIT_ARM_SLOTS = Set.of("ring", "hands");

    private CuriosRenderHelperMixin() {}

    private static boolean spr$isSlotForPart(String slotId, int index, BodyPart bodyPart) {
        if ("feet".equals(slotId)) {
            return bodyPart == BodyPart.LEFT_LEG || bodyPart == BodyPart.RIGHT_LEG;
        }
        if (SPLIT_ARM_SLOTS.contains(slotId)) {
            if (bodyPart == BodyPart.LEFT_ARM || bodyPart == BodyPart.RIGHT_ARM) {
                BodyPart expected = (index % 2 == 0) ? BodyPart.RIGHT_ARM : BodyPart.LEFT_ARM;
                return bodyPart == expected;
            }
            return false;
        }
        return switch (slotId) {
            case "head" -> bodyPart == BodyPart.HEAD;
            case "necklace", "back", "belt", "charm", "curio" -> bodyPart == BodyPart.TORSO;
            default -> bodyPart == BodyPart.TORSO;
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void spr$renderStack(
        ItemStack stack,
        SlotContext slotContext,
        RenderLayerParent<RagdollDollEntity, PlayerModel<RagdollDollEntity>> parent,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        float partialTick
    ) {
        if (DynamicLanternCompat.renderRagdollWaistItem(stack, slotContext, parent, poseStack, buffer, packedLight)) {
            return;
        }

        CuriosRendererRegistry.getRenderer(stack.getItem()).ifPresent(renderer -> {
            try {
                ICurioRenderer raw = renderer;
                raw.render(
                    stack, slotContext, poseStack, parent, buffer,
                    packedLight, partialTick,
                    0.0f, 0.0f, 0.0f, 0.0f, 0.0f
                );
            } catch (Exception e) {
                // Swallow rendering errors for individual curio items.
            }
        });
    }

    private static ModelPart spr$oppositeLimb(BodyPart bodyPart, PlayerModel<?> model) {
        return switch (bodyPart) {
            case LEFT_LEG -> model.rightLeg;
            case RIGHT_LEG -> model.leftLeg;
            case LEFT_ARM -> model.rightArm;
            case RIGHT_ARM -> model.leftArm;
            default -> null;
        };
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject(method = "renderFromStored", at = @At("HEAD"), cancellable = true, remap = false)
    private static void spr$renderFromStored(
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
        if (!RagdollPatchClientConfig.CURIOS_FIX_ENABLED.get()) return;

        PlayerModel<RagdollDollEntity> model = parent.getModel();
        ModelPart offLimb = spr$oppositeLimb(bodyPart, model);

        Set<String> slotIds = new LinkedHashSet<>();
        slotIds.addAll(blockEntity.getCurioItems().keySet());
        slotIds.addAll(blockEntity.getCurioCosmeticItems().keySet());

        for (String slotId : slotIds) {
            List<ItemStack> stacks = blockEntity.getCurioItems().getOrDefault(slotId, List.of());
            List<ItemStack> cosmetics = blockEntity.getCurioCosmeticItems().getOrDefault(slotId, List.of());
            int slots = Math.max(stacks.size(), cosmetics.size());
            for (int i = 0; i < slots; i++) {
                if (!spr$isSlotForPart(slotId, i, bodyPart)) continue;

                List<Boolean> options = blockEntity.getCurioRenderOptions().get(slotId);
                boolean shouldRender = options == null || i >= options.size() || Boolean.TRUE.equals(options.get(i));
                if (!shouldRender) continue;

                ItemStack stack;
                if (i < cosmetics.size() && !cosmetics.get(i).isEmpty()) {
                    stack = cosmetics.get(i);
                } else {
                    stack = i < stacks.size() ? stacks.get(i) : ItemStack.EMPTY;
                }
                if (stack.isEmpty()) continue;

                SlotContext slotContext = new SlotContext(slotId, entity, i, false, true);

                float offLimbY = 0.0f;
                if (offLimb != null) {
                    offLimbY = offLimb.y;
                    offLimb.y += 10000.0f;
                }

                spr$renderStack(stack, slotContext, parent, poseStack, buffer, packedLight, partialTick);

                if (offLimb != null) {
                    offLimb.y = offLimbY;
                }
            }
        }
        ci.cancel();
    }

    @SuppressWarnings("unchecked")
    @Inject(method = "render", at = @At("HEAD"), cancellable = true, remap = false)
    private static void spr$render(
        BodyPart bodyPart,
        LivingEntity entity,
        RenderLayerParent<RagdollDollEntity, PlayerModel<RagdollDollEntity>> parent,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        float partialTick,
        CallbackInfo ci
    ) {
        if (!RagdollPatchClientConfig.CURIOS_FIX_ENABLED.get()) return;

        var handler = entity.getCapability(CuriosCapability.INVENTORY);
        if (handler == null) {
            ci.cancel();
            return;
        }

        PlayerModel<RagdollDollEntity> model = parent.getModel();
        ModelPart offLimb = spr$oppositeLimb(bodyPart, model);

        for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
            String slotId = entry.getKey();
            ICurioStacksHandler stacksHandler = entry.getValue();
            var stacks = stacksHandler.getStacks();
            var cosmetics = stacksHandler.getCosmeticStacks();
            var renders = stacksHandler.getRenders();

            for (int i = 0; i < stacks.getSlots(); i++) {
                if (!spr$isSlotForPart(slotId, i, bodyPart)) continue;
                if (!renders.get(i)) continue;

                ItemStack cosmetic = i < cosmetics.getSlots() ? cosmetics.getStackInSlot(i) : ItemStack.EMPTY;
                ItemStack stack = !cosmetic.isEmpty() ? cosmetic : stacks.getStackInSlot(i);
                if (stack.isEmpty()) continue;

                SlotContext slotContext = new SlotContext(slotId, entity, i, false, true);

                float offLimbY = 0.0f;
                if (offLimb != null) {
                    offLimbY = offLimb.y;
                    offLimb.y += 10000.0f;
                }

                spr$renderStack(stack, slotContext, parent, poseStack, buffer, packedLight, partialTick);

                if (offLimb != null) {
                    offLimb.y = offLimbY;
                }
            }
        }
        ci.cancel();
    }
}
