package twtlinmiao.sableplayerragdollpatch;

import com.mojang.brigadier.CommandDispatcher;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchClientConfig;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.IExtensionPoint;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(SablePlayerRagdollPatch.MOD_ID)
public final class SablePlayerRagdollPatchNeoForge {

   public SablePlayerRagdollPatchNeoForge(IEventBus modEventBus, ModContainer container) {
      SablePlayerRagdollPatch.LOGGER.info("Sable Ragdolls Patch loaded");

      container.registerConfig(ModConfig.Type.SERVER, RagdollPatchConfig.SERVER_CONFIG,
            "sable_player_ragdoll_patch-server.toml");
      container.registerConfig(ModConfig.Type.CLIENT, RagdollPatchClientConfig.CLIENT_CONFIG,
            "sable_player_ragdoll_patch-client.toml");

      if (FMLEnvironment.dist == Dist.CLIENT) {
         registerConfigScreen(container);
         modEventBus.addListener(SablePlayerRagdollPatchNeoForge::onClientSetup);
      }

      NeoForge.EVENT_BUS.addListener(SablePlayerRagdollPatchNeoForge::onRightClickBlock);
      NeoForge.EVENT_BUS.addListener(SablePlayerRagdollPatchNeoForge::onRegisterCommands);
   }

   @SuppressWarnings("unchecked")
   private static void registerConfigScreen(ModContainer container) {
      try {
         Class<?> screenClass = Class.forName("net.minecraft.client.gui.screens.Screen");
         Class<IExtensionPoint> factoryClass = (Class<IExtensionPoint>) Class.forName("net.neoforged.neoforge.client.gui.IConfigScreenFactory")
               .asSubclass(IExtensionPoint.class);
         Class<?> configScreenClass = Class.forName("net.neoforged.neoforge.client.gui.ConfigurationScreen");
         Constructor<?> configScreenConstructor = configScreenClass.getConstructor(ModContainer.class, screenClass);

         IExtensionPoint configScreenFactory = (IExtensionPoint) Proxy.newProxyInstance(
               SablePlayerRagdollPatchNeoForge.class.getClassLoader(),
               new Class<?>[] { factoryClass },
               (proxy, method, args) -> {
                  if (method.getDeclaringClass() == Object.class) {
                     return handleObjectMethod(proxy, method.getName(), args);
                  }
                  if ("createScreen".equals(method.getName())) {
                     try {
                        return configScreenConstructor.newInstance(args[0], args[1]);
                     } catch (InvocationTargetException exception) {
                        throw exception.getCause();
                     }
                  }
                  return null;
               });

         container.registerExtensionPoint(factoryClass, configScreenFactory);
      } catch (ReflectiveOperationException | LinkageError exception) {
         SablePlayerRagdollPatch.LOGGER.warn("Failed to register client config screen", exception);
      }
   }

   private static Object handleObjectMethod(Object proxy, String methodName, Object[] args) {
      return switch (methodName) {
         case "toString" -> "Sable Ragdolls Patch config screen factory";
         case "hashCode" -> System.identityHashCode(proxy);
         case "equals" -> proxy == args[0];
         default -> null;
      };
   }

   private static void onClientSetup(FMLClientSetupEvent event) {
      event.enqueueWork(DynamicLightsCompat::register);
   }

   private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
      if (!RagdollPatchConfig.GRAB_INTERCEPT_ENABLED.get()) return;
      if (event.getEntity() instanceof Player player
            && ActiveGrabbers.PLAYERS.contains(player.getUUID())) {
         event.setCanceled(true);
      }
   }

   private static void onRegisterCommands(RegisterCommandsEvent event) {
      CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
      dispatcher.register(Commands.literal("nocollide")
         .requires(source -> source.hasPermission(2))
         .executes(ctx -> {
            boolean now = NoCollideState.toggle();
            String status = now ? "enabled" : "disabled";
            ctx.getSource().sendSuccess(
               () -> Component.literal("No-Collide mode for Sable Ragdolls is now " + status + "."),
               true
            );
            return 1;
         })
      );
   }
}
