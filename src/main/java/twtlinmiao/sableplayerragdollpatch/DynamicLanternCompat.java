package twtlinmiao.sableplayerragdollpatch;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.leo.sableplayerragdoll.entity.RagdollDollEntity;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import top.theillusivec4.curios.api.SlotContext;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchClientConfig;

public final class DynamicLanternCompat {
   private static boolean resolved;
   private static boolean available;
   private static Field renderWaistLantern;
   private static Method booleanValueGet;
   private static Method isRenderableWaistItem;
   private static Method getWaistAnchor;
   private static Method swingPivotY;
   private static Method applyModelOffset;
   private static Method attachToBody;
   private static Method renderModel;

   private DynamicLanternCompat() {
   }

   public static boolean renderRagdollWaistItem(
      ItemStack stack,
      SlotContext slotContext,
      RenderLayerParent<RagdollDollEntity, PlayerModel<RagdollDollEntity>> parent,
      PoseStack poseStack,
      MultiBufferSource buffer,
      int packedLight
   ) {
      if (!RagdollPatchClientConfig.WAIST_LANTERN_COMPAT_ENABLED.get()
         || stack.isEmpty()
         || slotContext.entity() instanceof Player
         || !resolve()) return false;

      try {
         if (!isWaistRenderingEnabled() || !(boolean) isRenderableWaistItem.invoke(null, stack)) {
            return false;
         }

         PlayerModel<RagdollDollEntity> model = parent.getModel();
         LivingEntity entity = slotContext.entity();

         poseStack.pushPose();
         Vec3 anchor = (Vec3) getWaistAnchor.invoke(null, hasTorsoOrLegArmor(entity));
         attachToBody.invoke(null, poseStack, model.body, anchor);

         float pivotY = (float) swingPivotY.invoke(null, stack);
         poseStack.translate(0.5F, pivotY, 0.5F);
         poseStack.translate(-0.5F, -pivotY, -0.5F);
         applyModelOffset.invoke(null, stack, poseStack);
         renderModel.invoke(null, stack, poseStack, buffer, packedLight);
         poseStack.popPose();
         return true;
      } catch (ReflectiveOperationException | RuntimeException exception) {
         SablePlayerRagdollPatch.LOGGER.warn("Failed to render Dynamic Lantern waist item on ragdoll", exception);
         return false;
      }
   }

   public static boolean isRenderableWaistItem(ItemStack stack) {
      if (stack.isEmpty() || !resolve()) return false;

      try {
         return (boolean) isRenderableWaistItem.invoke(null, stack);
      } catch (ReflectiveOperationException | RuntimeException exception) {
         SablePlayerRagdollPatch.LOGGER.warn("Failed to inspect Dynamic Lantern waist item", exception);
         return false;
      }
   }

   private static boolean resolve() {
      if (resolved) return available;
      resolved = true;

      try {
         Class<?> configClass = Class.forName("org.com.dynamiclantern.Config");
         Class<?> rulesClass = Class.forName("org.com.dynamiclantern.WaistItemRules");
         Class<?> rendererClass = Class.forName("org.com.dynamiclantern.client.CurioWaistItemRenderer");
         Class<?> modelRendererClass = Class.forName("org.com.dynamiclantern.client.WaistItemModelRenderer");

         renderWaistLantern = configClass.getField("RENDER_WAIST_LANTERN");
         Object booleanValue = renderWaistLantern.get(null);
         booleanValueGet = booleanValue.getClass().getMethod("get");
         isRenderableWaistItem = rulesClass.getMethod("isRenderableWaistItem", ItemStack.class);
         getWaistAnchor = rendererClass.getDeclaredMethod("getWaistAnchor", boolean.class);
         swingPivotY = rendererClass.getDeclaredMethod("swingPivotY", ItemStack.class);
         applyModelOffset = rendererClass.getDeclaredMethod("applyModelOffset", ItemStack.class, PoseStack.class);
         attachToBody = rendererClass.getDeclaredMethod("attachToBody", PoseStack.class, ModelPart.class, Vec3.class);
         renderModel = modelRendererClass.getDeclaredMethod("render", ItemStack.class, PoseStack.class, MultiBufferSource.class, int.class);

         getWaistAnchor.setAccessible(true);
         swingPivotY.setAccessible(true);
         applyModelOffset.setAccessible(true);
         attachToBody.setAccessible(true);
         renderModel.setAccessible(true);

         available = true;
      } catch (ReflectiveOperationException | LinkageError ignored) {
         available = false;
      }

      return available;
   }

   private static boolean isWaistRenderingEnabled() throws ReflectiveOperationException {
      Object value = booleanValueGet.invoke(renderWaistLantern.get(null));
      return value instanceof Boolean enabled && enabled;
   }

   private static boolean hasTorsoOrLegArmor(LivingEntity entity) {
      return hasArmorInSlot(entity.getItemBySlot(EquipmentSlot.CHEST), EquipmentSlot.CHEST)
         || hasArmorInSlot(entity.getItemBySlot(EquipmentSlot.LEGS), EquipmentSlot.LEGS);
   }

   private static boolean hasArmorInSlot(ItemStack stack, EquipmentSlot slot) {
      return stack.getItem() instanceof ArmorItem armorItem && armorItem.getEquipmentSlot() == slot;
   }
}
