package twtlinmiao.sableplayerragdollpatch;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;

public interface ModelPartVisibilityAccess {
    int spr$getModelPartMask();

    void spr$setModelPartMask(int mask);

    default boolean spr$hasModelPartMask() {
        return spr$getModelPartMask() >= 0;
    }

    default boolean spr$isModelPartShown(PlayerModelPart part) {
        int mask = spr$getModelPartMask();
        return mask < 0 || (mask & part.getMask()) != 0;
    }

    static int captureMask(Player player) {
        int mask = 0;
        for (PlayerModelPart part : PlayerModelPart.values()) {
            if (player.isModelPartShown(part)) {
                mask |= part.getMask();
            }
        }
        return mask;
    }
}
