package vip.floatationdevice.mgbridge;

import com.google.common.eventbus.Subscribe;
import org.bukkit.Bukkit;
import vip.floatationdevice.guilded4j.G4JWebSocketClient;
import vip.floatationdevice.guilded4j.event.ChatMessageCreatedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketClosedEvent;
import vip.floatationdevice.guilded4j.event.GuildedWebSocketInitializedEvent;
import vip.floatationdevice.guilded4j.object.ChatMessage;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.UUID;

import static vip.floatationdevice.mgbridge.BindManager.*;
import static vip.floatationdevice.mgbridge.I18nUtil.translate;
import static vip.floatationdevice.mgbridge.MGBridge.*;

@SuppressWarnings({"unused", "UnstableApiUsage"})
public class GuildedEventListener
{
    G4JWebSocketClient ws; // used to connect to guilded and receive guilded messages
    private final String socksProxyHost = System.getProperty("socksProxyHost"); // socks proxy settings in the arguments
    private final String socksProxyPort = System.getProperty("socksProxyPort");

    public GuildedEventListener()
    {
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
}
