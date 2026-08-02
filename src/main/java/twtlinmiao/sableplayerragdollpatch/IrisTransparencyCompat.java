package twtlinmiao.sableplayerragdollpatch;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public final class IrisTransparencyCompat {
    private IrisTransparencyCompat() {}

    public static RenderType ragdollSkinRenderType(ResourceLocation texture) {
        return RenderType.entityTranslucent(texture);
    }
}
