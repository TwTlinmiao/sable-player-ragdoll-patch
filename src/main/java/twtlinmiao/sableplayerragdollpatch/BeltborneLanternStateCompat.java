package twtlinmiao.sableplayerragdollpatch;

import java.lang.reflect.Method;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

public final class BeltborneLanternStateCompat {
    private static boolean resolved;
    private static boolean available;
    private static Method isLampStack;
    private static Method getStateItem;
    private static Method getLampStackPlayer;
    private static Method getLuminanceItem;

    private BeltborneLanternStateCompat() {}

    public static ItemStack capture(Player player) {
        if (player == null || !resolve()) return ItemStack.EMPTY;

        try {
            Object stack = getLampStackPlayer.invoke(null, player);
            if (stack instanceof ItemStack itemStack && !itemStack.isEmpty()) {
                return itemStack.copy();
            }
        } catch (ReflectiveOperationException | RuntimeException exception) {
            SablePlayerRagdollPatch.LOGGER.warn("Failed to capture Beltborne lantern stack", exception);
        }

        return ItemStack.EMPTY;
    }

    public static boolean isLamp(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !resolve()) return false;

        try {
            return Boolean.TRUE.equals(isLampStack.invoke(null, stack));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            SablePlayerRagdollPatch.LOGGER.warn("Failed to inspect Beltborne lantern stack", exception);
            return false;
        }
    }

    public static int luminance(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !resolve()) return 0;

        try {
            if (!Boolean.TRUE.equals(isLampStack.invoke(null, stack))) return 0;
            Object value = getLuminanceItem.invoke(null, stack.getItem());
            return value instanceof Integer luminance ? luminance : 0;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            SablePlayerRagdollPatch.LOGGER.warn("Failed to read Beltborne lantern luminance", exception);
            return 0;
        }
    }

    public static net.minecraft.world.level.block.state.BlockState state(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !resolve()) return null;

        try {
            if (!Boolean.TRUE.equals(isLampStack.invoke(null, stack))) return null;
            Object value = getStateItem.invoke(null, stack.getItem());
            return value instanceof net.minecraft.world.level.block.state.BlockState blockState ? blockState : null;
        } catch (ReflectiveOperationException | RuntimeException exception) {
            SablePlayerRagdollPatch.LOGGER.warn("Failed to resolve Beltborne lantern block state", exception);
            return null;
        }
    }

    private static boolean resolve() {
        if (resolved) return available;
        resolved = true;

        try {
            Class<?> lampRegistry = Class.forName("net.oxcodsnet.beltborne_lanterns.common.LampRegistry");
            Class<?> beltState = Class.forName("net.oxcodsnet.beltborne_lanterns.common.BeltState");
            isLampStack = lampRegistry.getMethod("isLamp", ItemStack.class);
            getStateItem = lampRegistry.getMethod("getState", Item.class);
            getLuminanceItem = lampRegistry.getMethod("getLuminance", Item.class);
            getLampStackPlayer = beltState.getMethod("getLampStack", Player.class);
            available = true;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            available = false;
        }

        return available;
    }
}
