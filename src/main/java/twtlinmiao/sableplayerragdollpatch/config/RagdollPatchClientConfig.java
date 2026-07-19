package twtlinmiao.sableplayerragdollpatch.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-side configuration for sable-player-ragdoll-patch.
 * Controls rendering fixes that only run on the client.
 * Not synced; each client controls its own settings.
 */
public final class RagdollPatchClientConfig {
    public static final ModConfigSpec CLIENT_CONFIG;
    public static ModConfigSpec.BooleanValue CURIOS_FIX_ENABLED;
    public static ModConfigSpec.BooleanValue IRIS_TRANSLUCENT_SKIN_FIX_ENABLED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment("Rendering fixes for sable-player-ragdoll-patch")
               .push("rendering");

        CURIOS_FIX_ENABLED = builder
            .comment(
                "Fix Curio rendering on ragdoll part block entities:",
                "  - Feet slot maps to both legs (was: only right leg)",
                "  - Rings/hands distributed across arms by slot index parity",
                "  - Opposite limb hidden during render to prevent double-rendering",
                "Disable if you do not use Curios and want the original behavior."
            )
            .define("curiosFixEnabled", true);

        IRIS_TRANSLUCENT_SKIN_FIX_ENABLED = builder
            .comment(
                "When Iris is installed, render ragdoll part player skins with the",
                "vanilla PlayerModel cutout render type instead of Sable's translucent",
                "render type. This avoids Iris translucent-entity sorting hiding",
                "rear faces on Curios/transparent cosmetics."
            )
            .define("irisTranslucentSkinFixEnabled", true);

        builder.pop();
        CLIENT_CONFIG = builder.build();
    }

    private RagdollPatchClientConfig() {}
}
