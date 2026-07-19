package twtlinmiao.sableplayerragdollpatch;

import com.mojang.brigadier.CommandDispatcher;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchClientConfig;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
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

      IConfigScreenFactory configScreenFactory = (modContainer, parent) -> new ConfigurationScreen(modContainer, parent);
      container.registerExtensionPoint(IConfigScreenFactory.class, configScreenFactory);

      modEventBus.addListener(SablePlayerRagdollPatchNeoForge::onClientSetup);
      NeoForge.EVENT_BUS.addListener(SablePlayerRagdollPatchNeoForge::onRightClickBlock);
      NeoForge.EVENT_BUS.addListener(SablePlayerRagdollPatchNeoForge::onRegisterCommands);
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
