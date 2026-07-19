package twtlinmiao.sableplayerragdollpatch;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.ModList;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchClientConfig;

public final class IrisTransparencyCompat {
    private static final boolean IRIS_LOADED = ModList.get().isLoaded("iris");

    private IrisTransparencyCompat() {}

    public static RenderType ragdollSkinRenderType(ResourceLocation texture) {
        if (IRIS_LOADED && RagdollPatchClientConfig.IRIS_TRANSLUCENT_SKIN_FIX_ENABLED.get()) {
            return RenderType.entityCutoutNoCull(texture);
        }
        return RenderType.entityTranslucent(texture);
    }
}
