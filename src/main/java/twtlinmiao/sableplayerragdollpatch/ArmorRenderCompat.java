package twtlinmiao.sableplayerragdollpatch;

import java.lang.reflect.Method;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
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

    private ArmorRenderCompat() {}

    public static ArmorState capture(Player player) {
        ArmorState state = ArmorState.empty();
        COSMETIC_ARMOR_REWORKED.capture(player, state);
        ACCESSORIES.capture(player, state);
        return state;
    }

    public static void apply(ArmorRenderAccess access, ArmorState state) {
        access.spr$setHiddenArmorMask(state.hiddenMask());
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

    public static final class ArmorState {
        private int hiddenMask;
        private final EnumMap<EquipmentSlot, ItemStack> overrides = new EnumMap<>(EquipmentSlot.class);

        private static ArmorState empty() {
            return new ArmorState();
        }

        public int hiddenMask() {
            return hiddenMask;
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
}
