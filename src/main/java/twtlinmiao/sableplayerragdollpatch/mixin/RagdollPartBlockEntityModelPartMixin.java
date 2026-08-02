package twtlinmiao.sableplayerragdollpatch.mixin;

import com.mojang.authlib.GameProfile;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity.BodyPart;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import twtlinmiao.sableplayerragdollpatch.BeltborneLanternAccess;
import twtlinmiao.sableplayerragdollpatch.BeltborneLanternStateCompat;
import twtlinmiao.sableplayerragdollpatch.ArmorRenderAccess;
import twtlinmiao.sableplayerragdollpatch.ArmorRenderCompat;
import twtlinmiao.sableplayerragdollpatch.ModelPartVisibilityAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RagdollPartBlockEntity.class)
public class RagdollPartBlockEntityModelPartMixin implements ModelPartVisibilityAccess, ArmorRenderAccess, BeltborneLanternAccess {
    @Unique
    private static final String SPR_MODEL_PART_MASK_KEY = "SprModelPartMask";
    @Unique
    private static final String SPR_HIDDEN_ARMOR_MASK_KEY = "SprHiddenArmorMask";
    @Unique
    private static final String SPR_HIDDEN_BODY_MASK_KEY = "SprHiddenBodyMask";
    @Unique
    private static final String SPR_SKINLESS_ARMOR_MASK_KEY = "SprSkinlessArmorMask";
    @Unique
    private static final String SPR_ARMOR_OVERRIDES_KEY = "SprArmorOverrides";
    @Unique
    private static final String SPR_ARMOR_SLOT_KEY = "Slot";
    @Unique
    private static final String SPR_ARMOR_ITEM_KEY = "Item";
    @Unique
    private static final String SPR_BELTBORNE_LANTERN_KEY = "SprBeltborneLantern";

    @Unique
    private int spr$modelPartMask = -1;
    @Unique
    private int spr$hiddenArmorMask = 0;
    @Unique
    private int spr$hiddenBodyMask = 0;
    @Unique
    private int spr$skinlessArmorMask = 0;
    @Unique
    private final EnumMap<EquipmentSlot, ItemStack> spr$armorRenderOverrides = new EnumMap<>(EquipmentSlot.class);
    @Unique
    private ItemStack spr$beltborneLanternStack = ItemStack.EMPTY;

    @Inject(method = "configure(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity$BodyPart;Lnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"), remap = false)
    private void spr$capturePlayerModelParts(BodyPart bodyPart, Player player, CallbackInfo ci) {
        spr$setModelPartMask(ModelPartVisibilityAccess.captureMask(player));
        ArmorRenderCompat.apply(this, ArmorRenderCompat.capture(player));
        spr$setBeltborneLanternStack(BeltborneLanternStateCompat.capture(player));
    }

    @Inject(method = "configure(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity$BodyPart;Lcom/mojang/authlib/GameProfile;)V", at = @At("TAIL"), remap = false)
    private void spr$clearPlayerModelParts(BodyPart bodyPart, GameProfile profile, CallbackInfo ci) {
        this.spr$modelPartMask = -1;
        this.spr$hiddenArmorMask = 0;
        this.spr$hiddenBodyMask = 0;
        this.spr$skinlessArmorMask = 0;
        this.spr$armorRenderOverrides.clear();
        this.spr$beltborneLanternStack = ItemStack.EMPTY;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"), remap = false)
    private void spr$saveModelPartMask(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (spr$modelPartMask >= 0) {
            tag.putInt(SPR_MODEL_PART_MASK_KEY, spr$modelPartMask);
        }
        if (spr$hiddenArmorMask != 0) {
            tag.putInt(SPR_HIDDEN_ARMOR_MASK_KEY, spr$hiddenArmorMask);
        }
        if (spr$hiddenBodyMask != 0) {
            tag.putInt(SPR_HIDDEN_BODY_MASK_KEY, spr$hiddenBodyMask);
        }
        if (spr$skinlessArmorMask != 0) {
            tag.putInt(SPR_SKINLESS_ARMOR_MASK_KEY, spr$skinlessArmorMask);
        }
        if (!spr$armorRenderOverrides.isEmpty()) {
            tag.put(SPR_ARMOR_OVERRIDES_KEY, spr$saveArmorOverrides(registries));
        }
        if (!spr$beltborneLanternStack.isEmpty()) {
            tag.put(SPR_BELTBORNE_LANTERN_KEY, spr$beltborneLanternStack.save(registries));
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"), remap = false)
    private void spr$loadModelPartMask(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        spr$modelPartMask = tag.contains(SPR_MODEL_PART_MASK_KEY) ? tag.getInt(SPR_MODEL_PART_MASK_KEY) : -1;
        spr$hiddenArmorMask = tag.contains(SPR_HIDDEN_ARMOR_MASK_KEY) ? tag.getInt(SPR_HIDDEN_ARMOR_MASK_KEY) : 0;
        spr$hiddenBodyMask = tag.contains(SPR_HIDDEN_BODY_MASK_KEY) ? tag.getInt(SPR_HIDDEN_BODY_MASK_KEY) : 0;
        spr$skinlessArmorMask = tag.contains(SPR_SKINLESS_ARMOR_MASK_KEY) ? tag.getInt(SPR_SKINLESS_ARMOR_MASK_KEY) : 0;
        spr$loadArmorOverrides(tag, registries);
        spr$beltborneLanternStack = tag.contains(SPR_BELTBORNE_LANTERN_KEY)
            ? ItemStack.parse(registries, tag.get(SPR_BELTBORNE_LANTERN_KEY)).orElse(ItemStack.EMPTY)
            : ItemStack.EMPTY;
    }

    @Override
    public int spr$getModelPartMask() {
        return spr$modelPartMask;
    }

    @Override
    public void spr$setModelPartMask(int mask) {
        this.spr$modelPartMask = mask;
        spr$syncArmorState();
    }

    @Override
    public int spr$getHiddenArmorMask() {
        return spr$hiddenArmorMask;
    }

    @Override
    public void spr$setHiddenArmorMask(int mask) {
        this.spr$hiddenArmorMask = mask;
        spr$syncArmorState();
    }

    @Override
    public int spr$getHiddenBodyMask() {
        return spr$hiddenBodyMask;
    }

    @Override
    public void spr$setHiddenBodyMask(int mask) {
        this.spr$hiddenBodyMask = mask;
        spr$syncArmorState();
    }

    @Override
    public int spr$getSkinlessArmorMask() {
        return spr$skinlessArmorMask;
    }

    @Override
    public void spr$setSkinlessArmorMask(int mask) {
        this.spr$skinlessArmorMask = mask;
        spr$syncArmorState();
    }

    @Override
    public ItemStack spr$getArmorRenderOverride(EquipmentSlot slot) {
        return spr$armorRenderOverrides.getOrDefault(slot, ItemStack.EMPTY);
    }

    @Override
    public void spr$setArmorRenderOverride(EquipmentSlot slot, ItemStack stack) {
        if (ArmorRenderCompat.slotMask(slot) == 0) return;
        if (stack == null || stack.isEmpty()) {
            spr$armorRenderOverrides.remove(slot);
        } else {
            spr$armorRenderOverrides.put(slot, stack.copy());
        }
        spr$syncArmorState();
    }

    @Override
    public void spr$clearArmorRenderOverrides() {
        spr$armorRenderOverrides.clear();
        spr$syncArmorState();
    }

    @Override
    public ItemStack spr$getBeltborneLanternStack() {
        return spr$beltborneLanternStack.copy();
    }

    @Override
    public void spr$setBeltborneLanternStack(ItemStack stack) {
        this.spr$beltborneLanternStack = stack == null ? ItemStack.EMPTY : stack.copy();
        spr$syncArmorState();
    }

    @Unique
    private ListTag spr$saveArmorOverrides(HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (Map.Entry<EquipmentSlot, ItemStack> entry : spr$armorRenderOverrides.entrySet()) {
            if (entry.getValue().isEmpty()) continue;

            CompoundTag slotTag = new CompoundTag();
            slotTag.putString(SPR_ARMOR_SLOT_KEY, entry.getKey().name());
            slotTag.put(SPR_ARMOR_ITEM_KEY, entry.getValue().save(registries));
            list.add(slotTag);
        }
        return list;
    }

    @Unique
    private void spr$loadArmorOverrides(CompoundTag tag, HolderLookup.Provider registries) {
        spr$armorRenderOverrides.clear();
        if (!tag.contains(SPR_ARMOR_OVERRIDES_KEY, Tag.TAG_LIST)) return;

        ListTag list = tag.getList(SPR_ARMOR_OVERRIDES_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag slotTag = list.getCompound(i);
            try {
                EquipmentSlot slot = EquipmentSlot.valueOf(slotTag.getString(SPR_ARMOR_SLOT_KEY));
                ItemStack stack = ItemStack.parse(registries, slotTag.get(SPR_ARMOR_ITEM_KEY)).orElse(ItemStack.EMPTY);
                if (ArmorRenderCompat.slotMask(slot) != 0 && !stack.isEmpty()) {
                    spr$armorRenderOverrides.put(slot, stack);
                }
            } catch (IllegalArgumentException ignored) {
                // Ignore unknown slots from future versions.
            }
        }
    }

    @Unique
    private void spr$syncArmorState() {
        BlockEntity blockEntity = (BlockEntity) (Object) this;
        blockEntity.setChanged();

        Level level = blockEntity.getLevel();
        if (level != null) {
            level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
    }
}
