package vip.floatationdevice.mgbridge;

import com.google.common.eventbus.Subscribe;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
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
import static vip.floatationdevice.mgbridge.MGBridge.mgbRunning;
import static vip.floatationdevice.mgbridge.MGBridge.token;
import static vip.floatationdevice.mgbridge.MGBridge.cfgPath;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class BindManager implements Listener, CommandExecutor
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
        instance.getLogger().info(translate("connecting"));
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
        instance.getLogger().info(translate("connected"));
    }

    @Subscribe
    public void onG4JConnectionClosed(GuildedWebSocketClosedEvent event)
    {
        if(mgbRunning)
        {
            // if the plugin is running normally but the connection was closed
            // then we can consider it as unexpected and do a reconnection
            instance.getLogger().warning(translate("disconnected-unexpected"));
            ws = new G4JWebSocketClient(token);
            if(socksProxyHost != null && socksProxyPort != null)
                ws.setProxy(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(socksProxyHost, Integer.parseInt(socksProxyPort))));
            ws.eventBus.register(this);
            ws.connect();
        }
        else
            // the plugin is being disabled or the server is stopping, so we can just ignore this
            instance.getLogger().info(translate("disconnected"));
    }

    @Subscribe
    public void onGuildedChat(ChatMessageCreatedEvent event)
    {
        ChatMessage msg = event.getChatMessageObject();// the received ChatMessage object
        if(msg.getServerId().equals(MGBridge.server) && msg.getChannelId().equals(MGBridge.channel))// in the right server and channel?
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
                                    instance.getLogger().info(translate("c-bind-success").replace("%PLAYER%", getPlayerName(bindMap.get(msg.getCreatorId()))));
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
                            instance.getLogger().info(translate("c-unbind-success").replace("%PLAYER%", getPlayerName(removed)));
                            saveBindMap();
                        }
                        else// player not bound?
                            instance.sendGuildedMsg(translate("g-no-bind"), msg.getId());
                        break;
                    }
                    default:
                    {
                        if(!msg.getContent().startsWith("/") && bindMap.containsKey(msg.getCreatorId()))
                            Bukkit.broadcastMessage("<" + getPlayerName(bindMap.get(msg.getCreatorId())) + "> " + msg.getContent());
                        break;
                    }
                }
            }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
    {
        if(!(sender instanceof Player))
        {
            sender.sendMessage(translate("non-player-executor"));
            return false;
        }

        if(args.length == 1 && args[0].equals("mkbind"))
        {
            String code = "";// 10-digit random bind code from ASCII x21(!) to x7E(~)
            for(int i = 0; i != 10; i++) code += String.valueOf((char) (r.nextInt(94) + 33));// StringBuilder not needed
            if(pendingPlayerMap.containsKey(((Player) sender).getUniqueId())) // remove old bind code if exists
                pendingMap.remove(pendingPlayerMap.get(((Player) sender).getUniqueId()));
            pendingMap.put(code, ((Player) sender).getUniqueId());
            pendingPlayerMap.put(((Player) sender).getUniqueId(), code);
            sender.sendMessage(translate("m-code-requested").replace("%CODE%", code));
            instance.getLogger().info(translate("c-code-requested").replace("%PLAYER%", sender.getName()).replace("%CODE%", code));
            return true;
        }
        else if(args.length == 1 && args[0].equals("rmbind"))
        {
            for(String u : bindMap.keySet())
            {
                if(bindMap.get(u).equals(((Player) sender).getUniqueId()))
                {
                    bindMap.remove(u);
                    sender.sendMessage(translate("m-unbind-success"));
                    instance.getLogger().info(translate("c-unbind-success").replace("%PLAYER%", sender.getName()));
                    saveBindMap();
                    return true;
                }
            }
            sender.sendMessage(translate("m-no-bind"));
            return false;
        }
        else
        {
            sender.sendMessage(translate("m-usage"));
            return false;
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

    public void saveBindMap()
    {
        try
        {
            ObjectOutputStream o = new ObjectOutputStream(new FileOutputStream(cfgPath + "bindMap.dat"));
            o.writeObject(new BindMapContainer(bindMap));
            o.close();
            instance.getLogger().info(translate("bindmap-save-success"));
        }
        catch(Exception e)
        {
            instance.getLogger().severe(translate("bindmap-save-failure").replace("%EXCEPTION%", e.toString()));
        }
    }

    public void loadBindMap()
    {
        try
        {
            ObjectInputStream o = new ObjectInputStream(new FileInputStream(cfgPath + "bindMap.dat"));
            BindMapContainer temp = (BindMapContainer) o.readObject();
            o.close();
            bindMap = temp.saveBindMap;
            instance.getLogger().info(translate("bindmap-load-success"));
        }
        catch(FileNotFoundException ignored) {}
        catch(Exception e)
        {
            instance.getLogger().severe(translate("bindmap-load-failure").replace("%EXCEPTION%", e.toString()));
        }
    }
}
