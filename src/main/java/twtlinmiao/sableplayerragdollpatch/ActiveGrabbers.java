package twtlinmiao.sableplayerragdollpatch;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which players are currently grabbing a ragdoll part.
 * Updated by {@link twtlinmiao.sableplayerragdollpatch.mixin.RagdollPartBlockEntityMixin}.
 * Read by NeoForge event handlers to prevent container interactions while grabbing.
 */
public final class ActiveGrabbers {
   private ActiveGrabbers() {}

   public static final Set<UUID> PLAYERS = ConcurrentHashMap.newKeySet();
}
