package twtlinmiao.sableplayerragdollpatch;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;

public final class IrisTransparencyCompat {
    private static final boolean IRIS_LOADED = ModList.get().isLoaded("iris");

    private IrisTransparencyCompat() {}

    public static RenderType ragdollSkinRenderType(ResourceLocation texture) {
        if (IRIS_LOADED) {
            return RenderType.entityCutoutNoCull(texture);
        }
        return RenderType.entityTranslucent(texture);
    }
}
