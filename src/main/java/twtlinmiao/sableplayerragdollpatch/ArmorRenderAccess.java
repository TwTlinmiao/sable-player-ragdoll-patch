package twtlinmiao.sableplayerragdollpatch;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public interface ArmorRenderAccess {
    int spr$getHiddenArmorMask();

    void spr$setHiddenArmorMask(int mask);

    ItemStack spr$getArmorRenderOverride(EquipmentSlot slot);

    void spr$setArmorRenderOverride(EquipmentSlot slot, ItemStack stack);

    void spr$clearArmorRenderOverrides();

    default boolean spr$isArmorSlotHidden(EquipmentSlot slot) {
        return (spr$getHiddenArmorMask() & ArmorRenderCompat.slotMask(slot)) != 0;
    }
}
