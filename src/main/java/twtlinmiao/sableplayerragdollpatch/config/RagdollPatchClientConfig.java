package twtlinmiao.sableplayerragdollpatch.config;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Client-side configuration for sable-player-ragdoll-patch.
 * Controls rendering fixes that only run on the client.
 * Not synced; each client controls its own settings.
 */
public final class RagdollPatchClientConfig {
    public static final ModConfigSpec CLIENT_CONFIG;
    public static ModConfigSpec.BooleanValue RAGDOLL_DYNAMIC_LIGHTS_ENABLED;
    public static ModConfigSpec.BooleanValue COSMETIC_ARMOR_COMPAT_ENABLED;
    public static ModConfigSpec.BooleanValue WAIST_LANTERN_COMPAT_ENABLED;
    public static ModConfigSpec.BooleanValue HIDE_CORPSE_CAPE_ENABLED;
    public static ModConfigSpec.BooleanValue LEAWIND_CAMERA_COMPAT_ENABLED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        RAGDOLL_DYNAMIC_LIGHTS_ENABLED = builder
            .comment(
                "When enabled, ragdoll hands holding luminous items act as dynamic light sources",
                "for Sodium/Embeddium Dynamic Lights and LambDynamicLights.",
                "Also brightens the ragdoll model itself to match the held light."
            )
            .define("ragdollDynamicLightsEnabled", true);

        COSMETIC_ARMOR_COMPAT_ENABLED = builder
            .comment(
                "Apply hidden armor and cosmetic armor overrides to ragdoll corpses.",
                "Supports Cosmetic Armor Reworked and Accessories armor visibility/alternative stacks.",
                "Disable to render the armor stored directly on the ragdoll without cosmetic overrides."
            )
            .define("cosmeticArmorCompatEnabled", true);

        WAIST_LANTERN_COMPAT_ENABLED = builder
            .comment(
                "Render waist lantern items on ragdoll corpses.",
                "Controls both Dynamic Lantern waist item rendering and Beltborne Lanterns/Accessories Layer lamps.",
                "Also lets supported waist lanterns contribute to ragdoll dynamic lights."
            )
            .define("waistLanternCompatEnabled", true);

        HIDE_CORPSE_CAPE_ENABLED = builder
            .comment(
                "Hide capes on ragdoll corpses even when the player had cape rendering enabled.",
                "Other skin customization parts, such as hats, sleeves, jacket and pants, still follow",
                "the player's settings captured at death. Disable to sync cape visibility too."
            )
            .define("hideCorpseCapeEnabled", true);

        LEAWIND_CAMERA_COMPAT_ENABLED = builder
            .comment(
                "Disable Leawind's Third Person camera while the local player is self-ragdolled.",
                "When enabled, falling down and standing up are handled by Sable's own camera",
                "to avoid Leawind snapping the camera back during self-ragdoll recovery."
            )
            .define("leawindCameraCompatEnabled", true);

        CLIENT_CONFIG = builder.build();
    }

    private RagdollPatchClientConfig() {}
}
