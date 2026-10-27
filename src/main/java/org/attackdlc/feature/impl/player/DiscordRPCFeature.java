package org.attackdlc.feature.impl.player;

import com.sun.jna.Native;
import net.minecraft.client.Minecraft;
import org.attackdlc.discord.DiscordEventHandlers;
import org.attackdlc.discord.DiscordRPC;
import org.attackdlc.discord.DiscordRichPresence;
import org.attackdlc.feature.Feature;
import org.attackdlc.feature.FeatureCategory;
import org.attackdlc.utils.NativeUtils;
import sdk.api.invoke.api.invoke;
import sdk.api.invoke.impl.PackerMode;
import sdk.api.invoke.impl.PackerType;

@invoke(ivirtualiz = PackerType.MUTATION, imode = PackerMode.BLOCK)
public final class DiscordRPCFeature extends Feature {
   private static final String DISCORD_ID = "1562455340954558464";
   private static final String DISCORD_PUBLIC_KEY = "e152366d40534070320d8a1c4c6ca420ab30f96571cdbdd85ac0bc1d7e3ce7e6";
   private static final String VERSION_LABEL = "Attack DLC | 26.2";
   private static final String LARGE_IMAGE_KEY = "https://yourimageshare.com/ib/WzgZxJCf3M.gif";
   private static DiscordRPC discordRPC;
   private static DiscordRichPresence presence;
   private static Thread rpcThread;
   private static volatile boolean running;
   private static volatile boolean available;

   public DiscordRPCFeature() {
      super("Discord RPC", "Shows Discord Rich Presence for AttackDLC Client.", FeatureCategory.PLAYER, -1);
      this.setVisible(false);
      initRPC();
      startRPC();
   }

   private static void initRPC() {
      try {
         NativeUtils.extractNativeLibrary("discord-rpc");
         discordRPC = (DiscordRPC)Native.load("discord-rpc", DiscordRPC.class);
         presence = new DiscordRichPresence();
         available = true;
      } catch (Throwable t) {
         available = false;
         System.err.println("[DiscordRPC] Failed to initialize: " + t.getMessage());
      }
   }

   private static void startRPC() {
      if (!running && available) {
         try {
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            discordRPC.Discord_Initialize(DISCORD_ID, handlers, true, null);
            presence.startTimestamp = System.currentTimeMillis() / 1000L;
            presence.largeImageKey = LARGE_IMAGE_KEY;
            presence.largeImageText = VERSION_LABEL;
            applyButtons();
            running = true;
            rpcThread = new Thread(() -> {
               while (running) {
                  try {
                     presence.details = modificationLabel();
                     // роль + ник: например "👑 Owner · Kitikat"
                     presence.state = roleLabel() + " · " + sessionName();
                     discordRPC.Discord_UpdatePresence(presence);
                     discordRPC.Discord_RunCallbacks();
                     Thread.sleep(5000L);
                  } catch (InterruptedException e) { break; }
                  catch (Exception e) { e.printStackTrace(); }
               }
            }, "Discord-RPC-Thread");
            rpcThread.setDaemon(true);
            rpcThread.start();
         } catch (Exception e) {
            e.printStackTrace();
            running = false;
         }
      }
   }

   private static void stopRPC() {
      running = false;
      if (rpcThread != null && rpcThread.isAlive()) rpcThread.interrupt();
      if (available) {
         try { discordRPC.Discord_ClearPresence(); discordRPC.Discord_Shutdown(); }
         catch (Exception e) { e.printStackTrace(); }
      }
   }

   private static void applyButtons() {
      try {
         String funpay = ru.xd.Session.getFunpayUrl();
         String site   = ru.xd.Session.getSiteUrl();
         if (funpay != null && !funpay.isBlank()) {
            presence.button_label_1 = "\u041a\u0443\u043f\u0438\u0442\u044c \u043a\u043b\u044e\u0447";
            presence.button_url_1   = funpay;
         }
         if (site != null && !site.isBlank()) {
            presence.button_label_2 = "\u0421\u0430\u0439\u0442";
            presence.button_url_2   = site;
         }
      } catch (Throwable ignored) {}
   }

   private static String modificationLabel() {
      return VERSION_LABEL;
   }

   /**
    * Роль аккаунта для Discord RPC.
    * owner → 👑 Owner | user → 🔑 User | free → ⚡ Free
    */
   private static String roleLabel() {
      try {
         String r = ru.xd.Session.getRole();
         if ("owner".equalsIgnoreCase(r)) return "\uD83D\uDC51 Owner";
         if ("user".equalsIgnoreCase(r))   return "\uD83D\uDD11 User";
      } catch (Throwable ignored) {}
      return "\u26A1 Free";
   }

   /**
    * Ник аккаунта из лаунчера.
    */
   private static String sessionName() {
      try {
         if (Class.forName("ru.xd.Session").getMethod("getName").invoke(null) instanceof String s && !s.isBlank()) {
            return s;
         }
      } catch (Throwable var2) {}
      return "\u041d\u0435 \u0430\u0432\u0442\u043e\u0440\u0438\u0437\u043e\u0432\u0430\u043d";
   }
}
