package vip.floatationdevice.mgbridge;

import com.google.common.eventbus.Subscribe;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import vip.floatationdevice.guilded4j.G4JWebSocketClient;
import vip.floatationdevice.guilded4j.event.ChatMessageCreatedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketClosedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketInitializedEvent;
import vip.floatationdevice.guilded4j.object.ChatMessage;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import static vip.floatationdevice.mgbridge.MGBridge.instance;
import static vip.floatationdevice.mgbridge.MGBridge.log;
import static vip.floatationdevice.mgbridge.MGBridge.mgbRunning;
import static vip.floatationdevice.mgbridge.MGBridge.token;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class BindManager
{
    public static HashMap<String, UUID> bindMap = new HashMap<String, UUID>();// key: guilded userId; value: mc player uuid
    public static HashMap<String, UUID> pendingMap = new HashMap<String, UUID>();// key: bind code; value: mc player uuid
    public static HashMap<UUID, String> pendingPlayerMap = new HashMap<UUID, String>();// pendingMap but with upside down
    public static final Random r = new Random(); // used to generate random bind code
    G4JWebSocketClient ws; // used to connect to guilded and receive guilded messages
    private final String socksProxyHost = System.getProperty("socksProxyHost"); // socks proxy settings in the arguments
    private final String socksProxyPort = System.getProperty("socksProxyPort");

    public BindManager()
    {
        loadBindMap();
        log.info(translate("connecting"));
        ws = new G4JWebSocketClient(MGBridge.token);
        // set socks proxy if it is set in the arguments
        if(socksProxyHost != null && socksProxyPort != null)
            ws.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksProxyHost, Integer.parseInt(socksProxyPort))));
        ws.eventBus.register(this);
        ws.connect();
    }

    @Subscribe
    public void onG4JConnectionOpened(GuildedWebSocketInitializedEvent event)
    {
        log.info(translate("connected"));
    }

    @Subscribe
    public void onG4JConnectionClosed(GuildedWebSocketClosedEvent event)
    {
        if(mgbRunning)
        {
            // if the plugin is running normally but the connection was closed
            // then we can consider it as unexpected and do a reconnection
            log.warning(translate("disconnected-unexpected"));
            ws = new G4JWebSocketClient(token);
            if(socksProxyHost != null && socksProxyPort != null)
                ws.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksProxyHost, Integer.parseInt(socksProxyPort))));
            ws.eventBus.register(this);
            ws.connect();
        }
        else
            // the plugin is being disabled or the server is stopping, so we can just ignore this
            log.info(translate("disconnected"));
    }

    @Subscribe
    public void onGuildedChat(ChatMessageCreatedEvent event)
    {
        ChatMessage msg = event.getChatMessageObject();// the received ChatMessage object
        if(msg.getServerId().equals(MGBridge.server) && msg.getChannelId().equals(MGBridge.channel))// in the right server and channel?
        {
            if(msg.getContent().startsWith("/mgb "))
            {
                String[] args = msg.getContent().split(" ");
                switch(args[1])
                {
                    case "mkbind":
                    {
                        if(args.length != 3)// incorrect command format?
                            instance.sendGuildedMsg(translate("g-usage"), msg.getId());
                        else// right usage?
                            if(bindMap.containsKey(msg.getCreatorId()))// player already bound?
                                instance.sendGuildedMsg(translate("g-already-bound").replace("%PLAYER%", getPlayerName(bindMap.get(msg.getCreatorId()))), msg.getId());
                            else// player not bound?
                            {
                                if(pendingMap.containsKey(args[2]))// code matched?
                                {
                                    bindMap.put(msg.getCreatorId(), pendingMap.get(args[2]));
                                    pendingPlayerMap.remove(pendingMap.get(args[2]));
                                    pendingMap.remove(args[2]);
                                    try
                                    {
                                        Bukkit.getPlayer(bindMap.get(msg.getCreatorId())).sendMessage(translate("m-bind-success"));
                                    }
                                    catch(NullPointerException ignored) {}
                                    instance.sendGuildedMsg(translate("g-bind-success").replace("%PLAYER%", getPlayerName(bindMap.get(msg.getCreatorId()))), msg.getId());
                                    log.info(translate("c-bind-success").replace("%PLAYER%", getPlayerName(bindMap.get(msg.getCreatorId()))));
                                    saveBindMap();
                                }
                                else// code not in pending list?
                                    instance.sendGuildedMsg(translate("invalid-code"), msg.getId());
                            }
                        break;
                    }
                    case "rmbind":
                    {
                        if(bindMap.containsKey(msg.getCreatorId()))// player bound?
                        {
                            try
                            {
                                Bukkit.getPlayer(bindMap.get(msg.getCreatorId())).sendMessage(translate("m-unbind-success"));
                            }
                            catch(Exception ignored) {}
                            UUID removed = bindMap.remove(msg.getCreatorId());
                            instance.sendGuildedMsg(translate("g-unbind-success"), msg.getId());
                            log.info(translate("c-unbind-success").replace("%PLAYER%", getPlayerName(removed)));
                            saveBindMap();
                        }
                        else// player not bound?
                            instance.sendGuildedMsg(translate("g-no-bind"), msg.getId());
                        break;
                    }
                    default:
                    {
                        break;
                    }
                }
            }
            else // not a mgb command
            {
                if(!msg.getContent().startsWith("/") && bindMap.containsKey(msg.getCreatorId())) // guilded user bound?
                    Bukkit.broadcastMessage("§e<§r" + getPlayerName(bindMap.get(msg.getCreatorId())) + "§e> §r" + msg.getContent());
            }
        }
    }

    public static String getPlayerName(final UUID u)
    {
        try {return Bukkit.getPlayer(u).getName();}
        catch(NullPointerException e) {return Bukkit.getOfflinePlayer(u).getName();}
    }

    public static class BindMapContainer implements Serializable
    {
        private static final long serialVersionUID = 1L;
        public HashMap<String, UUID> saveBindMap;

        public BindMapContainer(HashMap<String, UUID> bindMap){saveBindMap = bindMap;}
    }

    public static void saveBindMap()
    {
        try
        {
            ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(new File(instance.getDataFolder(), "bindMap.dat")));
            o.writeObject(new BindMapContainer(bindMap));
            o.close();
            log.info(translate("bindmap-save-success"));
        }
        catch(Exception e)
        {
            log.severe(translate("bindmap-save-failure").replace("%EXCEPTION%", e.toString()));
        }
    }

    public static void loadBindMap()
    {
        try
        {
            ObjectInputStream o = new ObjectInputStream(new FileInputStream(new File(instance.getDataFolder(), "bindMap.dat")));
            BindMapContainer temp = (BindMapContainer) o.readObject();
            o.close();
            bindMap = temp.saveBindMap;
            log.info(translate("bindmap-load-success").replace("%COUNT%", String.valueOf(bindMap.size())));
        }
        catch(FileNotFoundException ignored) {}
        catch(Exception e)
        {
            log.severe(translate("bindmap-load-failure").replace("%EXCEPTION%", e.toString()));
        }
    }
}
