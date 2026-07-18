package twtlinmiao.sableplayerragdollpatch;

import com.mojang.brigadier.CommandDispatcher;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchClientConfig;
import twtlinmiao.sableplayerragdollpatch.config.RagdollPatchConfig;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@Mod(SablePlayerRagdollPatch.MOD_ID)
public final class SablePlayerRagdollPatchNeoForge {

   public SablePlayerRagdollPatchNeoForge() {
      SablePlayerRagdollPatch.LOGGER.info("Sable Ragdolls Patch loaded");

      var container = ModLoadingContext.get().getActiveContainer();
      container.registerConfig(ModConfig.Type.SERVER, RagdollPatchConfig.SERVER_CONFIG,
            "sable_player_ragdoll_patch-server.toml");
      container.registerConfig(ModConfig.Type.CLIENT, RagdollPatchClientConfig.CLIENT_CONFIG,
            "sable_player_ragdoll_patch-client.toml");

      NeoForge.EVENT_BUS.addListener(SablePlayerRagdollPatchNeoForge::onRightClickBlock);
      NeoForge.EVENT_BUS.addListener(SablePlayerRagdollPatchNeoForge::onRegisterCommands);
   }

   private static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
      if (!RagdollPatchConfig.GRAB_INTERCEPT_ENABLED.get()) return;
      if (event.getEntity() instanceof ServerPlayer player
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
