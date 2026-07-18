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

        builder.comment("General settings").push("general");

        GRAB_INTERCEPT_ENABLED = builder
            .comment(
                "Cancel block interactions (opening chests, using buttons, etc.)",
                "while the player is grabbing a ragdoll part.",
                "Set to false to allow normal block interactions while grabbing."
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

        builder.pop().push("corpse");

        CORPSE_INTERCEPT_ENABLED = builder
            .comment(
                "When enabled, prevents interacting with a ragdoll corpse",
                "if the player is currently grabbing any part of that ragdoll.",
                "(Only applies when ragdoll-corpse mod is installed)"
            )
            .define("corpseInterceptEnabled", true);

        builder.pop();
        SERVER_CONFIG = builder.build();
    }

    private RagdollPatchConfig() {}
}
