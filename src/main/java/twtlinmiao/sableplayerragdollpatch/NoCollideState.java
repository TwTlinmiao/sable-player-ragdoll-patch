package twtlinmiao.sableplayerragdollpatch;

import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchConfig;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * State holder for the no-collide feature.
 * Prevents entities from colliding with ragdoll part blocks
 * so that players/mobs don't get stuck or pushed by ragdolls.
 * Lazily reads the default from server config on first use.
 */
public final class NoCollideState {
   private NoCollideState() {}

   private static final AtomicBoolean enabled = new AtomicBoolean(true);
   private static final AtomicBoolean initialized = new AtomicBoolean(false);

   /** True while ANY Entity.move() is executing (player/mob movement). */
   public static final ThreadLocal<Boolean> INSIDE_ENTITY_MOVE = ThreadLocal.withInitial(() -> false);
   /** True while Player pose fit checks are testing available space. */
   public static final ThreadLocal<Boolean> INSIDE_PLAYER_POSE_CHECK = ThreadLocal.withInitial(() -> false);

   public static boolean isEnabled() {
      if (initialized.compareAndSet(false, true)) {
         try {
            enabled.set(RagdollPatchConfig.NO_COLLIDE_DEFAULT.get());
         } catch (IllegalStateException e) {
            // Config not loaded yet - keep default (true)
         }
      }
      return enabled.get();
   }

   public static void setEnabled(boolean value) {
      initialized.set(true);
      enabled.set(value);
   }

   public static boolean toggle() {
      boolean now = !enabled.get();
      enabled.set(now);
      initialized.set(true);
      return now;
   }
}
