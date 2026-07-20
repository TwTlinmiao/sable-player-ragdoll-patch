package twtlinmiao.sableplayerragdollpatch;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity.BodyPart;
import dev.leo.sableplayerragdoll.entity.RagdollDollEntity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchClientConfig;

public final class BeltborneLanternRenderCompat {
    private static boolean resolved;
    private static boolean available;
    private static Method configGet;
    private static Method offsetX;
    private static Method offsetY;
    private static Method offsetZ;
    private static Method pivotX;
    private static Method pivotY;
    private static Method pivotZ;
    private static Method scale;
    private static Field rotXDeg;
    private static Field rotYDeg;
    private static Field rotZDeg;
    private static Method renderSingleBlock;

    private BeltborneLanternRenderCompat() {}

    public static boolean renderStoredAccessoriesLantern(
        BodyPart bodyPart,
        RagdollPartBlockEntity blockEntity,
        RenderLayerParent<RagdollDollEntity, PlayerModel<RagdollDollEntity>> parent,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight
    ) {
        if (!isEnabled() || bodyPart != BodyPart.TORSO || blockEntity == null || !resolve()) return false;

        ItemStack stack = findFirstStoredLamp(blockEntity, bodyPart);
        if (stack.isEmpty()) return false;

        return renderStack(stack, parent, poseStack, buffer, packedLight);
    }

    public static boolean hasStoredAccessoriesLantern(RagdollPartBlockEntity blockEntity, BodyPart bodyPart) {
        if (!isEnabled()) return false;
        return !findFirstStoredLamp(blockEntity, bodyPart).isEmpty();
    }

    public static boolean renderBeltStateLantern(
        ItemStack stack,
        RenderLayerParent<RagdollDollEntity, PlayerModel<RagdollDollEntity>> parent,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight
    ) {
        if (!isEnabled()) return false;
        return renderStack(stack, parent, poseStack, buffer, packedLight);
    }

    public static boolean isEnabled() {
        return RagdollPatchClientConfig.WAIST_LANTERN_COMPAT_ENABLED.get();
    }

    private static boolean renderStack(
        ItemStack stack,
        RenderLayerParent<RagdollDollEntity, PlayerModel<RagdollDollEntity>> parent,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight
    ) {
        if (stack.isEmpty() || !resolve() || !BeltborneLanternStateCompat.isLamp(stack)) return false;

        boolean pushed = false;
        try {
            Object config = configGet.invoke(null);
            if (config == null) return false;

            var model = parent.getModel();
            var body = model.body;

            poseStack.pushPose();
            pushed = true;
            body.translateAndRotate(poseStack);

            float fOffsetX = (float) offsetX.invoke(config);
            float fOffsetY = (float) offsetY.invoke(config);
            float fOffsetZ = (float) offsetZ.invoke(config);
            float fPivotX = (float) pivotX.invoke(config);
            float fPivotY = (float) pivotY.invoke(config);
            float fPivotZ = (float) pivotZ.invoke(config);
            float scaleValue = (float) scale.invoke(config);

            poseStack.translate(fOffsetX, fOffsetY, fOffsetZ);
            poseStack.translate(fPivotX, fPivotY, fPivotZ);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            poseStack.scale(scaleValue, scaleValue, scaleValue);

            int rotX = rotXDeg.getInt(config);
            int rotY = rotYDeg.getInt(config);
            int rotZ = rotZDeg.getInt(config);
            if (rotX != 0) {
                poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
            }
            if (rotY != 0) {
                poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            }
            if (rotZ != 0) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));
            }
            poseStack.translate(-fPivotX, -fPivotY, -fPivotZ);

            Object state = BeltborneLanternStateCompat.state(stack);
            if (state == null) return false;

            renderSingleBlock.invoke(
                Minecraft.getInstance().getBlockRenderer(),
                state,
                poseStack,
                buffer,
                packedLight,
                OverlayTexture.NO_OVERLAY
            );
            return true;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            SablePlayerRagdollPatch.LOGGER.warn("Failed to render Beltborne lantern on ragdoll", exception);
            return false;
        } finally {
            if (pushed) {
                poseStack.popPose();
            }
        }
    }

    private static ItemStack findFirstStoredLamp(RagdollPartBlockEntity blockEntity, BodyPart bodyPart) {
        Set<String> slotIds = new LinkedHashSet<>();
        slotIds.addAll(blockEntity.getAccessoriesItems().keySet());
        slotIds.addAll(blockEntity.getAccessoriesCosmeticItems().keySet());

        for (String slotId : slotIds) {
            if (!isSlotForPart(slotId, bodyPart)) continue;

            var stacks = blockEntity.getAccessoriesItems().getOrDefault(slotId, java.util.List.of());
            var cosmetics = blockEntity.getAccessoriesCosmeticItems().getOrDefault(slotId, java.util.List.of());
            var renderOptions = blockEntity.getAccessoriesRenderOptions().get(slotId);
            int slots = Math.max(stacks.size(), cosmetics.size());

            for (int index = 0; index < slots; index++) {
                if (!shouldRender(renderOptions, index)) continue;

                ItemStack stack = selectedStack(stacks, cosmetics, index);
                if (BeltborneLanternStateCompat.isLamp(stack)) {
                    return stack;
                }
            }
        }

        return ItemStack.EMPTY;
    }

    private static ItemStack selectedStack(java.util.List<ItemStack> stacks, java.util.List<ItemStack> cosmetics, int index) {
        if (index < cosmetics.size() && !cosmetics.get(index).isEmpty()) {
            return cosmetics.get(index);
        }
        return index < stacks.size() ? stacks.get(index) : ItemStack.EMPTY;
    }

    private static boolean shouldRender(java.util.List<Boolean> renderOptions, int index) {
        return renderOptions == null || index >= renderOptions.size() || Boolean.TRUE.equals(renderOptions.get(index));
    }

    private static boolean isSlotForPart(String slotId, BodyPart bodyPart) {
        if (slotId == null) return false;
        if ("feet".equals(slotId) || "legs".equals(slotId)) {
            return bodyPart == BodyPart.LEFT_LEG || bodyPart == BodyPart.RIGHT_LEG;
        }
        if ("ring".equals(slotId) || "hands".equals(slotId)) {
            return bodyPart == BodyPart.LEFT_ARM || bodyPart == BodyPart.RIGHT_ARM;
        }
        return switch (slotId) {
            case "head", "hat", "face" -> bodyPart == BodyPart.HEAD;
            case "necklace", "back", "belt", "charm", "curio", "chest", "body", "waist" -> bodyPart == BodyPart.TORSO;
            default -> bodyPart == BodyPart.TORSO;
        };
    }

    private static boolean resolve() {
        if (resolved) return available;
        resolved = true;

        try {
            Class<?> configClass = Class.forName("net.oxcodsnet.beltborne_lanterns.common.config.BLConfigs");
            Class<?> configType = Class.forName("net.oxcodsnet.beltborne_lanterns.common.config.BLConfig");
            configGet = configClass.getMethod("get");
            offsetX = configType.getMethod("fOffsetX");
            offsetY = configType.getMethod("fOffsetY");
            offsetZ = configType.getMethod("fOffsetZ");
            pivotX = configType.getMethod("fPivotX");
            pivotY = configType.getMethod("fPivotY");
            pivotZ = configType.getMethod("fPivotZ");
            scale = configType.getMethod("fScale");
            rotXDeg = configType.getField("rotXDeg");
            rotYDeg = configType.getField("rotYDeg");
            rotZDeg = configType.getField("rotZDeg");
            renderSingleBlock = Minecraft.getInstance().getBlockRenderer().getClass().getMethod(
                "renderSingleBlock",
                Class.forName("net.minecraft.world.level.block.state.BlockState"),
                PoseStack.class,
                MultiBufferSource.class,
                int.class,
                int.class
            );
            available = true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            available = false;
        }

        return available;
    }
}
