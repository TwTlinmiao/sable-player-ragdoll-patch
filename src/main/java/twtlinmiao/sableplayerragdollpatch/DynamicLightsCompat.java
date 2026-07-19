package twtlinmiao.sableplayerragdollpatch;

import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity;
import dev.leo.sableplayerragdoll.block.entity.RagdollPartBlockEntity.BodyPart;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.ClientSubLevel;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.joml.Vector3d;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class DynamicLightsCompat {
   private static final double MAX_RADIUS = 7.75D;
   private static final double MAX_RADIUS_SQUARED = MAX_RADIUS * MAX_RADIUS;
   private static final int REBUILD_RADIUS = 8;

   private static final Map<RagdollPartBlockEntity, RagdollLightSource> SOURCES = new WeakHashMap<>();

   private static boolean initialized;
   private static SodiumBackend sodiumBackend;
   private static LambBackend lambBackend;
   private static boolean disabledSourcesCleared = true;

   private DynamicLightsCompat() {
   }

   public static void register() {
      if (initialized) return;
      initialized = true;

      sodiumBackend = SodiumBackend.tryCreate();
      lambBackend = LambBackend.tryCreate();

      if (sodiumBackend != null) {
         SablePlayerRagdollPatch.LOGGER.info("Registered Sodium/Embeddium Dynamic Lights ragdoll compatibility");
      }
      if (lambBackend != null) {
         SablePlayerRagdollPatch.LOGGER.info("Registered LambDynamicLights ragdoll compatibility");
      }
   }

   public static void updateRenderedPart(RagdollPartBlockEntity blockEntity, float partialTick) {
      if (blockEntity == null || (sodiumBackend == null && lambBackend == null)) return;
      if (!isEnabled()) {
         clearSources();
         return;
      }

      disabledSourcesCleared = false;

      RagdollLightSource source = SOURCES.computeIfAbsent(blockEntity, RagdollLightSource::new);
      source.update(partialTick);

      if (source.luminance() > 0) {
         if (sodiumBackend != null) sodiumBackend.add(source);
         if (lambBackend != null) lambBackend.add(source);
      }
   }

   public static int modelLuminance(RagdollPartBlockEntity blockEntity) {
      if (blockEntity == null || !isEnabled()) return 0;

      boolean submerged = isSubmerged(blockEntity);
      return Math.max(
         getStackLuminance(blockEntity.itemBySlot(EquipmentSlot.MAINHAND), submerged),
         getStackLuminance(blockEntity.itemBySlot(EquipmentSlot.OFFHAND), submerged)
      );
   }

   private static int getLuminance(RagdollPartBlockEntity blockEntity) {
      EquipmentSlot slot = lightSlot(blockEntity.bodyPart());
      if (slot == null) return 0;

      ItemStack stack = blockEntity.itemBySlot(slot);
      return getStackLuminance(stack, isSubmerged(blockEntity));
   }

   private static int getStackLuminance(ItemStack stack, boolean submerged) {
      if (stack.isEmpty()) return 0;

      int luminance = 0;
      if (sodiumBackend != null) luminance = Math.max(luminance, sodiumBackend.getItemLuminance(stack, submerged));
      if (lambBackend != null) luminance = Math.max(luminance, lambBackend.getItemLuminance(stack, submerged));
      if (sodiumBackend == null && lambBackend == null && stack.getItem() instanceof BlockItem blockItem) {
         luminance = Math.max(luminance, blockItem.getBlock().defaultBlockState().getLightEmission());
      }
      return luminance;
   }

   private static boolean isEnabled() {
      return RagdollPatchClientConfig.RAGDOLL_DYNAMIC_LIGHTS_ENABLED.get();
   }

   private static void clearSources() {
      if (disabledSourcesCleared) return;

      for (RagdollLightSource source : SOURCES.values()) {
         source.disable();
         if (sodiumBackend != null) sodiumBackend.remove(source);
      }
      disabledSourcesCleared = true;
   }

   private static EquipmentSlot lightSlot(BodyPart bodyPart) {
      if (bodyPart == BodyPart.RIGHT_ARM) return EquipmentSlot.MAINHAND;
      if (bodyPart == BodyPart.LEFT_ARM) return EquipmentSlot.OFFHAND;
      return null;
   }

   private static boolean isSubmerged(RagdollPartBlockEntity blockEntity) {
      Level level = blockEntity.getLevel();
      return level != null && !level.getFluidState(blockEntity.getBlockPos()).isEmpty();
   }

   private static Vector3d worldCenter(BlockEntity blockEntity, float partialTick) {
      if (blockEntity == null || blockEntity.getLevel() == null) {
         return new Vector3d();
      }

      ClientSubLevel subLevel = Sable.HELPER.getContainingClient(blockEntity);
      BlockPos blockPos = blockEntity.getBlockPos();
      Vector3d localCenter = new Vector3d(blockPos.getX() + 0.5D, blockPos.getY() + 0.5D, blockPos.getZ() + 0.5D);
      if (subLevel == null) {
         return localCenter;
      }

      return subLevel.renderPose(partialTick).transformPosition(localCenter, new Vector3d());
   }

   private static double lightAtPos(BlockPos pos, double currentLightLevel, Vector3d sourcePos, int luminance) {
      if (luminance <= 0) return currentLightLevel;

      double dx = pos.getX() - sourcePos.x + 0.5D;
      double dy = pos.getY() - sourcePos.y + 0.5D;
      double dz = pos.getZ() - sourcePos.z + 0.5D;
      double distanceSquared = dx * dx + dy * dy + dz * dz;
      if (distanceSquared > MAX_RADIUS_SQUARED) return currentLightLevel;

      double multiplier = 1.0D - Math.sqrt(distanceSquared) / MAX_RADIUS;
      return Math.max(currentLightLevel, multiplier * luminance);
   }

   private static final class RagdollLightSource {
      private final WeakReference<RagdollPartBlockEntity> blockEntity;
      private final Object sodiumProxy;
      private final Object lambBehaviorProxy;
      private Vector3d position = new Vector3d();
      private Vector3d previousPosition = new Vector3d(Double.NaN, Double.NaN, Double.NaN);
      private int luminance;
      private int previousLuminance = -1;
      private boolean lambAdded;
      private final Set<Long> sodiumTrackedSections = new HashSet<>();

      private RagdollLightSource(RagdollPartBlockEntity blockEntity) {
         this.blockEntity = new WeakReference<>(blockEntity);
         this.sodiumProxy = sodiumBackend == null ? null : sodiumBackend.createProxy(this);
         this.lambBehaviorProxy = lambBackend == null ? null : lambBackend.createBehaviorProxy(this);
      }

      private void update(float partialTick) {
         RagdollPartBlockEntity entity = this.blockEntity.get();
         if (entity == null || entity.isRemoved()) {
            this.luminance = 0;
            return;
         }

         this.position = worldCenter(entity, partialTick);
         this.luminance = getLuminance(entity);
      }

      private void disable() {
         this.luminance = 0;
      }

      private Level level() {
         RagdollPartBlockEntity entity = this.blockEntity.get();
         return entity == null ? null : entity.getLevel();
      }

      private int luminance() {
         return this.luminance;
      }

      private boolean changed() {
         return this.luminance != this.previousLuminance
            || Math.abs(this.position.x - this.previousPosition.x) > 0.1D
            || Math.abs(this.position.y - this.previousPosition.y) > 0.1D
            || Math.abs(this.position.z - this.previousPosition.z) > 0.1D;
      }

      private void markClean() {
         this.previousLuminance = this.luminance;
         this.previousPosition = new Vector3d(this.position);
      }

      private Object sodiumProxy() {
         return this.sodiumProxy;
      }

      private Object lambBehaviorProxy() {
         return this.lambBehaviorProxy;
      }
   }

   private static final class SodiumBackend {
      private final Object instance;
      private final Class<?> sourceClass;
      private final Method addLightSource;
      private final Method removeLightSource;
      private final Method containsLightSource;
      private final Method getLuminanceFromItemStack;
      private final Method scheduleChunkRebuild;

      private SodiumBackend(
         Object instance,
         Class<?> sourceClass,
         Method addLightSource,
         Method removeLightSource,
         Method containsLightSource,
         Method getLuminanceFromItemStack,
         Method scheduleChunkRebuild
      ) {
         this.instance = instance;
         this.sourceClass = sourceClass;
         this.addLightSource = addLightSource;
         this.removeLightSource = removeLightSource;
         this.containsLightSource = containsLightSource;
         this.getLuminanceFromItemStack = getLuminanceFromItemStack;
         this.scheduleChunkRebuild = scheduleChunkRebuild;
      }

      private static SodiumBackend tryCreate() {
         try {
            Class<?> sodiumClass = Class.forName("toni.sodiumdynamiclights.SodiumDynamicLights");
            Class<?> sourceClass = Class.forName("toni.sodiumdynamiclights.DynamicLightSource");
            Object instance = sodiumClass.getMethod("get").invoke(null);
            return new SodiumBackend(
               instance,
               sourceClass,
               sodiumClass.getMethod("addLightSource", sourceClass),
               sodiumClass.getMethod("removeLightSource", sourceClass),
               sodiumClass.getMethod("containsLightSource", sourceClass),
               sodiumClass.getMethod("getLuminanceFromItemStack", ItemStack.class, boolean.class),
               sodiumClass.getMethod("scheduleChunkRebuild", net.minecraft.client.renderer.LevelRenderer.class, int.class, int.class, int.class)
            );
         } catch (ClassNotFoundException ignored) {
            return null;
         } catch (ReflectiveOperationException | RuntimeException exception) {
            SablePlayerRagdollPatch.LOGGER.warn("Failed to initialize Sodium/Embeddium Dynamic Lights compatibility", exception);
            return null;
         }
      }

      private Object createProxy(RagdollLightSource source) {
         return Proxy.newProxyInstance(
            this.sourceClass.getClassLoader(),
            new Class<?>[] { this.sourceClass },
            new SodiumLightSourceHandler(source, this)
         );
      }

      private void add(RagdollLightSource source) {
         if (source.sodiumProxy() == null) return;

         try {
            if (!(boolean) this.containsLightSource.invoke(this.instance, source.sodiumProxy())) {
               this.addLightSource.invoke(this.instance, source.sodiumProxy());
            }
         } catch (ReflectiveOperationException exception) {
            SablePlayerRagdollPatch.LOGGER.warn("Failed to add ragdoll Sodium dynamic light", exception);
         }
      }

      private void remove(RagdollLightSource source) {
         if (source.sodiumProxy() == null) return;

         try {
            if ((boolean) this.containsLightSource.invoke(this.instance, source.sodiumProxy())) {
               this.removeLightSource.invoke(this.instance, source.sodiumProxy());
            }
         } catch (ReflectiveOperationException exception) {
            SablePlayerRagdollPatch.LOGGER.warn("Failed to remove ragdoll Sodium dynamic light", exception);
         }
      }

      private int getItemLuminance(ItemStack stack, boolean submerged) {
         try {
            return (int) this.getLuminanceFromItemStack.invoke(null, stack, submerged);
         } catch (ReflectiveOperationException exception) {
            SablePlayerRagdollPatch.LOGGER.warn("Failed to read Sodium dynamic light item luminance", exception);
            return 0;
         }
      }

      private void schedule(Object renderer, int sectionX, int sectionY, int sectionZ) {
         try {
            this.scheduleChunkRebuild.invoke(null, renderer, sectionX, sectionY, sectionZ);
         } catch (ReflectiveOperationException exception) {
            SablePlayerRagdollPatch.LOGGER.warn("Failed to schedule Sodium dynamic light chunk rebuild", exception);
         }
      }
   }

   private static final class SodiumLightSourceHandler implements InvocationHandler {
      private final RagdollLightSource source;
      private final SodiumBackend backend;

      private SodiumLightSourceHandler(RagdollLightSource source, SodiumBackend backend) {
         this.source = source;
         this.backend = backend;
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) {
         return switch (method.getName()) {
            case "sdl$getDynamicLightX" -> this.source.position.x;
            case "sdl$getDynamicLightY" -> this.source.position.y;
            case "sdl$getDynamicLightZ" -> this.source.position.z;
            case "sdl$getDynamicLightLevel" -> this.source.level();
            case "sdl$getLuminance" -> this.source.luminance();
            case "sdl$resetDynamicLight" -> {
               this.source.previousLuminance = 0;
               yield null;
            }
            case "sdl$dynamicLightTick" -> null;
            case "sdl$shouldUpdateDynamicLight" -> true;
            case "sdl$isDynamicLightEnabled" -> isTracked(proxy);
            case "sdl$setDynamicLightEnabled" -> {
               if ((boolean) args[0]) this.backend.add(this.source);
               else this.backend.remove(this.source);
               yield null;
            }
            case "sodiumdynamiclights$updateDynamicLight" -> update((Object) args[0]);
            case "sodiumdynamiclights$scheduleTrackedChunksRebuild" -> {
               scheduleTracked((Object) args[0]);
               yield null;
            }
            case "toString" -> "SableRagdollSodiumDynamicLight";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> throw new UnsupportedOperationException(method.toString());
         };
      }

      private boolean isTracked(Object proxy) {
         try {
            return (boolean) this.backend.containsLightSource.invoke(this.backend.instance, proxy);
         } catch (ReflectiveOperationException exception) {
            return false;
         }
      }

      private boolean update(Object renderer) {
         if (this.source.level() != Minecraft.getInstance().level || !this.source.changed()) {
            return false;
         }

         scheduleAround(renderer, this.source.previousPosition);
         scheduleAround(renderer, this.source.position);
         this.source.markClean();
         return true;
      }

      private void scheduleTracked(Object renderer) {
         for (long section : this.source.sodiumTrackedSections) {
            this.backend.schedule(renderer, SectionPos.x(section), SectionPos.y(section), SectionPos.z(section));
         }
      }

      private void scheduleAround(Object renderer, Vector3d position) {
         if (Double.isNaN(position.x)) return;

         int minX = SectionPos.blockToSectionCoord((int) Math.floor(position.x - REBUILD_RADIUS));
         int minY = SectionPos.blockToSectionCoord((int) Math.floor(position.y - REBUILD_RADIUS));
         int minZ = SectionPos.blockToSectionCoord((int) Math.floor(position.z - REBUILD_RADIUS));
         int maxX = SectionPos.blockToSectionCoord((int) Math.floor(position.x + REBUILD_RADIUS));
         int maxY = SectionPos.blockToSectionCoord((int) Math.floor(position.y + REBUILD_RADIUS));
         int maxZ = SectionPos.blockToSectionCoord((int) Math.floor(position.z + REBUILD_RADIUS));

         for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
               for (int z = minZ; z <= maxZ; z++) {
                  this.source.sodiumTrackedSections.add(SectionPos.asLong(x, y, z));
                  this.backend.schedule(renderer, x, y, z);
               }
            }
         }
      }
   }

   private static final class LambBackend {
      private final Object behaviorManager;
      private final Class<?> behaviorClass;
      private final Class<?> boundingBoxClass;
      private final Method addBehavior;
      private final Method getLuminanceFromItemStack;

      private LambBackend(
         Object behaviorManager,
         Class<?> behaviorClass,
         Class<?> boundingBoxClass,
         Method addBehavior,
         Method getLuminanceFromItemStack
      ) {
         this.behaviorManager = behaviorManager;
         this.behaviorClass = behaviorClass;
         this.boundingBoxClass = boundingBoxClass;
         this.addBehavior = addBehavior;
         this.getLuminanceFromItemStack = getLuminanceFromItemStack;
      }

      private static LambBackend tryCreate() {
         try {
            Class<?> lambClass = Class.forName("dev.lambdaurora.lambdynlights.LambDynLights");
            Class<?> behaviorClass = Class.forName("dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior");
            Class<?> boundingBoxClass = Class.forName("dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior$BoundingBox");
            Object instance = lambClass.getMethod("get").invoke(null);
            Object behaviorManager = lambClass.getMethod("dynamicLightBehaviorManager").invoke(instance);
            return new LambBackend(
               behaviorManager,
               behaviorClass,
               boundingBoxClass,
               behaviorManager.getClass().getMethod("add", behaviorClass),
               lambClass.getMethod("getLuminanceFromItemStack", ItemStack.class, boolean.class)
            );
         } catch (ClassNotFoundException ignored) {
            return null;
         } catch (ReflectiveOperationException | RuntimeException exception) {
            SablePlayerRagdollPatch.LOGGER.warn("Failed to initialize LambDynamicLights compatibility", exception);
            return null;
         }
      }

      private Object createBehaviorProxy(RagdollLightSource source) {
         return Proxy.newProxyInstance(
            this.behaviorClass.getClassLoader(),
            new Class<?>[] { this.behaviorClass },
            new LambLightBehaviorHandler(source, this)
         );
      }

      private void add(RagdollLightSource source) {
         if (source.lambBehaviorProxy() == null || source.lambAdded) return;

         try {
            this.addBehavior.invoke(this.behaviorManager, source.lambBehaviorProxy());
            source.lambAdded = true;
         } catch (ReflectiveOperationException exception) {
            SablePlayerRagdollPatch.LOGGER.warn("Failed to add ragdoll LambDynamicLights behavior", exception);
         }
      }

      private int getItemLuminance(ItemStack stack, boolean submerged) {
         try {
            return (int) this.getLuminanceFromItemStack.invoke(null, stack, submerged);
         } catch (ReflectiveOperationException exception) {
            SablePlayerRagdollPatch.LOGGER.warn("Failed to read LambDynamicLights item luminance", exception);
            return 0;
         }
      }

      private Object boundingBox(Vector3d position) throws ReflectiveOperationException {
         int minX = (int) Math.floor(position.x - REBUILD_RADIUS);
         int minY = (int) Math.floor(position.y - REBUILD_RADIUS);
         int minZ = (int) Math.floor(position.z - REBUILD_RADIUS);
         int maxX = (int) Math.floor(position.x + REBUILD_RADIUS);
         int maxY = (int) Math.floor(position.y + REBUILD_RADIUS);
         int maxZ = (int) Math.floor(position.z + REBUILD_RADIUS);
         return this.boundingBoxClass.getConstructor(int.class, int.class, int.class, int.class, int.class, int.class)
            .newInstance(minX, minY, minZ, maxX, maxY, maxZ);
      }
   }

   private static final class LambLightBehaviorHandler implements InvocationHandler {
      private final RagdollLightSource source;
      private final LambBackend backend;

      private LambLightBehaviorHandler(RagdollLightSource source, LambBackend backend) {
         this.source = source;
         this.backend = backend;
      }

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
         return switch (method.getName()) {
            case "lightAtPos" -> lightAtPos((BlockPos) args[0], (double) args[1], this.source.position, this.source.luminance());
            case "getBoundingBox" -> this.backend.boundingBox(this.source.position);
            case "hasChanged" -> this.source.changed();
            case "isRemoved" -> false;
            case "toString" -> "SableRagdollLambDynamicLight";
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            default -> throw new UnsupportedOperationException(method.toString());
         };
      }
   }
}
