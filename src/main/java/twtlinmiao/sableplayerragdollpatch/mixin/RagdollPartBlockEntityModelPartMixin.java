package twtlinmiao.sableplayerragdollpatch.mixin;

import com.mojang.authlib.GameProfile;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity.BodyPart;
import twtlinmiao.sableplayerragdollpatch.ModelPartVisibilityAccess;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RagdollPartBlockEntity.class)
public class RagdollPartBlockEntityModelPartMixin implements ModelPartVisibilityAccess {
    @Unique
    private static final String SPR_MODEL_PART_MASK_KEY = "SprModelPartMask";

    @Unique
    private int spr$modelPartMask = -1;

    @Inject(method = "configure(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity$BodyPart;Lnet/minecraft/world/entity/player/Player;)V", at = @At("TAIL"), remap = false)
    private void spr$capturePlayerModelParts(BodyPart bodyPart, Player player, CallbackInfo ci) {
        spr$setModelPartMask(ModelPartVisibilityAccess.captureMask(player));
    }

    @Inject(method = "configure(Ldev/leo/sableplayerragdoll/block/entity/RagdollPartBlockEntity$BodyPart;Lcom/mojang/authlib/GameProfile;)V", at = @At("TAIL"), remap = false)
    private void spr$clearPlayerModelParts(BodyPart bodyPart, GameProfile profile, CallbackInfo ci) {
        this.spr$modelPartMask = -1;
    }

    @Inject(method = "saveAdditional", at = @At("TAIL"), remap = false)
    private void spr$saveModelPartMask(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        if (spr$modelPartMask >= 0) {
            tag.putInt(SPR_MODEL_PART_MASK_KEY, spr$modelPartMask);
        }
    }

    @Inject(method = "loadAdditional", at = @At("TAIL"), remap = false)
    private void spr$loadModelPartMask(CompoundTag tag, HolderLookup.Provider registries, CallbackInfo ci) {
        spr$modelPartMask = tag.contains(SPR_MODEL_PART_MASK_KEY) ? tag.getInt(SPR_MODEL_PART_MASK_KEY) : -1;
    }

    @Override
    public int spr$getModelPartMask() {
        return spr$modelPartMask;
    }

    @Override
    public void spr$setModelPartMask(int mask) {
        this.spr$modelPartMask = mask;
        BlockEntity blockEntity = (BlockEntity) (Object) this;
        blockEntity.setChanged();

        Level level = blockEntity.getLevel();
        if (level != null) {
            level.sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
    }
}
