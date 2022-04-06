package vip.floatationdevice.mgbridge;

import cn.hutool.json.JSONObject;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import vip.floatationdevice.guilded4j.G4JClient;
import vip.floatationdevice.guilded4j.object.ChatMessage;
import vip.floatationdevice.mgbridge.gce.Command_list;
import vip.floatationdevice.mgbridge.gce.Command_mkbind;
import vip.floatationdevice.mgbridge.gce.Command_ping;
import vip.floatationdevice.mgbridge.gce.Command_rmbind;

import java.util.UUID;
import java.util.logging.Logger;

import static vip.floatationdevice.mgbridge.ConfigManager.cfg;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;

public final class MGBridge extends JavaPlugin implements Listener
{
    public static MGBridge instance;
    public static Logger log;
    static String lang, token, server, channel;
    static Boolean mgbRunning = false;
    G4JClient g4JClient = null;
    GuildedEventListener gEventListener = null;
    Boolean forwardJoinLeaveEvents = true;
    Boolean debug = false;

    @Override
    public void onEnable()
    {
        instance = this;
        log = getLogger();
        Bukkit.getPluginManager().registerEvents(this, this);
        try
        {
            if(!ConfigManager.loadConfig())
            {
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            lang = cfg.getString("language");
            token = cfg.getString("token");
            server = cfg.getString("server");
            channel = cfg.getString("channel");
            forwardJoinLeaveEvents = cfg.getBoolean("forwardJoinLeaveEvents");
            debug = cfg.getBoolean("debug");
            I18nUtil.setLanguage(lang);
            if(notSet(lang, token, server, channel) || !lang.matches("^[a-z]{2}_[A-Z]{2}$") || channel.length() != 36 || server.length() != 8)
            {
                log.severe(translate("invalid-config"));
                Bukkit.getPluginManager().disablePlugin(this);
                return;
            }
            BindManager.loadBindMap();
            g4JClient = new G4JClient(token);
            getCommand("mgb").setExecutor(new BukkitCommandExecutor());
            gEventListener = new GuildedEventListener()
                    .registerExecutor(new Command_mkbind())
                    .registerExecutor(new Command_rmbind())
                    .registerExecutor(new Command_ping())
                    .registerExecutor(new Command_list());
            mgbRunning = true;
            sendGuildedMsg(translate("mgb-started").replace("%VERSION%", getDescription().getVersion()), null);
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
        mgbRunning = false;
        if(gEventListener != null)
        {
            gEventListener.ws.close();
            gEventListener.clearExecutors();
            gEventListener = null;
        }
        if(g4JClient != null)
        {
            ChatMessage result = null;
            try {result = g4JClient.createChannelMessage(channel, translate("mgb-stopped"), null, null);}
            catch(Exception e) {log.severe(translate("msg-send-failed").replace("%EXCEPTION%", e.toString()));}
            g4JClient = null;
            if(debug && result != null)
                log.info("\n" + new JSONObject(result.toString()).toStringPretty());
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event)
    {
        String message = event.getMessage();
        if(!message.startsWith("/"))
            sendGuildedMsg("<" + event.getPlayer().getName() + "> " + message, null);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event)
    {
        if(forwardJoinLeaveEvents)
            sendGuildedMsg(translate("player-connected").replace("%PLAYER%", event.getPlayer().getName()), null);
    }

    @EventHandler
    public void onUnusualLeave(PlayerKickEvent event)
    {
        if(forwardJoinLeaveEvents)
            sendGuildedMsg(translate("player-disconnected-unusual").replace("%PLAYER%", event.getPlayer().getName()).replace("%REASON%", event.getReason()), null);
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event)
    {
        if(forwardJoinLeaveEvents)
            sendGuildedMsg(translate("player-disconnected").replace("%PLAYER%", event.getPlayer().getName()), null);
    }

    public void sendGuildedMsg(String msg, String replyTo)
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
                        result = g4JClient.createChannelMessage(MGBridge.channel, msg, replyTo == null ? null : new String[]{replyTo}, false);
                    }
                    catch(Exception e)
                    {
                        log.severe(translate("msg-send-failed").replace("%EXCEPTION%", e.toString()));
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

    public static String getPlayerName(final UUID u)
    {
        try {return Bukkit.getPlayer(u).getName();}
        catch(NullPointerException e) {return Bukkit.getOfflinePlayer(u).getName();}
    }

    public GuildedEventListener getGEventListener()
    {
        return gEventListener;
    }
}
