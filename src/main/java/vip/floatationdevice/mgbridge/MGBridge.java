package vip.floatationdevice.mgbridge;

import cn.hutool.json.JSONObject;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import vip.floatationdevice.guilded4j.G4JClient;
import vip.floatationdevice.guilded4j.object.ChatMessage;
import vip.floatationdevice.guilded4j.object.Embed;
import vip.floatationdevice.mgbridge.gce.*;

import java.util.UUID;
import java.util.logging.Logger;

import static vip.floatationdevice.mgbridge.ConfigManager.*;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;

public final class MGBridge extends JavaPlugin implements Listener
{
    /**
     * The instance of the plugin.
     */
    public static MGBridge instance;
    static Logger log;
    G4JClient g4JClient = null;
    GuildedEventListener gEventListener = null;

    @Override
    public void onEnable()
    {
        instance = this;
        log = getLogger();
        try
        {
            if(!ConfigManager.loadConfig())
            {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            g4JClient = new G4JClient(token);
            getCommand("mgb").setExecutor(new BukkitCommandExecutor());
            gEventListener = new GuildedEventListener()
                    .registerExecutor(new Command_help())
                    .registerExecutor(new Command_helpof())
                    .registerExecutor(new Command_mkbind())
                    .registerExecutor(new Command_rmbind())
                    .registerExecutor(new Command_ping())
                    .registerExecutor(new Command_list());
            g4JClient.setProxy(proxy);
            Bukkit.getPluginManager().registerEvents(this, this);
            sendGuildedEmbed(new Embed().setTitle(translate("mgb-started").replace("%VERSION%", getDescription().getVersion())).setColor(0xffffff), null, null, null);
        }
        catch(Throwable e)
        {
            log.severe("Failed to initialize plugin!");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable()
    {
        if(gEventListener != null)
        {
            gEventListener.disconnect();
            gEventListener.unregisterAllExecutors();
            gEventListener = null;
        }

        if(g4JClient != null)
        {
            ChatMessage result = null;
            // this time the message sending must be done in main thread or bukkit will complain about the plugin is registering a task while being disabled
            try
            {
                result = g4JClient.getChatMessageManager()
                        .createChannelMessage(
                                channel,
                                null,
                                new Embed[]{new Embed().setTitle(translate("mgb-stopped")).setAuthorName("MGBridge " + getDescription().getVersion()).setAuthorUrl(getDescription().getWebsite()).setColor(0xffffff)},
                                null,
                                null,
                                null
                        );
            }
            catch(Exception e) {log.severe(translate("msg-send-failed").replace("%EXCEPTION%", e.toString()));}
            g4JClient = null;
            if(debug && result != null)
                log.info("\n" + new JSONObject(result.toString()).toStringPretty());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChat(AsyncPlayerChatEvent event)
    {
        if(event.isCancelled()) return; // don't forward cancelled events
        String message = event.getMessage();
        if(toGuildedMessageFormat != null && !message.startsWith("/")) // check if M->G forwarding is enabled and the message is not a command
            sendGuildedMessage(toGuildedMessageFormat.replace("{PLAYER}", event.getPlayer().getName()).replace("{MESSAGE}", message), null, null, null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event)
    {
        if(forwardJoinLeaveEvents)
            sendGuildedEmbed(new Embed().setTitle(translate("player-connected").replace("%PLAYER%", event.getPlayer().getName())).setColor(0xffff00), null, null, null);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onUnusualLeave(PlayerKickEvent event)
    {
        if(forwardJoinLeaveEvents)
            sendGuildedMessage(translate("player-disconnected-unusual").replace("%PLAYER%", event.getPlayer().getName()).replace("%REASON%", event.getReason()), null, null, null);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLeave(PlayerQuitEvent event)
    {
        if(forwardJoinLeaveEvents)
            sendGuildedEmbed(new Embed().setTitle(translate("player-disconnected").replace("%PLAYER%", event.getPlayer().getName())).setColor(0xffff00), null, null, null);
    }

    /**
     * Sends a message to Guilded server.
     * @param msg The content of the message.
     * @param replyTo The ID of the message to reply to. Set to null to not reply.
     * @param isPrivate Whether the reply is private or not.
     * @param isSilent Whether the reply is silent or not.
     */
    public void sendGuildedMessage(String msg, String replyTo, Boolean isPrivate, Boolean isSilent)
    {
        Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable()
        {// fuck lambdas all my codes are lambda-free
            @Override
            public void run()
            {
                if(g4JClient != null)
                {
                    ChatMessage result = null;
                    try
                    {
                        result = g4JClient.getChatMessageManager()
                                .createChannelMessage(
                                        channel,
                                        msg,
                                        null,
                                        replyTo == null ? null : new String[]{replyTo},
                                        isPrivate,
                                        isSilent
                                );
                    }
                    catch(Exception e)
                    {
                        log.severe(translate("msg-send-failed").replace("%EXCEPTION%", e.getMessage()));
                    }
                    if(debug && result != null)
                        log.info("\n" + new JSONObject(result.toString()).toStringPretty());
                }
            }
        });
    }

    /**
     * Sends an embed to Guilded server.
     * @param emb The embed to send.
     * @param replyTo The ID of the message to reply to. Set to null to not reply.
     * @param isPrivate Whether the reply is private or not.
     * @param isSilent Whether the reply is silent or not.
     */
    public void sendGuildedEmbed(Embed emb, String replyTo, Boolean isPrivate, Boolean isSilent)
    {
        Bukkit.getScheduler().runTaskAsynchronously(instance, new Runnable()
        {// fuck lambdas all my codes are lambda-free
            @Override
            public void run()
            {
                if(g4JClient != null)
                {
                    ChatMessage result = null;
                    try
                    {
                        result = g4JClient.getChatMessageManager()
                                .createChannelMessage(
                                        channel,
                                        null,
                                        new Embed[]{emb.setAuthorName("MGBridge " + getDescription().getVersion()).setAuthorUrl(getDescription().getWebsite())},
                                        replyTo == null ? null : new String[]{replyTo},
                                        isPrivate,
                                        isSilent
                                );
                    }
                    catch(Exception e)
                    {
                        log.severe(translate("msg-send-failed").replace("%EXCEPTION%", e.getMessage()));
                    }
                    if(debug && result != null)
                        log.info("\n" + new JSONObject(result.toString()).toStringPretty());
                }
            }
        });
    }

    static boolean notSet(String... s)
    {
        for(String a : s)
            if(a == null || a.isEmpty())
                return true;
        return false;
    }

    /**
     * Gets the name of a player by UUID.
     * @param u The UUID of the player.
     * @return The name of the player. If the player is not found, returns "???".
     */
    public static String getPlayerName(final UUID u)
    {
        try {return Bukkit.getPlayer(u).getName();}
        catch(NullPointerException e)
        {
            String s = Bukkit.getOfflinePlayer(u).getName();
            return s != null ? s : "???";
        }
    }

    /**
     * Gets the Guilded event listener.
     * @return The GuildedEventListener object.
     */
    public GuildedEventListener getGEventListener()
    {
        return gEventListener;
    }

    /**
     * Gets the G4JClient used by the plugin.
     * @return The G4JClient object.
     */
    public G4JClient getG4JClient()
    {
        return g4JClient;
    }

    /**
     * Gets the Guilded channel ID used by the plugin.
     * @return The ID of the Guilded channel.
     */
    public static String getChannelId()
    {
        return channel;
    }

    /**
     * Gets the Guilded server ID used by the plugin.
     * @return The ID of the Guilded server.
     */
    public static String getServerId()
    {
        return server;
    }

    /**
     * Check if the Guilded user is bound to a Minecraft player.
     * @param userId The ID of the Guilded user.
     */
    public static boolean isBound(String userId)
    {
        return BindManager.bindMap.containsKey(userId);
    }
}
