package twtlinmiao.sableplayerragdollpatch.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Server-side configuration for sable-player-ragdoll-patch.
 * Controls gameplay-affecting behavior.
 * Synced from server to client automatically.
 */
public final class RagdollPatchConfig {
    public static final ModConfigSpec SERVER_CONFIG;
    public static ModConfigSpec.BooleanValue GRAB_INTERCEPT_ENABLED;
    public static ModConfigSpec.BooleanValue NO_COLLIDE_DEFAULT;
    public static ModConfigSpec.BooleanValue CORPSE_INTERCEPT_ENABLED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        GRAB_INTERCEPT_ENABLED = builder
            .comment(
                "Cancel container and door interactions",
                "while the player is grabbing a ragdoll part.",
                "Set to false to allow normal container and door interactions while grabbing."
            )
            .define("grabInterceptEnabled", true);

        NO_COLLIDE_DEFAULT = builder
            .comment(
                "Default state of No-Collide mode on server/world load.",
                "When enabled, ragdoll part blocks have no collision,",
                "preventing players and mobs from getting stuck.",
                "Can be toggled at runtime with /nocollide command."
            )
            .define("noCollideDefault", true);

        CORPSE_INTERCEPT_ENABLED = builder
            .comment(
                "When enabled, prevents interacting with a ragdoll corpse",
                "if the player is currently grabbing any part of that ragdoll.",
                "(Only applies when ragdoll-corpse mod is installed)"
            )
            .define("corpseInterceptEnabled", true);

        SERVER_CONFIG = builder.build();
    }

    private RagdollPatchConfig() {}
}
