package twtlinmiao.sableplayerragdollpatch;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity.BodyPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class ArmorRenderCompat {
    public static final EquipmentSlot[] ARMOR_SLOTS = {
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    };

    private static final Map<EquipmentSlot, Integer> CAR_SLOT_INDEX = Map.of(
        EquipmentSlot.FEET, 0,
        EquipmentSlot.LEGS, 1,
        EquipmentSlot.CHEST, 2,
        EquipmentSlot.HEAD, 3
    );

    private static final CosmeticArmorReworked COSMETIC_ARMOR_REWORKED = new CosmeticArmorReworked();
    private static final Accessories ACCESSORIES = new Accessories();
    private static final ArmourersWorkshop ARMOURERS_WORKSHOP = new ArmourersWorkshop();

    private ArmorRenderCompat() {}

    public static ArmorState capture(Player player) {
        ArmorState state = ArmorState.empty();
        COSMETIC_ARMOR_REWORKED.capture(player, state);
        ACCESSORIES.capture(player, state);
        ARMOURERS_WORKSHOP.capture(player, state);
        return state;
    }

    public static void apply(ArmorRenderAccess access, ArmorState state) {
        access.spr$setHiddenArmorMask(state.hiddenMask());
        access.spr$setHiddenBodyMask(state.hiddenBodyMask());
        access.spr$setSkinlessArmorMask(state.skinlessMask());
        access.spr$clearArmorRenderOverrides();
        for (Map.Entry<EquipmentSlot, ItemStack> entry : state.overrides().entrySet()) {
            access.spr$setArmorRenderOverride(entry.getKey(), entry.getValue());
        }
    }

    public static int slotMask(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> 1;
            case CHEST -> 1 << 1;
            case LEGS -> 1 << 2;
            case FEET -> 1 << 3;
            default -> 0;
        };
    }

    public static boolean bodyPartCoveredByMask(BodyPart bodyPart, int hiddenBodyMask) {
        return switch (bodyPart) {
            case HEAD -> (hiddenBodyMask & slotMask(EquipmentSlot.HEAD)) != 0;
            case TORSO, LEFT_ARM, RIGHT_ARM -> (hiddenBodyMask & slotMask(EquipmentSlot.CHEST)) != 0;
            case LEFT_LEG, RIGHT_LEG -> (hiddenBodyMask & slotMask(EquipmentSlot.LEGS)) != 0;
        };
    }

    public static ItemStack withoutArmourersWorkshopSkin(ItemStack stack) {
        try {
            return ArmourersWorkshopCompat.withoutSkin(stack);
        } catch (LinkageError ignored) {
            return stack == null ? ItemStack.EMPTY : stack.copy();
        }
    }

    public static final class ArmorState {
        private int hiddenMask;
        private int hiddenBodyMask;
        private int skinlessMask;
        private final EnumMap<EquipmentSlot, ItemStack> overrides = new EnumMap<>(EquipmentSlot.class);

        private static ArmorState empty() {
            return new ArmorState();
        }

        public int hiddenMask() {
            return hiddenMask;
        }

        public int hiddenBodyMask() {
            return hiddenBodyMask;
        }

        public int skinlessMask() {
            return skinlessMask;
        }

        public Map<EquipmentSlot, ItemStack> overrides() {
            return Map.copyOf(overrides);
        }

        private void hide(EquipmentSlot slot) {
            int mask = slotMask(slot);
            if (mask == 0) return;
            hiddenMask |= mask;
            overrides.remove(slot);
        }

        private void hideBody(EquipmentSlot slot) {
            int mask = slotMask(slot);
            if (mask == 0) return;
            hiddenBodyMask |= mask;
        }

        private void skinless(EquipmentSlot slot) {
            int mask = slotMask(slot);
            if (mask == 0) return;
            skinlessMask |= mask;
        }

        private void override(EquipmentSlot slot, ItemStack stack) {
            if ((hiddenMask & slotMask(slot)) != 0 || stack == null || stack.isEmpty()) return;
            overrides.put(slot, stack.copy());
        }
    }

    private static final class CosmeticArmorReworked {
        private boolean resolved;
        private Method getStacks;
        private Method getStackInSlot;
        private Method isSkinArmor;

        private void capture(Player player, ArmorState state) {
            try {
                if (!resolve()) return;

                Object stacks = getStacks.invoke(null, player.getUUID());
                if (stacks == null) {
                    stacks = getStacks.invoke(null, new UUID(0L, 0L));
                }
                if (stacks == null) return;

                for (EquipmentSlot slot : ARMOR_SLOTS) {
                    Integer index = CAR_SLOT_INDEX.get(slot);
                    if (index == null) continue;

                    if (Boolean.TRUE.equals(isSkinArmor.invoke(stacks, index))) {
                        state.hide(slot);
                        continue;
                    }

                    Object stack = getStackInSlot.invoke(stacks, index);
                    if (stack instanceof ItemStack itemStack && !itemStack.isEmpty()) {
                        state.override(slot, itemStack);
                    }
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Optional compatibility: absent or mismatched Cosmetic Armor Reworked APIs are ignored.
            }
        }

        private boolean resolve() throws ReflectiveOperationException {
            if (resolved) return getStacks != null;
            resolved = true;

            Class<?> api = Class.forName("lain.mods.cos.api.CosArmorAPI");
            Class<?> stacks = Class.forName("lain.mods.cos.api.inventory.CAStacksBase");
            getStacks = api.getMethod("getCAStacks", UUID.class);
            getStackInSlot = stacks.getMethod("getStackInSlot", int.class);
            isSkinArmor = stacks.getMethod("isSkinArmor", int.class);
            return true;
        }
    }

    private static final class Accessories {
        private boolean resolved;
        private Method getAlternativeStack;

        private void capture(Player player, ArmorState state) {
            try {
                if (!resolve()) return;

                for (EquipmentSlot slot : ARMOR_SLOTS) {
                    Object alternative = getAlternativeStack.invoke(null, player, slot);
                    if (alternative == null) continue;
                    if (alternative instanceof ItemStack itemStack) {
                        if (itemStack.isEmpty()) {
                            state.hide(slot);
                        } else {
                            state.override(slot, itemStack);
                        }
                    }
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Optional compatibility: absent or mismatched Accessories APIs are ignored.
            }
        }

        private boolean resolve() throws ReflectiveOperationException {
            if (resolved) return getAlternativeStack != null;
            resolved = true;

            Class<?> armorSlotTypes = Class.forName("io.wispforest.accessories.menu.ArmorSlotTypes");
            getAlternativeStack = armorSlotTypes.getMethod("getAlternativeStack", LivingEntity.class, EquipmentSlot.class);
            return true;
        }
    }

    private static final class ArmourersWorkshop {
        private void capture(Player player, ArmorState state) {
            try {
                if (!ArmourersWorkshopCompat.resolve()) return;

                ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
                ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                ItemStack legs = player.getItemBySlot(EquipmentSlot.LEGS);
                ItemStack feet = player.getItemBySlot(EquipmentSlot.FEET);

                boolean headSkin = ArmourersWorkshopCompat.isSlot(head, "HEAD");
                boolean chestSkin = ArmourersWorkshopCompat.isSlot(chest, "CHEST");
                boolean legsSkin = ArmourersWorkshopCompat.isSlot(legs, "LEGS");
                boolean feetSkin = ArmourersWorkshopCompat.isSlot(feet, "FEET");
                boolean outfit = ArmourersWorkshopCompat.isSlot(head, "OUTFIT")
                    || ArmourersWorkshopCompat.isSlot(chest, "OUTFIT")
                    || ArmourersWorkshopCompat.isSlot(legs, "OUTFIT")
                    || ArmourersWorkshopCompat.isSlot(feet, "OUTFIT");

                if (outfit) {
                    state.hideBody(EquipmentSlot.HEAD);
                    state.hideBody(EquipmentSlot.CHEST);
                    state.hideBody(EquipmentSlot.LEGS);
                    return;
                }
                if (headSkin) {
                    state.hideBody(EquipmentSlot.HEAD);
                }
                if (chestSkin) {
                    state.hideBody(EquipmentSlot.CHEST);
                }
                if (legsSkin && feetSkin) {
                    state.hideBody(EquipmentSlot.LEGS);
                } else if (legsSkin || feetSkin) {
                    if (legsSkin) state.skinless(EquipmentSlot.LEGS);
                    if (feetSkin) state.skinless(EquipmentSlot.FEET);
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                // Optional compatibility: absent or mismatched Armourer's Workshop APIs are ignored.
            }
        }
    }

    private static final class ArmourersWorkshopCompat {
        private static boolean resolved;
        private static Method descriptorOf;
        private static Method descriptorIsEmpty;
        private static Method descriptorType;
        private static Method slotByType;
        private static Method componentHolderGet;
        private static Method componentRemove;
        private static Object skinComponentHolder;

        private static boolean resolve() throws ReflectiveOperationException {
            if (resolved) return descriptorOf != null;
            resolved = true;

            Class<?> skinDescriptor = Class.forName("moe.plushie.armourers_workshop.core.skin.SkinDescriptor");
            Class<?> skinType = Class.forName("moe.plushie.armourers_workshop.core.skin.SkinType");
            Class<?> skinSlotType = Class.forName("moe.plushie.armourers_workshop.core.menu.SkinSlotType");
            Class<?> modDataComponents = Class.forName("moe.plushie.armourers_workshop.init.ModDataComponents");

            descriptorOf = skinDescriptor.getMethod("of", ItemStack.class);
            descriptorIsEmpty = skinDescriptor.getMethod("isEmpty");
            descriptorType = skinDescriptor.getMethod("type");
            slotByType = skinSlotType.getMethod("byType", skinType);
            skinComponentHolder = modDataComponents.getField("SKIN").get(null);
            componentHolderGet = skinComponentHolder.getClass().getMethod("get");
            Object component = componentHolderGet.invoke(skinComponentHolder);
            componentRemove = component.getClass().getMethod("remove", ItemStack.class);
            return true;
        }

        private static boolean isSlot(ItemStack stack, String expectedSlot) throws ReflectiveOperationException {
            if (stack == null || stack.isEmpty() || !resolve()) return false;
            Object descriptor = descriptorOf.invoke(null, stack);
            if (descriptor == null || Boolean.TRUE.equals(descriptorIsEmpty.invoke(descriptor))) return false;
            Object type = descriptorType.invoke(descriptor);
            if (type == null) return false;
            Object slot = slotByType.invoke(null, type);
            return slot instanceof Enum<?> enumSlot && enumSlot.name().equals(expectedSlot);
        }

        static ItemStack withoutSkin(ItemStack stack) {
            if (stack == null || stack.isEmpty()) return ItemStack.EMPTY;
            ItemStack copy = stack.copy();
            try {
                if (resolve()) {
                    Object component = componentHolderGet.invoke(skinComponentHolder);
                    componentRemove.invoke(component, copy);
                }
            } catch (ReflectiveOperationException | LinkageError ignored) {
                return copy;
            }
            return copy;
        }
    }
}
